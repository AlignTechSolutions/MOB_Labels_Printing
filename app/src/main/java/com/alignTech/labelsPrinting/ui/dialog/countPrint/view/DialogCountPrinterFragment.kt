package com.alignTech.labelsPrinting.ui.dialog.countPrint.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogCountPrinterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogCountPrinterFragment : BaseDialogFragment<FragmentDialogCountPrinterBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }


    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_count_printer

    override fun onFragmentCreated(dataBinder: FragmentDialogCountPrinterBinding) {
       dataBinder.apply {
           fragment = this@DialogCountPrinterFragment
           lifecycleOwner = this@DialogCountPrinterFragment
       }
    }

    fun setCount(count: Int , newCount : (count: Int) -> Unit){
       dataBinder.apply {
           countPrintEt.setText(count.toString())

           onClickCountPrint.setOnClickListener {
               newCount(countPrintEt.text.toString().toIntOrNull()?:1)
               dismiss()
           }
       }
    }

}