package com.alignTech.labelsPrinting.core.base.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alignTech.labelsPrinting.core.util.setWindowParams
import com.alignTech.labelsPrinting.core.util.setupFullHeight
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/13/2021 - 12:48 AM
 */
abstract class BaseSheetDialogFragment<T> : BottomSheetDialogFragment() where T: ViewDataBinding {

    @get:LayoutRes
    protected abstract val layoutResourceLayout : Int
    protected lateinit var dataBinder : T
    protected lateinit var rootView : View


    abstract fun onFragmentCreated(dataBinder : T)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setupFullHeight(requireActivity())
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this@BaseSheetDialogFragment.layoutResourceLayout.let {
            dataBinder = DataBindingUtil.inflate(inflater, it, container, false)
            this@BaseSheetDialogFragment.onFragmentCreated(dataBinder)
            rootView = dataBinder.root

            return rootView
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setWindowParams()
    }

    protected fun backFragment(){
        requireActivity().onBackPressed()
    }

    protected fun RecyclerView.initLinearLayoutManager(
        orientation: Int,
        rvAdapter: RecyclerView.Adapter<*>,
        manager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), orientation, false)
    ) {
        this.apply {
            layoutManager = manager
            setHasFixedSize(true)
            adapter = rvAdapter
        }
    }




}
