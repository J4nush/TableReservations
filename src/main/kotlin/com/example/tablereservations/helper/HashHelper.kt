package com.example.tablereservations.helper
import java.math.BigInteger
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class HashHelper {
    fun createMD5(text:String): String {
        println(text)
        val md = MessageDigest.getInstance("MD5")
        val temp = BigInteger(1, md.digest(text.toByteArray())).toString(16).padStart(32, '0')
        println(temp)
        return temp
    }

    fun compareStrings(text: String, hash: String): Boolean{
//        println(text)
        println(hash)
        val md = MessageDigest.getInstance("MD5")
        return hash == BigInteger(1, md.digest(text.toByteArray())).toString(16).padStart(32, '0')
    }
}