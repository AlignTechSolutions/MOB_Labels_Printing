package com.alignTech.labelsPrinting.core.util

import android.util.Log
import com.google.gson.Gson
import com.alignTech.labelsPrinting.core.util.EncryptionUtil.decodeFromBase64
import com.alignTech.labelsPrinting.domain.model.jwt.JwtClaim

object JwtUtil {

    private const val TAG = "JwtUtil"

    fun getJwtPayload(JWTEncoded: String): JwtClaim? {
        var result: JwtClaim? = null
        try {
            val split = JWTEncoded.split(".").toTypedArray()
            val jwtDetails = Gson().fromJson(decodeFromBase64(split[1]), JwtClaim::class.java)
            result = jwtDetails
        } catch (e: Exception) { //Error
            e.printStackTrace()
        }
        Log.i(TAG, "getJwtPayload: $result")
        return result
    }

}