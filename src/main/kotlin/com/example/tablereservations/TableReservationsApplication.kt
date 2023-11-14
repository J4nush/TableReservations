package com.example.tablereservations

import com.example.tablereservations.helper.HashHelper
import com.example.tablereservations.service.*
import com.example.tablereservations.data.*
import com.fasterxml.jackson.module.kotlin.jsonMapper
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
import java.time.LocalDate
import java.util.Date


@SpringBootApplication
class TableReservationsApplication

fun main(args: Array<String>) {
    runApplication<TableReservationsApplication>(*args)
}

@RestController
class MessageController(val tablesService: TablesService,
                        val statusService: StatusesService,
                        val usersService: UsersService,
                        val rolesService: RolesService,
                        val hashHelper: HashHelper,
                        val authService: AuthService
){
    private lateinit var roles: List<Role>

    @PostConstruct
    fun init(){
        roles = rolesService.findRoles()
    }


    @GetMapping("/")
    fun index(): List<Status> = statusService.findStatuses()

    @GetMapping("/init")
    fun initThisShit() = tablesService.initTables()

    @GetMapping("/test")
    fun genMD5(@RequestHeader("Authorization") authorizationHeader: String?): ResponseEntity<String>{
//        return hashHelper.createMD5(text)
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Brak tokenu autoryzacyjnego")
        }

        val token = authorizationHeader.substring(7) // Usuń "Bearer " z nagłówka
        if (authService.validateToken(token)) {
            // Token jest ważny
            return ResponseEntity.ok(roles.toString())
        }

        return ResponseEntity.status(401).body("Nieprawidłowy token")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<String> {
        val user = usersService.findUserByLogin(loginRequest.login)
        if (user != null && hashHelper.compareStrings(loginRequest.password, user.password)) {
//            return ResponseEntity.ok("Zalogowano pomyślnie")
            val token = authService.generateToken(user)
            return ResponseEntity.ok(token)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Niepoprawny login lub hasło")
    }

    @GetMapping("/tables")
    fun getTables(): ResponseEntity<Any>{
        return ResponseEntity.ok(tablesService.findTables())
    }

    @GetMapping("/avaible_tables")
    fun getAvaibleTables(@RequestParam date: String?): ResponseEntity<Any>{
        val dateAsObject : Date
        dateAsObject = if(date == null){
            Date()

        }else{
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.parse(date)
        }
        return  ResponseEntity.ok(tablesService.findAvailableTablesForDate(dateAsObject))
    }



}