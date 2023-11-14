package com.example.tablereservations.service
import com.example.tablereservations.data.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service


@Service
class UsersService(val db: JdbcTemplate) {
    fun findUsers(): List<User> = db.query("select * from users") { response, _ ->
        User(response.getInt("id"), response.getString("login"), response.getString("password"),
            response.getString("first_name"), response.getString("last_name"),
            response.getInt("role_id"), response.getInt("status_id")
            )
    }

    fun createUser(user: User){
        db.update("insert into users (login, password, first_name, last_name, role_id, status_id) values (?, ?, ?, ?, ?, ?, ?)",
            user.login, user.password, user.first_name, user.last_name, user.role_id, user.status_id
            )
    }

    fun findUserByLogin(login: String): User? {
        return db.query("select * from users where login = ?", arrayOf(login)) { response, _ ->
            User(response.getInt("id"), response.getString("login"), response.getString("password"),
                response.getString("first_name"), response.getString("last_name"),
                response.getInt("role_id"), response.getInt("status_id")
            )
        }.firstOrNull()
    }



}