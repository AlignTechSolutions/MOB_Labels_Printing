package com.alignTech.labelsPrinting.domain.network

import com.alignTech.labelsPrinting.domain.model.auth.*
import com.alignTech.labelsPrinting.domain.model.baseResponse.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    /**-----------------------   POST   ---------------------*/

    @POST
    suspend fun requestLogin(@Url url: String, @Body authObject: AuthData): Response<AuthDataItem>

    @POST
    suspend fun requestCompanyConfiguration(@Url url: String, @Query("code") companyCode: String): Response<BaseResponse<CompanyConfigs>>

    @POST
    suspend fun requestLogout(@Url url: String, @Body tokenId: LogoutData): Response<BaseResponse<Any>>

    @POST
    suspend fun requestResetPassword(@Url url: String, @Body resetObject: ResetPasswordData): Response<BaseResponse<Any>>

}