package com.alignTech.labelsPrinting.ui.dialog.loading.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogLoadingBinding

class DialogLoadingFragment : BaseDialogFragment<FragmentDialogLoadingBinding>() {

    private var message: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentDialogLoadingBinding) {
        dataBinder.apply {
            fragment = this@DialogLoadingFragment
            lifecycleOwner = this@DialogLoadingFragment
            status.text = message
        }
    }

    fun setStatusMessage(message: String) {
        this.message = message
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_loading


}