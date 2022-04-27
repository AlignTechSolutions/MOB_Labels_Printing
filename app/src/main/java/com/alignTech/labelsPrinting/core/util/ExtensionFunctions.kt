package com.alignTech.labelsPrinting.core.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.ui.setupWithNavController
import com.alfayedoficial.kotlinutils.KUAutoComplete
import com.alignTech.labelsPrinting.BuildConfig
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 2/6/2022 - 11:59 AM
 */

fun DialogFragment.setWindowParams() {
    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog?.window?.setLayout(
        ConstraintLayout.LayoutParams.MATCH_PARENT,
        ConstraintLayout.LayoutParams.MATCH_PARENT
    )
}

fun Dialog.setupFullHeight(context: Context){
    try {
        setOnShowListener{ dialogInterface: DialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(context,bottomSheetDialog)
        }
    } catch (e:Exception) {
        print(e.message)
    }
}

private fun setupFullHeight(context : Context,bottomSheetDialog: BottomSheetDialog) {
    val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
    if (bottomSheet != null) {
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        val windowHeight = getWindowHeight(context)
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = true
    }
}

private fun getWindowHeight(context: Context): Int {
    val displayMetrics = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display?.apply { getRealMetrics(displayMetrics) }
    } else (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun TextInputLayout.markRequiredInRed() {
    hint = buildSpannedString {
        append(hint)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun EditText.markRequiredInRed() {
    hint = buildSpannedString {
        append(hint)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun KUAutoComplete.markRequiredInRed() {
    hint = buildSpannedString {
        append(hint)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun TextView.markRequiredInRed() {
    text = buildSpannedString {
        append(text)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun MaterialButton.kuSetColor(@ColorRes color : Int){
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
        setTextColor(context.resources.getColor(color , context.theme))
    }else{
        setTextColor(context.resources.getColor(color))
    }
}



fun String.kuIsDoubleNumber(): Boolean =  try {
    this.toDouble()
    true
} catch (n: NumberFormatException) {
    false
} catch (e: Exception) {
    false
}

fun EditText.setNumberNotAcceptMinus(vararg validation : String) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(text: Editable?) {
            var number: String
            validation.forEach {
                if (text?.contains(it)!!) {
                    number = text.toString().replace(it, "")
                    setText(number)
                }
            }
        }
    })
}

fun EditText.checkEmptyEditText(): Boolean {
    return this.text.isEmpty()
}

fun validateMobileVersion(version: Int): Boolean = BuildConfig.VERSION_NAME.replace(".", "").toInt() < version

fun Fragment.showAlertDialog(context: Context, message: String, positiveFunction: (() -> Unit)? = null) {
    try{
        AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.warning))
            .setMessage(message)
            .setCancelable(false)
            .setIcon(R.drawable.ic_baseline_warning_24)
            .setPositiveButton(resources.getString(R.string.proceed)) { dialog, _ ->
                dialog.dismiss()
                positiveFunction?.let { it() }
            }.show()
    }catch (e:Exception){
        Log.i("TAG", "showAlertDialog: ${e.message}")
    }
}

fun Fragment.setBaseActivityFragmentsToolbar(title: String, toolbar: Toolbar, textView: TextView) {
    ( activity as BaseActivity<*>).apply {
        setHasOptionsMenu(true)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.setupWithNavController(navController, appBarConfiguration)
        textView.text = title
    }
}

fun Activity.setBaseActivityFragmentsToolbar(title: String, toolbar: Toolbar, textView: TextView) {
    ( this as BaseActivity<*>).apply {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        textView.text = title
    }
}



