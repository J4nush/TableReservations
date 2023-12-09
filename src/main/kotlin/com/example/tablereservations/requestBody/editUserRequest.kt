package com.example.tablereservations.requestBody

import org.springframework.http.HttpStatus

data class editUserRequest(
    val user_id: Int,
    val login: String,
    val first_name: String,
    val last_name: String,
    val role_id: Int,
    val status_id: Int
)
