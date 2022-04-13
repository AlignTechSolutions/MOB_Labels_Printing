package com.alignTech.labelsPrinting.domain.model.auth

data class AuthDataItem(
    var token: String? = "",
    var refreshToken: String? = "",
    var jobId:String = "",
    var success:Boolean? = false,
    var errors: ArrayList<String>?,
)