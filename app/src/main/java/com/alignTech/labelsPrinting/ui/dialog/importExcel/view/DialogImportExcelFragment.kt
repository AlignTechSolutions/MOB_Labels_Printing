package com.alignTech.labelsPrinting.ui.dialog.importExcel.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogImportExcelBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogImportExcelFragment : BaseDialogFragment<FragmentDialogImportExcelBinding>() {

    private var dialogCallBack : DialogCallBack? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return  rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentDialogImportExcelBinding) {
        dataBinder.apply {
            fragment = this@DialogImportExcelFragment
            lifecycleOwner = this@DialogImportExcelFragment
        }
    }

    fun initDialogCallBack(dialogCallBack : DialogCallBack){
        this.dialogCallBack = dialogCallBack
    }

    fun dialogCancel() {
        dismiss()
    }

    fun dialogImport() {
        dismiss()
        dialogCallBack?.dialogOnClick(dataBinder.btnImport.id)
    }


    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_import_excel


}