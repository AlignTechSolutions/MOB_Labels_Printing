package com.alignTech.labelsPrinting.ui.dialog.resetDatabase.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alfayedoficial.kotlinutils.kuRes
import com.alfayedoficial.kotlinutils.kuSnackBarError
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogResetDatabaseBinding
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class DialogResetDatabaseFragment : BaseDialogFragment<FragmentDialogResetDatabaseBinding>() {

    private var drawerLayout: DrawerLayout? = null
    private val activityViewModel : OneSingleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentDialogResetDatabaseBinding) {
        dataBinder.apply {
            fragment = this@DialogResetDatabaseFragment
            lifecycleOwner = this@DialogResetDatabaseFragment
        }
    }

    fun yes(){
        runBlocking {
            val job = launch { activityViewModel.clearData() }
            job.join()
            drawerLayout?.closeDrawer(GravityCompat.START)
            dismiss()
            activityViewModel.getAllLabelsPrinting()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                kuSnackBarError("تم إعادة تعيين قاعدة البيانات بنجاح", kuRes.getColor(R.color.TemplateGreen ,requireActivity().theme), kuRes.getColor(R.color.white ,requireActivity().theme))
            }else{
                kuSnackBarError("تم إعادة تعيين قاعدة البيانات بنجاح", kuRes.getColor(R.color.TemplateGreen), kuRes.getColor(R.color.white))
            }
        }

    }
    fun no(){
        drawerLayout?.closeDrawer(GravityCompat.START)
        dismiss()
    }

    fun init(drawerLayout: DrawerLayout) {
       this.drawerLayout = drawerLayout
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_reset_database
}