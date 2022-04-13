package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.AppConstants.BASE_URL_KEY
import com.alignTech.labelsPrinting.core.util.AppConstants.FCM_TOKEN
import com.alignTech.labelsPrinting.core.util.AppConstants.VERSION_NAME
import com.alignTech.labelsPrinting.core.util.AppConstants.loadBaseUrl
import com.alignTech.labelsPrinting.core.util.checkEmptyEditText
import com.alignTech.labelsPrinting.core.util.showAlertDialog
import com.alignTech.labelsPrinting.core.util.validateMobileVersion
import com.alignTech.labelsPrinting.databinding.FragmentAuthBinding
import com.alignTech.labelsPrinting.domain.model.auth.AuthData
import com.alignTech.labelsPrinting.domain.model.auth.CompanyConfigs
import com.alignTech.labelsPrinting.domain.network.networkSettings.ServerCallBack
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.viewModel.LoginViewModel
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentAuthBinding) {
        dataBinder.apply {
            fragment  = this@AuthFragment
            lifecycleOwner  = this@AuthFragment

            /* text Watcher to remove error editText when user write ...*/
            userNameEt.kuRemoveErrorListener()
            passwordEt.kuRemoveErrorListener()
            companyCodeEt.kuRemoveErrorListener()

            val version = String.format(kuRes.getString(R.string.version) , VERSION_NAME)
            tvVersion.text = version


        }
    }

    override fun setUpViewModelStateObservers() {}


    fun login(){
        if (validateInputs()){
            kuHideSoftKeyboard()
            requestCompanyCode(dataBinder.companyCodeEt.text.toString())
        }
    }

    private fun validateInputs(): Boolean {
        val status: Boolean
        dataBinder.apply {
            when {
                userNameEt.checkEmptyEditText() -> {
                    userNameEt.error = kuRes.getString(R.string.required)
                    kuToast(kuRes.getString(R.string.please_enter_user_name))
                    status = false
                }
                passwordEt.checkEmptyEditText() -> {
                    passwordEt.error = kuRes.getString(R.string.required)
                    kuToast(kuRes.getString(R.string.please_enter_password))
                    status = false
                }
                companyCodeEt.checkEmptyEditText() -> {
                    companyCodeEt.error = kuRes.getString(R.string.required)
                    kuToast(kuRes.getString(R.string.please_enter_company_code))
                    status = false
                }
                else -> {
                    status = true
                }
            }
        }
        return status
    }


    private fun requestCompanyCode(code: String) {
        dataBinder.apply {
            viewModel.requestCompanyUrl(code).observe(viewLifecycleOwner) {
                when (it.status) {
                    ServerCallBack.Status.LOADING -> {
                        pbLogin.kuShow()
                        errorMessage.kuHide()
                    }
                    ServerCallBack.Status.ERROR -> {
                        pbLogin.kuHide()
                        errorMessage.kuShow()
                        errorMessage.text = it.message
                    }
                    ServerCallBack.Status.SUCCESS -> {
                        successResultOfCompanyCode(it, code)
                    }
                }
            }
        }
    }

    private fun successResultOfCompanyCode(it: ServerCallBack<CompanyConfigs>, code: String) {
        dataBinder.apply{
            if (it.data != null) {
                if (it.data.mobileVersion != null && validateMobileVersion(it.data.mobileVersion!!)) {
                    showAlertDialog(requireContext(), kuRes.getString(R.string.old_app_version)) { kuOpenAppOnGooglePlay() }
                } else {

                    appPreferences.setValue(BASE_URL_KEY , it.data.companyLink)
                    loadBaseUrl(appPreferences)

                    requestLogin(userNameEt.text.toString(), passwordEt.text.toString(), code)
                }
            } else {
                pbLogin.kuHide()
                errorMessage.kuShow()
                errorMessage.text = kuRes.getString(R.string.company_url_not_found)
            }
        }

    }

    private fun requestLogin(userName: String, password: String, companyCode: String) {
        val fcmToken = appPreferences.getStringValue(FCM_TOKEN,"")
        val loginObject = AuthData(companyCode, password, userName, fcmToken.ifEmpty { null })

        dataBinder.apply {
            viewModel.requestLogin(loginObject).observe(viewLifecycleOwner) {
                when (it.status) {
                    ServerCallBack.Status.ERROR -> {
                        pbLogin.kuHide()
                        errorMessage.kuShow()
                        errorMessage.text = it.message
                    }
                    ServerCallBack.Status.SUCCESS -> {
                        pbLogin.kuHide()
                        viewModel.saveToken(appPreferences, it.data!!)
                        navController.navigate(R.id.action_authFragment_to_labelsPrintingFragment)
                        (activity as OneSingleActivity).apply {
                            snackBarError("تم تسجيل الدخول بنجاح" , R.color.TemplateGreen, R.color.white)
                        }
                    }
                    else -> {
                        pbLogin.kuShow()
                        errorMessage.kuHide()
                    }
                }
            }
        }

    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_auth
    


}