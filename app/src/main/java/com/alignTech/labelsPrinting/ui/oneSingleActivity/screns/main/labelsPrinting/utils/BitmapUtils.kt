package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils

import android.graphics.*
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting

object BitmapUtils {


    fun Bitmap.createBitmap(label: LabelsPrinting, vertical: Boolean = false):Bitmap{
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
        kuInfoLog("jjjjjjjjjjjjj" , "width ==${width}")
        kuInfoLog("jjjjjjjjjjjjj" , "height ==${height}")
        kuInfoLog("jjjjjjjjjjjjj" , "height ==${height+190}")
        paint.textSize = height.toFloat()
        kuInfoLog("jjjjjjjjjjjjj" , "textSize ==${paint.textSize}")

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("Arial", Typeface.BOLD)
        label.barCode?.let { canvas.drawText(it, width / 2f, height+16f, paint) }
        label.nameProduct?.let { canvas.drawText(it, width / 2f, height+30f, paint) }
        label.price?.let { canvas.drawText(it.toString(), width / 2f, height+55f, paint) }
        print(bitmap)

       if (vertical) bitmap = bitmap.rotateBitmap(90f)

        return bitmap
    }

    fun Bitmap.rotateBitmap( angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

}