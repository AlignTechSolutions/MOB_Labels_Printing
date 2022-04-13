package com.alignTech.labelsPrinting.domain.network.networkSettings


/*********
 * this is a callback back came from the server
 *
 * *********/
data class ServerCallBack<out T>(val status: Status, val data: T?, val message: String?, val isExist: Boolean? = null) {

  enum class Status {
    SUCCESS,
    ERROR,
    LOADING
  }

  companion object {
    fun <T> success(data: T?, message: String? = null): ServerCallBack<T> {
      return ServerCallBack(Status.SUCCESS, data, message)
    }

    fun <T> error(message: String,isExist:Boolean? = null, data: T? = null): ServerCallBack<T> {
      return ServerCallBack(Status.ERROR, data, message,isExist)
    }

    fun <T> loading(data: T? = null): ServerCallBack<T> {
      return ServerCallBack(Status.LOADING, data, null)
    }
  }
}