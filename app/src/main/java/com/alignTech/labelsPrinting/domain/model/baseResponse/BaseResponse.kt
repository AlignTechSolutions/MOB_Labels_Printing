package com.alignTech.labelsPrinting.domain.model.baseResponse

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
  @SerializedName("message") var message: String? = "",
  @SerializedName("errorsCount") var errorCount: Int? = 0,
  @SerializedName("isSuccess") var isSuccess: Boolean? = true,
  @SerializedName("isExists") var isExists: Boolean? = false,
  @SerializedName("errors") var errorsList: ArrayList<String>? = arrayListOf(),
  @SerializedName("data") var data: T?
)
