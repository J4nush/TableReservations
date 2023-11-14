package com.example.tablereservations.service
import com.example.tablereservations.data.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class AuthService {
    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private val expirationTime: Long = 0

    private lateinit var signingKey: Key

    @PostConstruct
    fun init() {
        signingKey = Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(user: User): String {
        val expirationDate = Date(System.currentTimeMillis() + expirationTime)
        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("login", user.login)
            .claim("role_id", user.role_id)
            .claim("user_id", user.id)
            .setExpiration(expirationDate)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun parseToken(token: String): Claims {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).body
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).body
            val expiration = claims.expiration
            expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun readRole(token: String): Any?{
        try {
            val claims = parseToken(token)
            val role_id = claims["role_id"]
            return role_id
        }catch (e: Exception){
            return false
        }
    }
    fun readUser(token: String): Any?{
        try {
            val claims = parseToken(token)
            val user_id = claims["user_id"]
            return user_id
        }catch (e: Exception){
            return false
        }
    }
    fun readLogin(token: String): Any?{
        try {
            val claims = parseToken(token)
            val user_login = claims["user_login"]
            return user_login
        }catch (e: Exception){
            return false
        }
    }
    fun refreshToken(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).body
            val expiration = claims.expiration
            if (expiration.after(Date()) && expiration.before(Date(System.currentTimeMillis() + 5 * 60 * 1000))) {
                val user = User(
                    claims.subject.toInt(),
                    claims["login"].toString(),
                    "",
                    "",
                    "",
                    claims["role_id"].toString().toInt(),
                    1 // Assuming some status_id, you might want to change this
                )
                generateToken(user)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
