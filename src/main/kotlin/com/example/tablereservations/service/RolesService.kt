package com.example.tablereservations.service
import com.example.tablereservations.data.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service

@Service
class RolesService(val db: JdbcTemplate) {
    fun findRoles():List<Role> = db.query("select * from roles") { response, _ ->
        Role(
            response.getInt("id"),
            response.getString("key"),
            response.getString("name")
        )
    }

    fun findSRoleByKey(status_key: String): List<Role> = db.query("select * from roles where `key` = ?", status_key){
            response, _ -> Role(
        response.getInt("id"),
        response.getString("key"),
        response.getString("name")
    )
    }
}