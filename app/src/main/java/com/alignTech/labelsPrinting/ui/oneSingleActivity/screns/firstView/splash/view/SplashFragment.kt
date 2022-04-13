package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.splash.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfayedoficial.kotlinutils.kuRunDelayed
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.ExtensionsApp.isFirstTime
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.databinding.FragmentSplashBinding
import com.daimajia.androidanimations.library.Techniques
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return rootView
    }

    override fun onFragmentCreated(dataBinder: FragmentSplashBinding) {
        dataBinder.apply {
            fragment = this@SplashFragment
            lifecycleOwner = this@SplashFragment
        }
    }

    override fun setUpViewModelStateObservers() {}

    override fun onResume() {
        super.onResume()
        dataBinder.lyParent.setAnimation(Techniques.FadeIn, duration = 2700)
        kuRunDelayed(3100) {
            if (isFirstTime(appPreferences))
                if (TokenUtil.isTokenExpired(appPreferences)) {
                    navController.navigate(R.id.action_splashFragment_to_authFragment)
                } else {
                    navController.navigate(R.id.action_splashFragment_to_labelsPrintingFragment)
                }
            else
                navController.navigate(R.id.action_splashFragment_to_wizardFragment)
        }
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_splash

}