package com.example.tablereservations.requestBody
data class CreateReservationRequest(val table_id: Int,
                                    val date: String?,
                                    val email: String,
                                    val hour: String)
