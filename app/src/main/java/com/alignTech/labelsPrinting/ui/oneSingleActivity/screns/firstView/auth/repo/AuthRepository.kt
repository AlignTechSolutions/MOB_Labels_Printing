package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo

import androidx.lifecycle.MutableLiveData
import com.alignTech.labelsPrinting.domain.model.auth.*
import com.alignTech.labelsPrinting.domain.network.networkSettings.ServerCallBack


/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/27/2021 - 3:08 PM
 */
interface AuthRepository{

    // Network Api
    fun requestLogin(objectAuth : AuthData): MutableLiveData<ServerCallBack<AuthDataItem>>

    fun requestCompanyUrl(companyCode: String): MutableLiveData<ServerCallBack<CompanyConfigs>>

    fun requestLogout(tokenId: LogoutData): MutableLiveData<ServerCallBack<Any>>

    fun resetPasswordRequest(requestObject: ResetPasswordData): MutableLiveData<ServerCallBack<Any>>


}