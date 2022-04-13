package com.alignTech.labelsPrinting.domain.model.jwt

data class JwtClaim(
    val sub: String = "",
    val nbf: Int = 0,
    val CompanyId: String = "",
    val EmployeeId: String = "",
    val IsSalesPerson:String = "",
    val CompanyBranchId: String = "",
    val ActiveSalesPersonId: String = "",
    val id: String = "",
    val exp: Int = 0,
    val iat: Int = 0,
    val jti: String = "",
    val email: String = ""
)