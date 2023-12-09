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
            response.getString("hour"),
            response.getString("email"),
            response.getInt("status_id")
        )
    }

    fun getReservationsByDate(date: Date): MutableList<Reservation> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return db.query("""SELECT * FROM reservations r WHERE YEAR(r.date) = ? AND MONTH(r.date) = ? AND DAYOFMONTH(r.date) = ?""",
            arrayOf(year, month, day)) { response, _ ->
            Reservation(
                response.getInt("id"),
                response.getInt("table_id"),
                response.getDate("date"),
                response.getString("hour"),
                response.getString("email"),
                response.getInt("status_id")
            )
        }
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
                response.getString("hour"),
                response.getString("email"),
                response.getInt("status_id")
            )
        }
        }
        return try {
            if(temp?.size!! > 0){
                true
            }else{
                false
            }
        }catch (e: Exception){
            false
        }

    }

    fun save(reservation: Reservation) {
        db.update(
            "insert into reservations (table_id, `date`, `hour`, email, status_id) values (?, ?, ?, ?, ?)",
            reservation.table_id, reservation.date, reservation.hour, reservation.email, reservation.status
        )
    }

    fun makeReservationAccepted(reservation_id: Int): Boolean {
        return try {
            db.update("update reservations set status_id = 4 where id = ? ", reservation_id)
            true
        }catch (e: Exception){
            false
        }
    }

    fun makeReservationCanceled(reservation_id: Int): Boolean {
        return try{
            db.update("update reservations set status_id = 5 where id = ? ", reservation_id)
            true
        }catch (e: Exception){
            false
        }
    }

    fun updateReservationStatus(reservationId: Int, statusId: Int) {
        db.update("update reservations set status_id = ? where id = ?", statusId, reservationId)
    }


}