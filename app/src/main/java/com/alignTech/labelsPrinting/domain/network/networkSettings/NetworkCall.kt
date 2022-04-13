package com.alignTech.labelsPrinting.domain.network.networkSettings

import androidx.lifecycle.MutableLiveData
import com.alignTech.labelsPrinting.core.util.AppConstants.GENERAL_ERROR
import com.alignTech.labelsPrinting.core.util.AppConstants.GENERAL_ERROR_EN
import com.alignTech.labelsPrinting.core.util.AppConstants.NO_INTERNET
import com.alignTech.labelsPrinting.core.util.AppConstants.NO_INTERNET_AR
import com.alignTech.labelsPrinting.core.util.AppConstants.lag
import com.alignTech.labelsPrinting.domain.model.auth.AuthDataItem
import com.alignTech.labelsPrinting.domain.model.baseResponse.BaseResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException

object NetworkCall {

  fun <T> makeCall(requestFun: suspend () -> Response<BaseResponse<T>>): MutableLiveData<ServerCallBack<T>> {
    val result = MutableLiveData<ServerCallBack<T>>()
    //this is for showing loading on the screen
    result.value = ServerCallBack.loading(null)
    //this is for making the call inside CoroutineScope
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val response = requestFun()
        withContext(Dispatchers.Main) {
          if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
              result.value = ServerCallBack.success(it.data, it.message)
              return@withContext
            }
          } else {
            if (response.errorBody() != null) {
              val type = object : TypeToken<BaseResponse<T>>() {}.type
              val error = Gson().fromJson<BaseResponse<T>>(response.errorBody()!!.charStream(), type)
              result.value = ServerCallBack.error(
                error.errorsList?.joinToString(",") ?: GENERAL_ERROR,
                error.isExists == true
              )
            } else result.value = ServerCallBack.error(GENERAL_ERROR)
          }
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
          if (e is ConnectException || e is UnknownHostException) {
            //this is no internet exception
            result.value =
              ServerCallBack.error(if (lag == "en") NO_INTERNET else NO_INTERNET_AR)
          } else
            result.value =
              ServerCallBack.error(if (lag == "en") GENERAL_ERROR else GENERAL_ERROR_EN)
        }
      }
    }
    return result
  }

  fun loginCall(requestFun: suspend () -> Response<AuthDataItem>): MutableLiveData<ServerCallBack<AuthDataItem>> {
    val result = MutableLiveData<ServerCallBack<AuthDataItem>>()
    //this is for showing loading on the screen
    result.value = ServerCallBack.loading(null)
    //this is for making the call inside CoroutineScope
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val response = requestFun()
        withContext(Dispatchers.Main) {
          if (response.isSuccessful && response.body() != null) {
            response.let {
              result.value = ServerCallBack.success(it.body())
              return@withContext
            }
          } else {
            if (response.errorBody() != null) {
              val type = object : TypeToken<AuthDataItem>() {}.type
              val error = Gson().fromJson<AuthDataItem>(response.errorBody()!!.charStream(), type)
              result.value = ServerCallBack.error(error.errors!!.joinToString(","))
            } else
              result.value = ServerCallBack.error(GENERAL_ERROR)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
          if (e is ConnectException || e is UnknownHostException) {
            //this is no internet exception
            result.value = ServerCallBack.error(NO_INTERNET)
          } else
            result.value = ServerCallBack.error(GENERAL_ERROR)
        }
      }
    }
    return result
  }



}