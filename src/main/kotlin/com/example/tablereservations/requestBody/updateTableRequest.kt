package com.example.tablereservations.requestBody

data class updateTableRequest(
    val id: Int,
    val seats: Int,
    val status: Int
)
