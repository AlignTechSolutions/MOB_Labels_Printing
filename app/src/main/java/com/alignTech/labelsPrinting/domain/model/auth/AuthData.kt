package com.alignTech.labelsPrinting.domain.model.auth

import com.google.gson.annotations.SerializedName

data class AuthData(
    val companyCode: String = "",
    val password: String = "",
    val userName: String = "",
    @SerializedName("userToken") var fcmToken:String? = null
)