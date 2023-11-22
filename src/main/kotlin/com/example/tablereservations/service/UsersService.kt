package com.example.tablereservations.service
import com.example.tablereservations.data.*
import com.example.tablereservations.requestBody.CreateUserRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service


@Service
class UsersService(val db: JdbcTemplate) {
    fun findUsers(): List<User> = db.query("select * from users") { response, _ ->
        User(response.getInt("id"), response.getString("login"), "",
            response.getString("first_name"), response.getString("last_name"),
            response.getInt("role_id"), response.getInt("status_id")
            )
    }

    fun createUser(user: CreateUserRequest){
        db.update("insert into users (login, password, first_name, last_name, role_id, status_id) values (?, ?, ?, ?, ?, ?)",
            user.login, user.password, user.first_name, user.last_name, user.role_id, user.status_id
            )
    }


    fun findUserByLogin(login: String): User? {
        return db.query("select * from users where login = ?", arrayOf(login)) { response, _ ->
            User(response.getInt("id"), response.getString("login"),  response.getString("password"),
                response.getString("first_name"), response.getString("last_name"),
                response.getInt("role_id"), response.getInt("status_id")
            )
        }.firstOrNull()
    }

    fun findUserById(id: Int): User? {
        return db.query("select * from users where id = ?", arrayOf(id)) { response, _ ->
            User(response.getInt("id"), response.getString("login"), "",
                response.getString("first_name"), response.getString("last_name"),
                response.getInt("role_id"), response.getInt("status_id")
            )
        }.firstOrNull()
    }

    fun changeUserRole(user_id: Int, new_role: Int){
        db.update("UPDATE users SET role_id = ? WHERE id = ?", arrayOf(new_role, user_id))
    }

    fun makeUserAnAdmin(user_id: Int){
        db.update("UPDATE users SET role_id = 1 WHERE id = ?", user_id)

    }
    fun makeUserAnManager(user_id: Int){
        db.update("UPDATE users SET role_id = 2 WHERE id = ?", user_id)

    }
    fun makeUserAnWorker(user_id: Int){
        db.update("UPDATE users SET role_id = 3 WHERE id = ?", user_id)
    }

    fun activateUser(user_id: Int){
        db.update("UPDATE users SET status_id = 7 WHERE id = ?", user_id)

    }
    fun blockUser(user_id: Int){
        db.update("UPDATE users SET status_id = 8 WHERE id = ?", user_id)

    }

    fun changeUserStatus(user_id: Int, new_role: Int){
        db.update("UPDATE users SET role_id = ? WHERE id = ?", arrayOf(new_role, user_id))
    }


}