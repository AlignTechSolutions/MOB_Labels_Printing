package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.viewModel

import androidx.lifecycle.ViewModel
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.domain.model.auth.AuthData
import com.alignTech.labelsPrinting.domain.model.auth.AuthDataItem
import com.alignTech.labelsPrinting.domain.model.auth.ResetPasswordData
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

   fun requestLogin(loginObject: AuthData) = repository.requestLogin(loginObject)

  fun requestCompanyUrl(companyCode: String) = repository.requestCompanyUrl(companyCode)

  fun saveToken(appPreferences: KUPreferences, data: AuthDataItem) = TokenUtil.saveToken(appPreferences, data)

  fun resetPasswordRequest(requestObject: ResetPasswordData) = repository.resetPasswordRequest(requestObject)

}