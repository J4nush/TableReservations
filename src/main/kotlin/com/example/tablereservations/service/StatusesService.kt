package com.example.tablereservations.service
import com.example.tablereservations.data.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service

@Service
class StatusesService(val db: JdbcTemplate) {

    fun findStatuses():List<Status> = db.query("select * from statuses") { response, _ ->
        Status(
            response.getInt("id"),
            response.getString("key"),
            response.getString("name")
        )
    }

    fun findStatusByKey(status_key: String): List<Status> = db.query("select * from statuses where `key` = ?", status_key){
        response, _ -> Status(
        response.getInt("id"),
        response.getString("key"),
        response.getString("name")
        )
    }
}