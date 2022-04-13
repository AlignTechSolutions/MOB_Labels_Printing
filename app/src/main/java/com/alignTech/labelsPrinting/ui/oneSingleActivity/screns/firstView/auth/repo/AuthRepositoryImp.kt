package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo

import com.alignTech.labelsPrinting.core.util.AppConstants.BASE_URL
import com.alignTech.labelsPrinting.core.util.AppConstants.DOMAIN_URL
import com.alignTech.labelsPrinting.domain.model.auth.AuthData
import com.alignTech.labelsPrinting.domain.model.auth.LogoutData
import com.alignTech.labelsPrinting.domain.model.auth.ResetPasswordData
import com.alignTech.labelsPrinting.domain.network.ApiService
import com.alignTech.labelsPrinting.domain.network.networkSettings.NetworkCall
import com.alignTech.labelsPrinting.domain.network.networkSettings.NetworkUtil
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(): AuthRepository{

    @Inject
    lateinit var  apiService: ApiService

    override fun requestLogin(objectAuth: AuthData)= NetworkCall.loginCall {
        apiService.requestLogin(NetworkUtil.getApiLink(BASE_URL, NetworkUtil.NetworkLinks.Login.type), objectAuth)
    }

    override fun requestCompanyUrl(companyCode: String)= NetworkCall.makeCall {
        apiService.requestCompanyConfiguration(NetworkUtil.getApiLink(DOMAIN_URL, NetworkUtil.NetworkLinks.CompanyConfiguration.type), companyCode)
    }

    override fun requestLogout(tokenId: LogoutData)= NetworkCall.makeCall {
        apiService.requestLogout(NetworkUtil.getApiLink(BASE_URL, NetworkUtil.NetworkLinks.Logout.type), tokenId)
    }

    override fun resetPasswordRequest(requestObject: ResetPasswordData) = NetworkCall.makeCall {
        apiService.requestResetPassword(NetworkUtil.getApiLink(BASE_URL, NetworkUtil.NetworkLinks.ResetPassword.type), requestObject)
    }


}