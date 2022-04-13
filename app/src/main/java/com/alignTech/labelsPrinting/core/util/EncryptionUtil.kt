package com.alignTech.labelsPrinting.core.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

object EncryptionUtil {

    fun decodeFromBase64(strEncoded: String): String {
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, Charsets.UTF_8)
    }

    fun decodeBitmapToBase64(bitmap: Bitmap?): String? {
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

}