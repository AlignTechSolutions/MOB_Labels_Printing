package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.wizard.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.databinding.FragmentWizardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WizardFragment : BaseFragment<FragmentWizardBinding>() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        super.onCreateView(inflater, container, savedInstanceState)

        return  rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentWizardBinding) {
        dataBinder.apply {
            fragment = this@WizardFragment
            lifecycleOwner = this@WizardFragment
        }
    }

    override fun setUpViewModelStateObservers() { }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_wizard

}