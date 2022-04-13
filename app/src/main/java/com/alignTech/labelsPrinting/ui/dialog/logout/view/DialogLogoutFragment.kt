package com.alignTech.labelsPrinting.ui.dialog.logout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfayedoficial.kotlinutils.kuHide
import com.alfayedoficial.kotlinutils.kuRes
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogLogoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogLogoutFragment : BaseDialogFragment<FragmentDialogLogoutBinding>() {

    private var dialogCallBack : DialogCallBack? = null
    private var message :String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentDialogLogoutBinding) {
        dataBinder.apply {
            fragment = this@DialogLogoutFragment
            lifecycleOwner = this@DialogLogoutFragment

            if (message != null){
                btnCancel.kuHide()
                imgBtnClose.kuHide()
                isCancelable = false
                textView.text = message
                btnLogout.text = kuRes.getString(R.string.yes)
            }
        }
    }

    fun initDialogCallBack(dialogCallBack : DialogCallBack){
        this.dialogCallBack = dialogCallBack
    }

    fun dialogCancel() {
        dismiss()
    }

    fun initMessage(message :String?){
        this.message = message
    }

    fun dialogLogout() {
        dismiss()
        dialogCallBack?.dialogOnClick(dataBinder.btnLogout.id)
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_logout

}