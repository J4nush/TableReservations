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
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
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
    @Configuration
    class WebConfiguration : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**").allowedMethods("*")
        }
    }
    @PostConstruct
    fun init() {
        roles = rolesService.findRoles()
    }


    @GetMapping("/")
    fun index(): List<Status> = statusService.findStatuses()

    @PostMapping("/token_validity")
    fun validateTokenAndRefresh(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<Any> {     if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(401).body("Unauthorized")
    }
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid token")
        }

        val refreshedToken = authService.refreshToken(token)
        if (refreshedToken != null) {
            val userId = authService.readUserId(token)
            if (userId == 0){
                return ResponseEntity.status(401).body("Unauthorized")
            }
            val user = usersService.findUserById(userId)
            val jsonResponse = "{\"token\": \"${refreshedToken}\", \"role\": \"${user!!.role_id}\", \"first_name\": \"${user!!.first_name}\"}"
            return ResponseEntity.ok(jsonResponse)
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Token is valid")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<String> {

        val user = usersService.findUserByLogin(loginRequest.login)
        println(user)
        if (user != null && hashHelper.compareStrings(loginRequest.password, user.password)) {
//            return ResponseEntity.ok("Zalogowano pomyślnie")
            val token = authService.generateToken(user)
            val jsonResponse = "{\"token\": \"${token}\", \"role\": \"${user.role_id}\", \"first_name\": \"${user.first_name}\"}"
            return ResponseEntity.ok(jsonResponse)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong login credentials")
    }

    @GetMapping("/tables")
    fun getTables(): ResponseEntity<Any> {
        return ResponseEntity.ok(tablesService.findTables())
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

    @PostMapping("/accept_reservation")
    fun acceptReservation(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody reservationRequest: changeReservationRequest
    ): ResponseEntity<Any>{
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized")
        }
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2 && authService.readRole(token) != 3 )
            return ResponseEntity.status(401).body("Invaild role")
        
        return if(reservationsService.makeReservationAccepted(reservationRequest.reservation_id)){
            ResponseEntity.ok(reservationRequest.reservation_id)
        }else{
            ResponseEntity.status(HttpStatus.CONFLICT).body("Error has occured.")
        }
    }

    @PostMapping("/reject_reservation")
    fun rejectReservation(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody reservationRequest: changeReservationRequest
    ): ResponseEntity<Any>{
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized")
        }
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2 && authService.readRole(token) != 3 )
            return ResponseEntity.status(401).body("Invaild role")

        return if(reservationsService.makeReservationCanceled(reservationRequest.reservation_id)){
            ResponseEntity.ok(reservationRequest.reservation_id)
        }else{
            ResponseEntity.status(HttpStatus.CONFLICT).body("Error has occured.")
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        if (usersService.findUserByLogin(createRequest.login) != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create user. User with this login exist.")
        createRequest.password = hashHelper.createMD5(createRequest.password)
        usersService.createUser(createRequest)
        return ResponseEntity.ok("User -" + createRequest.login + "- created")
    }

    @PostMapping("/edit_user")
    fun editUser(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody editRequest: editUserRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized")
        }
        val token = authorizationHeader.substring(7)
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")
        println(editRequest.toString())

        if (usersService.findUserById(editRequest.user_id) == null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't edit user. This user don't exist.")

        return try{
            usersService.editUser(editRequest)
            ResponseEntity.ok("User has been edited.")
        }catch (e: Exception){
            ResponseEntity.status(HttpStatus.CONFLICT).body("An error has occured")
        }


    }

    @GetMapping("/users")
    fun getUsers(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
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
        val token = authorizationHeader.substring(7) 
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        usersService.blockUser(changeUser.user_id)
        return ResponseEntity.ok(usersService.findUserById(changeUser.user_id))
    }

    @PostMapping("/close_table")
    fun closeTable(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody table: changeTableRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7)
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        tablesService.closeTable(table.table_id)
        return ResponseEntity.ok(tablesService.findTableById(table.table_id))
    }

    @PostMapping("/open_table")
    fun openTable(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody table: changeTableRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7)
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        tablesService.openTable(table.table_id)
        return ResponseEntity.ok(tablesService.findTableById(table.table_id))
    }

    @PostMapping("/new_table")
    fun openTable(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody table: newTableRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7)
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        tablesService.save(table)
        return ResponseEntity.ok("Table has been added.")
    }
    @PostMapping("/edit_table")
    fun openTable(
        @RequestHeader("Authorization") authorizationHeader: String?,
        @RequestBody table: updateTableRequest
    ): ResponseEntity<Any> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Unauthorized")
        val token = authorizationHeader.substring(7)
        if (!authService.validateToken(token)) return ResponseEntity.status(401).body("Invaild token")
        if (authService.readRole(token) == 0 && authService.readRole(token) != 1 && authService.readRole(token) != 2)
            return ResponseEntity.status(401).body("Invaild role")

        tablesService.udpdateTable(table)
        return ResponseEntity.ok("Table has been updated.")
    }

}