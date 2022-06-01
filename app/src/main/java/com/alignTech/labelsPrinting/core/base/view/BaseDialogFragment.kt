package com.alignTech.labelsPrinting.core.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alfayedoficial.kotlinutils.kuRes
import com.alfayedoficial.kotlinutils.kuSnackBarError
import com.alignTech.labelsPrinting.core.util.setWindowParams
import dagger.Module
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException

import javax.inject.Inject

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/13/2021 - 12:48 AM
 */
abstract class BaseDialogFragment<T> : DialogFragment() where T: ViewDataBinding {

    @get:LayoutRes
    protected abstract val layoutResourceLayout : Int

    private var _dataBinder : T? = null
    private var _rootView : View? = null
    private var _navController: NavController? = null

    protected val dataBinder : T
        get() = _dataBinder!!
    protected val navController: NavController
        get() = _navController!!
    protected val rootView : View
             get() = _rootView!!
    @Inject
    protected lateinit var  appPreferences: KUPreferences
    @Inject protected lateinit var  retrofit: Retrofit


    abstract fun onFragmentCreated(dataBinder : T)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this@BaseDialogFragment.layoutResourceLayout.let {
            _dataBinder = DataBindingUtil.inflate(inflater, it, container, false)
            this@BaseDialogFragment.onFragmentCreated(dataBinder)
            _rootView = dataBinder.root

            return rootView
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _navController = findNavController()
    }


    override fun onStart() {
        super.onStart()
        setWindowParams()
    }

    fun snackBarError(msg:String , bgColor : Int , tvColor : Int){
        kuSnackBarError(msg, kuRes.getColor(bgColor ,requireContext().theme), kuRes.getColor(tvColor ,requireContext().theme))
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



}
