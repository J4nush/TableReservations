package com.example.tablereservations.requestBody

data class CreateUserRequest(
    val login: String,
    var password: String,
    val first_name: String,
    val last_name: String,
    val role_id: Int,
    val status_id: Int
)