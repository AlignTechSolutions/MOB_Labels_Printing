package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils

import android.graphics.*
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting

object BitmapUtils {


    fun Bitmap.createBitmap(label: LabelsPrinting, vertical: Boolean = false):Bitmap? = try{
        var bitmap = Bitmap.createBitmap(width, height+190, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)

        val startX: Int = (canvas.width - width) / 2 //for horizontal position
        val startY: Int = (canvas.height - height) / 2 //for vertical position

        // set barcode image
        canvas.drawBitmap(this , startX.toFloat(), 0f, paint)

        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.textSize = 24f

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("Arial", Typeface.BOLD)
        paint.letterSpacing = 0.7f
        label.barCode?.let { canvas.drawText(it, canvas.width / 2f, (this.height+20).toFloat(), paint) }
        paint.letterSpacing = 0f
        label.nameProduct?.let { canvas.drawText(it,canvas.width / 2f, (this.height+45f), paint) }
        label.price?.let { canvas.drawText("السعر : $it", canvas.width / 2f, (this.height+50f)+20f, paint) }
        print(bitmap)

        if (vertical) bitmap = bitmap.rotateBitmap(90f)
        bitmap
    }catch (e:Exception){
        null
    }

    fun Bitmap.rotateBitmap( angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

}