package com.alignTech.labelsPrinting.core.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 3/26/2021 - 4:57 PM
 */
class ContextUtils (base: Context) : ContextWrapper(base) {
    companion object {

        fun wrap(c: Context, lang: String): ContextWrapper {
            var context = c
            val localeToSwitchTo = Locale(lang)
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                configuration.setLayoutDirection(localeToSwitchTo)
            } else {
                configuration.locale = localeToSwitchTo
                configuration.setLayoutDirection(localeToSwitchTo)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
                configuration.setLayoutDirection(localeToSwitchTo)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
                configuration.setLayoutDirection(localeToSwitchTo)
            }
            return ContextUtils(context)
        }

        fun updateLanguage(context: Context, locale: Locale){
            val config = context.resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            }
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}