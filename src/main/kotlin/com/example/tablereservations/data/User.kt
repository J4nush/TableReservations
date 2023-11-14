package com.example.tablereservations.data

data class User(val id: Int,
                val login: String,
                val password: String,
                val first_name: String,
                val last_name: String,
                val role_id: Int,
                val status_id: Int)
