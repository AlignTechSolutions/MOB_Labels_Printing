package com.alignTech.labelsPrinting.ui.dialog.addNewRow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.SaveDBCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.setNumberNotAcceptMinus
import com.alignTech.labelsPrinting.databinding.FragmentDialogAddNewRowToDbBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewRowToDbFragmentDialog : BaseDialogFragment<FragmentDialogAddNewRowToDbBinding>() {

    private lateinit var saveDBCallBack : SaveDBCallBack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_add_new_row_to_db

    override fun onFragmentCreated(dataBinder: FragmentDialogAddNewRowToDbBinding) {
        dataBinder.apply {
            fragment = this@AddNewRowToDbFragmentDialog
            lifecycleOwner = this@AddNewRowToDbFragmentDialog
            price.setNumberNotAcceptMinus()
        }
    }

    fun setOnSaveItemClickListener(saveDBCallBack: SaveDBCallBack) {
        this.saveDBCallBack = saveDBCallBack
    }

    fun saveDB() {
        dataBinder.apply {

            val barCode = code.text.toString()
            val nameProduct = nameProduct.text.toString()
            val price = price.text.toString()

            when {
                barCode.isEmpty() ||  nameProduct.isEmpty()  || price.isEmpty() -> {
                    inCode.error = "حقل مطلوب !"
                    inNameProduct.error = "حقل مطلوب !"
                    inNumber.error = "حقل مطلوب !"
                }
                else -> {
                    LabelsPrinting().apply {
                        this.barCode = barCode
                        this.nameProduct = nameProduct
                        this.price = price.toDouble()
                    }.let {
                        saveDBCallBack.saveNewLabelsPrinting(it)
                    }

                    dismiss()
                }
            }
        }
    }
}