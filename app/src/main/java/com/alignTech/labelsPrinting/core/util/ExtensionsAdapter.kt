package com.alignTech.labelsPrinting.core.util

import android.widget.TextView
import androidx.databinding.BindingAdapter


/**
 * Created by ( Eng Ali Al Fayed)
 * Class do : Adapter Extensions
 * Date 1/1/2021 - 4:59 PM
 */

@BindingAdapter("app:bindString")
fun TextView.setBindString(txtString: String?) {
    if (txtString != null) text = txtString
}





