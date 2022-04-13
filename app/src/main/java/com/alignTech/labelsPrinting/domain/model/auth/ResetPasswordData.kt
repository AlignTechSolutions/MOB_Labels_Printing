package com.alignTech.labelsPrinting.domain.model.auth

data class ResetPasswordData(
    val userId: String,
    val oldPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)