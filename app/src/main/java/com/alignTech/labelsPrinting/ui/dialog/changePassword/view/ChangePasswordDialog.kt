package com.alignTech.labelsPrinting.ui.dialog.changePassword.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alfayedoficial.kotlinutils.kuHide
import com.alfayedoficial.kotlinutils.kuRemoveErrorListener
import com.alfayedoficial.kotlinutils.kuShow
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.databinding.DialogChangePassBinding
import com.alignTech.labelsPrinting.domain.model.auth.ResetPasswordData
import com.alignTech.labelsPrinting.domain.network.networkSettings.ServerCallBack
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.viewModel.LoginViewModel
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordDialog : BaseDialogFragment<DialogChangePassBinding>() {

  private val viewModel: LoginViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    super.onCreateView(inflater, container, savedInstanceState)

    return rootView
  }

  override fun onFragmentCreated(dataBinder: DialogChangePassBinding) {
    dataBinder.apply {

      /* text Watcher to remove error editText when user write ...*/
      oldPassword.kuRemoveErrorListener()
      newPassword.kuRemoveErrorListener()
      confirmNewPass.kuRemoveErrorListener()

      cancelBtn.setOnClickListener { dismiss() }
      changePassBtn.setOnClickListener {
        if (validatePasswords()) {
          requestChangePassword(
            TokenUtil.getTokenId(appPreferences),
            oldPassword.text.toString(), newPassword.text.toString())
        }
      }
    }
  }

  private fun validatePasswords(): Boolean {
    val status: Boolean
    dataBinder.apply {
      when {
        oldPassword.text.isNullOrEmpty() -> {
          oldPassword.error = resources.getString(R.string.required)
          status = false
        }
        newPassword.text.isNullOrEmpty() || newPassword.text.toString().length < 6 -> {
          newPassword.error = resources.getString(R.string.required)
          kuToast(resources.getString(R.string.please_enter_password_length_greater_than_6))
          status = false
        }
        confirmNewPass.text.isNullOrEmpty() || confirmNewPass.text.toString().length < 6 -> {
          confirmNewPass.error = resources.getString(R.string.required)
          kuToast(resources.getString(R.string.please_enter_password_length_greater_than_6))
          status = false
        }
        newPassword.text.toString() != confirmNewPass.text.toString() -> {
          confirmNewPass.error = resources.getString(R.string.required)
          kuToast(resources.getString(R.string.password_not_match))
          status = false
        }
        else ->   status = true
      }
    }
    return status
  }

  private fun requestChangePassword(tokenId: String, oldPass: String, newPass: String) {
    dataBinder.apply {
      val requestObject = ResetPasswordData(tokenId, oldPass, newPass,newPass)
      viewModel.resetPasswordRequest(requestObject).observe(viewLifecycleOwner) {
        when (it.status) {
          ServerCallBack.Status.ERROR -> {
            pbLogin.kuHide()
            errorMessage.kuShow()
            errorMessage.text = it.message
          }
          ServerCallBack.Status.SUCCESS -> {
            pbLogin.kuHide()
            kuToast(resources.getString(R.string.password_changed_msg))
            dismiss()
            (requireActivity() as OneSingleActivity).logOut()
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
    get() = R.layout.dialog_change_pass


}