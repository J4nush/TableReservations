package com.example.tablereservations.service
import com.example.tablereservations.data.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.expression.common.ExpressionUtils.toInt
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class AuthService {

    @Autowired
    private lateinit var usersService: UsersService

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

    fun readRole(token: String): Int{
        return try {
            val claims = parseToken(token)
            val role_id = claims["role_id"]
            role_id as Int
        }catch (e: Exception){
            0
        }
    }
    fun readUserId(token: String): Int{
        return try {
            val claims = parseToken(token)
            val user_id = claims["user_id"]
            user_id.toString().toInt()
        }catch (e: Exception){
            0
        }
    }
    fun readLogin(token: String): Any?{
        return try {
            val claims = parseToken(token)
            val user_login = claims["user_login"]
            user_login
        }catch (e: Exception){
            false
        }
    }

    fun refreshToken(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).body
            val expiration = claims.expiration
            if (expiration.after(Date()) && expiration.before(Date(System.currentTimeMillis() + 5 * 60 * 1000))) {
                val user = usersService.findUserById(claims.subject.toInt())
                generateToken(user!!)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
