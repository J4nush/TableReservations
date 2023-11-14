package com.example.tablereservations.helper
import java.math.BigInteger
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class HashHelper {
    fun createMD5(text:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(text.toByteArray())).toString(16).padStart(32, '0')
    }

    fun compareStrings(text: String, hash: String): Boolean{
        val md = MessageDigest.getInstance("MD5")
        return hash == BigInteger(1, md.digest(text.toByteArray())).toString(16).padStart(32, '0')
    }
}