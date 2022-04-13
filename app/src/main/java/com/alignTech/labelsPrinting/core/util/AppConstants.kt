package com.alignTech.labelsPrinting.core.util

import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.BuildConfig


/**
 * Created by ( Eng Ali Al Fayed)
 * Class do : Constants of Values
 * Date 8/12/2020 - 5:19 PM
 */
object AppConstants {

    const val USER_SELECTED_COUNTRY = "USER_SELECTED_COUNTRY"

    const val API_VERSION = "v1"

    const val REQUEST_TIME_OUT: Long = 60
    const val PICK_FILE_RESULT_CODE = 0

    const val FCM_TOKEN = "FCM_TOKEN"
    const val IS_FIRST_TIME = "IS_FIRST_TIME"

    const val DOMAIN_URL: String = BuildConfig.SERVER_URL
    var BASE_URL: String = DOMAIN_URL


    fun loadBaseUrl(appPreferences: KUPreferences) { // new version 1.0.13.1-beta
        BASE_URL = appPreferences.getStringValue(BASE_URL_KEY , defaultValue = BASE_URL)
    }

    const val LOCATION_KEY = "LOCATION_KEY"
    const val VERSION_NAME = " " + BuildConfig.VERSION_NAME

    const val TOKEN = "token"
    const val JOB_ID = "job id"
    const val BASE_URL_KEY = "base_url"
    const val TOKEN_EXP_DATE: String = "TOKEN_EXP_DATE"
    const val SYNC_TIME: String = "sync time"


    var lag : String = "ar"
    const val NO_INTERNET = "Error connecting to host"
    const val NO_INTERNET_AR = "لا يوجد اتصال بالانترنت حاول مره اخرى."
    const val GENERAL_ERROR = "Something went wrong. please try again"
    const val GENERAL_ERROR_EN = "هناك خطأ ما. حاول مرة اخرى"
    const val GENERAL_ERROR_500 = "هناك خطأ ما فى قاعدة البيانات . حاول مرة اخرى"

}