package com.alignTech.labelsPrinting.core.util

import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.AppConstants.IS_FIRST_TIME
import com.google.gson.Gson

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do : App Extensions to Files
 * Date 1/1/2021 - 4:59 PM
 */
object ExtensionsApp {

    /**
     * @param kuPreferences
     * @return FCMToken
     */
    fun fCMToken(kuPreferences: KUPreferences): String = kuPreferences.getStringValue(AppConstants.FCM_TOKEN)

    /**
     * @param kuPreferences
     * @return settingsConfig
     */
    fun <T>kuGetCustomModel(kuPreferences: KUPreferences ,  classOfT: Class<T>? , key : String): T? = Gson().fromJson(kuPreferences.getStringValue(key),classOfT)

    fun isFirstTime(appPreferences: KUPreferences):Boolean= appPreferences.getBooleanValue(IS_FIRST_TIME,true)

}
