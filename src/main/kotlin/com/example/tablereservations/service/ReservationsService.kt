package com.example.tablereservations.service

import com.example.tablereservations.data.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReservationsService(val db: JdbcTemplate) {

    fun findReservations(): List<Reservation> = db.query("select * from reservations") { response, _ ->
        Reservation(
            response.getInt("id"),
            response.getInt("table_id"),
            response.getDate("date"),
            response.getString("email"),
            response.getInt("status_id")
        )
    }

    fun isResevationExistForThisDay(table_id: Int, date: Date): Boolean{
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val temp = db.query("""SELECT * FROM reservations r WHERE r.table_id = ? AND 
            YEAR(r.date) = ? AND MONTH(r.date) = ? AND DAYOFMONTH(r.date) = ? 
        """.trimMargin(),  arrayOf(table_id, year, month, day)){
            response, _ -> {
            Reservation(
                response.getInt("id"),
                response.getInt("table_id"),
                response.getDate("date"),
                response.getString("email"),
                response.getInt("status_id")
            )
        }
        }
        try {
            if(temp?.size!! > 0){
                return true
            }
        }
        return false

    }

    fun save(reservation: Reservation) {
        db.update(
            "insert into reservations (table_id, date, email, status_id) values (?, ?, ?, ?)",
            reservation.table_id, reservation.date, reservation.email, reservation.status
        )
    }


    fun updateReservationStatus(reservationId: Int, statusId: Int) {
        db.update("update reservations set status_id = ? where id = ?", statusId, reservationId)
    }


}