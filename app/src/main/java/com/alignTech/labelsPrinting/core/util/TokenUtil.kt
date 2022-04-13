package com.alignTech.labelsPrinting.core.util

import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.AppConstants.JOB_ID
import com.alignTech.labelsPrinting.core.util.AppConstants.SYNC_TIME
import com.alignTech.labelsPrinting.core.util.AppConstants.TOKEN
import com.alignTech.labelsPrinting.core.util.AppConstants.TOKEN_EXP_DATE
import com.alignTech.labelsPrinting.domain.model.auth.AuthDataItem
import java.util.*

object TokenUtil {

  private var token: String = ""

  fun loadTokenToMemory(appPreferences: KUPreferences) {// version 1.0.13.1-beta
    token = getToken(appPreferences)
  }

  fun getTokenFromMemory(): String {
    return token
  }

  fun getEmail( appPreferences: KUPreferences): String =
    getToken(appPreferences).let { JwtUtil.getJwtPayload(it)?.email.orEmpty() }


  private fun getExpDate(appPreferences: KUPreferences): Long {// version 1.0.13.1-beta
    return getToken(appPreferences).let {
      JwtUtil.getJwtPayload(it)?.exp!!.toLong()
    }
  }

  fun getCompanyId( appPreferences: KUPreferences): Int {
    return if (getToken(appPreferences).isNotEmpty()) {
      getToken(appPreferences).let {
        JwtUtil.getJwtPayload(it)?.CompanyId!!.toInt()
      }
    } else 0
  }

  fun getCompanyBranchId( appPreferences: KUPreferences): Int {
    return if (getToken(appPreferences).isNotEmpty()) {
      getToken(appPreferences).let {
        JwtUtil.getJwtPayload(it)?.CompanyBranchId!!.toInt()
      }
    } else 0
  }

  fun getSalesPersonId( appPreferences: KUPreferences): Int {
    return if (getToken(appPreferences).isNotEmpty()) {
      getToken(appPreferences).let {
        if (JwtUtil.getJwtPayload(it)?.ActiveSalesPersonId != ""){
          JwtUtil.getJwtPayload(it)?.ActiveSalesPersonId!!.toInt()
        }else{
          0
        }
      }
    } else 0
  }

  fun getEmployeeId( appPreferences: KUPreferences): Int {
    return if (getToken(appPreferences).isNotEmpty()) {
      getToken(appPreferences).let {
        JwtUtil.getJwtPayload(it)?.EmployeeId!!.toInt()
      }
    } else 0
  }

  fun getTokenId( appPreferences: KUPreferences): String {
    return if (getToken(appPreferences).isNotEmpty()) {
      getToken(appPreferences).let {
        JwtUtil.getJwtPayload(it)?.id!!
      }
    } else ""
  }

  fun getJobId(appPreferences: KUPreferences):String= appPreferences.getStringValue(JOB_ID,"")

  fun getToken(appPreferences: KUPreferences): String = // version 1.0.13.1-beta
    appPreferences.getStringValue(TOKEN, "")

  fun getStartTime(appPreferences: KUPreferences): Long {
    return appPreferences.getLongValue(SYNC_TIME, 0L)
  }

  fun saveToken(appPreferences: KUPreferences, token: AuthDataItem) { // version 1.0.13.1-beta
    appPreferences.setValue(TOKEN, token.token)
    appPreferences.setValue(JOB_ID, token.jobId)
    appPreferences.setValue(TOKEN_EXP_DATE, getExpDate(appPreferences))
    appPreferences.setValue(SYNC_TIME, System.currentTimeMillis())
    loadTokenToMemory(appPreferences)
  }

  fun isTokenExpired(appPreferences: KUPreferences): Boolean {
    loadTokenToMemory(appPreferences)
    return if (token.isEmpty()) true
    else DateUtil.isDateAfterByDateFormat(Date(), getExpDate(appPreferences).let { Date(it) })
  }

  fun removeToken(appPreferences: KUPreferences) {
    appPreferences.removeKey(TOKEN)
    appPreferences.removeKey(TOKEN_EXP_DATE)
    appPreferences.removeKey(JOB_ID)
    token = "" // remove token from memory
  }

  fun getRemainingTime(appPreferences: KUPreferences): Long {
    return run {
      val expireLong = getExpDate(appPreferences) * 1000
      val currentLong = System.currentTimeMillis()
      if (expireLong < currentLong || (expireLong.minus(currentLong) / (1000 * 60 * 60) % 24) > 8) 0L
      else expireLong.minus(currentLong)
    }
  }

  private const val TAG = "TokenUtil"

}