package com.alignTech.labelsPrinting.core.base.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import com.alfayedoficial.kotlinutils.KUConstants
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.ContextUtils
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.ui.dialog.loading.view.DialogLoadingFragment
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/7/2021 - 9:25 PM
 */
abstract class BaseActivity<T> : AppCompatActivity() where T : ViewDataBinding {

    @get:LayoutRes
    protected abstract val layoutResourceId : Int
    @Inject
    protected lateinit var  appPreferences: KUPreferences
    protected lateinit var dataBinder: T
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController

    val loadingProgress: DialogLoadingFragment by lazy {
        DialogLoadingFragment().apply {
            setStatusMessage(this@BaseActivity.getString(R.string.please_wait))
            isCancelable = false
        }
    }

    val loadingProgress2: DialogLoadingFragment by lazy {
        DialogLoadingFragment().apply {
            setStatusMessage(this@BaseActivity.getString(R.string.please_wait))
            isCancelable = false
        }
    }

    abstract fun onActivityCreated(dataBinder : T)

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        this@BaseActivity.initial()
        updateLanguage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this@BaseActivity.initial()
//        setTransparentStatusWithWhiteIcons(R.color.purple_500)
        setUpViewModelStateObservers()
    }

    private fun initial(){
        this@BaseActivity.layoutResourceId.let {
            val dataBinder = DataBindingUtil.setContentView<T>(this@BaseActivity , it)
            this.dataBinder = dataBinder
            this@BaseActivity.onActivityCreated(dataBinder)
        }
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 1) {
            fm.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun <T>parseError(retrofit : Retrofit, response: ResponseBody, nameClass: Class<*>): T {
        val converter: Converter<ResponseBody?, T> =
            retrofit
                .responseBodyConverter(nameClass, arrayOfNulls<Annotation>(0))
        val errorModel: T = try {
            converter.convert(response)!!
        } catch (e: IOException) {
            return nameClass.newInstance() as T
        }
        return errorModel
    }

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences(newBase.getString(R.string.app_name) + "1", Context.MODE_PRIVATE)
        val lang = sharedPref.getString(KUConstants.KU_LOCALE, "ar")!!
        super.attachBaseContext(ContextUtils.wrap(newBase , lang))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLanguage()
    }

    private fun updateLanguage(){
        val lang = appPreferences.getStringValue(KUConstants.KU_LOCALE, "ar")
        ContextUtils.updateLanguage(this, Locale(lang))
    }

    open fun setUpViewModelStateObservers(){}



}
