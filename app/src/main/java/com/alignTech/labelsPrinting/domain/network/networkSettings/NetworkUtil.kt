package com.alignTech.labelsPrinting.domain.network.networkSettings

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData

object NetworkUtil {

  val networkConnectivityLiveData: MutableLiveData<Boolean> = MutableLiveData(true)
  const val PATH = "PATH"
  const val PATH2 = "PATH2"

  @SuppressLint("MissingPermission")
  @RequiresApi(Build.VERSION_CODES.N)
  fun setNetworkListener(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

      override fun onAvailable(network: Network) = networkConnectivityLiveData.postValue(true)

      override fun onLost(network: Network) = networkConnectivityLiveData.postValue(false)
    })
  }

  enum class NetworkLinks(val type: String) {

    /**-----------------------   GET   ---------------------*/
    CompanyInfo("/api/v1/CompanyBranches/GetBranchProfile/$PATH"),

    /**-----------------------   POST   ---------------------*/
    Login("/api/v1/identity/login"),
    CompanyConfiguration("/api/v1/identity/CompanyConfiguration"),
    ResetPassword("/api/v1/identity/ChangePassword"),
    Logout("/api/v1/identity/Logout"),
  }

  fun getApiLink(domain: String, endPoint: String, path: String? = null) = domain.plus(endPoint).replace(PATH, path ?: "")
}