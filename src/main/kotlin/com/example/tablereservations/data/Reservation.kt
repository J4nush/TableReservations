package com.example.tablereservations.data

import java.util.*

data class Reservation(val id: Int, val table_id: Int, val date: Date, val email: String, val status: Int)
