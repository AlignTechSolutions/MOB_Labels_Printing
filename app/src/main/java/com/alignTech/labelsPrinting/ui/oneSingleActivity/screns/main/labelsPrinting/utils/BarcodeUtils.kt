package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.TAG
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BarcodeUtils {

    private var staticDevice: BluetoothDevice? = null

    fun getBluetoothNameDevice(macAddressPrinter: String?): BluetoothDevice {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddressPrinter)
    }

    fun setStaticDevice(device: BluetoothDevice?) {
        staticDevice = device
    }

    fun generateBarcodeWriter(text: String, width: Int, height: Int, format: BarcodeFormat): Bitmap? = try {
            val matrix = MultiFormatWriter().encode(text, format, width, height)
            matrix.toBitmap()
        } catch (e: Exception) {
            kuInfoLog("generateBarcode", e.message.toString())
            e.printStackTrace()
            null
        }

    fun generateEAN13Barcode(text: String, width: Int, height: Int): Bitmap? = try {
            val matrix = MultiFormatWriter().encode(text, BarcodeFormat.EAN_13, width, height)
            matrix.toBitmap()
        } catch (e: Exception) {
            kuInfoLog("generateEAN13Barcode", e.message.toString())
            e.printStackTrace()
            null
        }


    private fun BitMatrix.toBitmap() : Bitmap?{
        val height: Int = height
        val width: Int = width
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

}