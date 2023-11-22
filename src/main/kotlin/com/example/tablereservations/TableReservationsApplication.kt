package com.example.tablereservations

import com.example.tablereservations.helper.HashHelper
import com.example.tablereservations.service.*
import com.example.tablereservations.data.*
import com.example.tablereservations.requestBody.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.text.SimpleDateFormat
import java.util.Date


@SpringBootApplication
class TableReservationsApplication

fun main(args: Array<String>) {
    runApplication<TableReservationsApplication>(*args)
}


@RestController
class MessageController(
    val tablesService: TablesService,
    val statusService: StatusesService,
    val usersService: UsersService,
    val rolesService: RolesService,
    val hashHelper: HashHelper,
    val authService: AuthService,
    val reservationsService: ReservationsService
) {
    private lateinit var roles: List<Role>

    @PostConstruct
    fun init() {
        roles = rolesService.findRoles()
    }


    @GetMapping("/")
    fun index(): List<Status> = statusService.findStatuses()

    @GetMapping("/init")
    fun initThisShit() = tablesService.initTables()

    @GetMapping("/test")
    fun genMD5(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<String> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized")
        }
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (authService.validateToken(token)) {
            // Token jest ważny
            return ResponseEntity.ok(authService.readRole(token).toString())
        }

        return ResponseEntity.status(401).body("Invaild toknen")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<String> {

        val user = usersService.findUserByLogin(loginRequest.login)
        println(user)
        if (user != null && hashHelper.compareStrings(loginRequest.password, user.password)) {
//            return ResponseEntity.ok("Zalogowano pomyślnie")
            val token = authService.generateToken(user)
            return ResponseEntity.ok(token)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Niepoprawny login lub hasło")
    }

    @GetMapping("/tables")
    fun getTables(): ResponseEntity<Any> {
        return ResponseEntity.ok(tablesService.findTables())
    }

    @GetMapping("/test_av")
    fun testFunction(@RequestParam date: String?, @RequestParam table: Int): ResponseEntity<Boolean> {
        val dateAsObject: Date
        dateAsObject = if (date == null) {
            Date()
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.parse(date)
        }

        return ResponseEntity.ok(reservationsService.isResevationExistForThisDay(table, dateAsObject))
    }

    @GetMapping("/all_reservations")
    fun getAllReservations(): ResponseEntity<Any> {
        return ResponseEntity.ok(reservationsService.findReservations())
    }

    @GetMapping("/reservations_by_date")
    fun getReservationsByDate(@RequestParam date: String?): ResponseEntity<Any> {
        val dateAsObject: Date
        dateAsObject = if (date == null) {
            Date()
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.parse(date)
        }
        return ResponseEntity.ok(reservationsService.getReservationsByDate(dateAsObject))
    }

    @GetMapping("/avaible_tables")
    fun getAvaibleTables(@RequestParam date: String?): ResponseEntity<Any> {
        val dateAsObject: Date
        dateAsObject = if (date == null) {
            Date()

        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.parse(date)
        }
        return ResponseEntity.ok(tablesService.findAvailableTablesForDate(dateAsObject))
    }

    @PostMapping("/create_reservation")
    fun makeAnReservation(
        @RequestBody createRequest: CreateReservationRequest
    ): ResponseEntity<Any> {
        val dateAsObject: Date
        dateAsObject = if (createRequest.date == null) {
            Date()

        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.parse(createRequest.date)
        }

        return if (!reservationsService.isResevationExistForThisDay(createRequest.table_id, dateAsObject)) {
            reservationsService.save(
                Reservation(
                    0,
                    createRequest.table_id,
                    dateAsObject,
                    createRequest.hour,
                    createRequest.email,
                    statusService.findStatusByKey("RES_NEW")[0].id
                )
            )
            ResponseEntity.ok("Reservation created")
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create reservations for this date")
        }
    }


    @PostMapping("/create_user")
    fun createUser(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody createRequest: CreateUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized")
        }
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        if (usersService.findUserByLogin(createRequest.login) != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create user. User with this login exist.")
        createRequest.password = hashHelper.createMD5(createRequest.password)
        usersService.createUser(createRequest)
        return ResponseEntity.ok("User -" + createRequest.login + "- created")
    }

    @GetMapping("/users")
    fun getUsers(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        return ResponseEntity.ok(usersService.findUsers())
    }

    @PostMapping("/user_as_admin")
    fun makeUserAnAdmin(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody changeUser: changeUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.makeUserAnAdmin(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

    @PostMapping("/user_as_manager")
    fun makeUserAnManager(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody changeUser: changeUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.makeUserAnManager(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

    @PostMapping("/user_as_worker")
    fun makeUserAnWorker(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody changeUser: changeUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.makeUserAnWorker(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

    @PostMapping("/activate_user")
    fun activateUser(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody changeUser: changeUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.activateUser(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

    @PostMapping("/block_user")
    fun blockUser(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody changeUser: changeUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 || authService.readRole(token) != 1 || authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.blockUser(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

}