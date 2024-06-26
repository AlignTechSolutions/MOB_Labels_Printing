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
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alfayedoficial.kotlinutils.kuRes
import com.alfayedoficial.kotlinutils.kuSnackBarError
import com.alignTech.labelsPrinting.core.util.setWindowParams
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
    protected lateinit var dataBinder : T
    protected lateinit var navController: NavController
    protected lateinit var rootView : View
    @Inject
    protected lateinit var  appPreferences: KUPreferences
    @Inject protected lateinit var  retrofit: Retrofit


    abstract fun onFragmentCreated(dataBinder : T)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this@BaseDialogFragment.layoutResourceLayout.let {
            dataBinder = DataBindingUtil.inflate(inflater, it, container, false)
            this@BaseDialogFragment.onFragmentCreated(dataBinder)
            rootView = dataBinder.root

            return rootView
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
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
