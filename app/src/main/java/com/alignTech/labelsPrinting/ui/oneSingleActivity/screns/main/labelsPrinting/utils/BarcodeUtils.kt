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
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import com.google.zxing.pdf417.PDF417Writer
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

    fun generateBarcode(barcodeTxt: String, format: BarcodeFormat): Bitmap? = try {
        val bitmap = when(format){
            BarcodeFormat.CODABAR ->{generateCodaBarCode(barcodeTxt)}
            BarcodeFormat.CODE_39 ->{ generateCode39Code(barcodeTxt) }
            BarcodeFormat.CODE_93 ->{generateCode93Code(barcodeTxt)}
            BarcodeFormat.CODE_128 ->{generateCode128Code(barcodeTxt)}
            BarcodeFormat.ITF ->{ generateITFCode(barcodeTxt) }
            BarcodeFormat.QR_CODE ->{ generateQRCode(barcodeTxt) }
            BarcodeFormat.UPC_A ->{ generateUPCACode(barcodeTxt) }
            BarcodeFormat.UPC_E ->{ generateUPCECode(barcodeTxt) }
            BarcodeFormat.EAN_8 ->{ generateEAN8Barcode(barcodeTxt) }
            BarcodeFormat.EAN_13 ->{ generateEAN13Barcode(barcodeTxt) }
            BarcodeFormat.AZTEC ->{ generateAZTECBarcode(barcodeTxt) }
            BarcodeFormat.DATA_MATRIX ->{ generateDataMatrixBarcode(barcodeTxt) }
            BarcodeFormat.PDF_417 ->{ generatePDF417Barcode(barcodeTxt) }
            else ->{null}
        }
        bitmap
    }catch (e: Exception) {
        kuInfoLog("generateBarcode", e.message.toString())
        e.printStackTrace()
        null
    }


    fun generateCodaBarCode(text: String): Bitmap? = try {
        val matrix = CodaBarWriter().encode(text, BarcodeFormat.CODABAR, 400, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateCode39Code(text: String): Bitmap? = try {
        val matrix = Code39Writer().encode(text, BarcodeFormat.CODE_39, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateCode93Code(text: String): Bitmap? = try {
        val matrix = Code93Writer().encode(text, BarcodeFormat.CODE_93, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateCode128Code(text: String): Bitmap? = try {
        val matrix = Code128Writer().encode(text, BarcodeFormat.CODE_128, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateITFCode(text: String): Bitmap? = try {
        val matrix = ITFWriter().encode(text, BarcodeFormat.ITF, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateQRCode(text: String): Bitmap? = try {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 900, 250)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateUPCACode(text: String): Bitmap? = try {
        val matrix = UPCAWriter().encode(text, BarcodeFormat.UPC_A, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateUPCECode(text: String): Bitmap? = try {
        val matrix = UPCEWriter().encode(text, BarcodeFormat.UPC_E, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateEAN8Barcode(text: String): Bitmap? = try {
        val matrix = EAN8Writer().encode(text, BarcodeFormat.EAN_8, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateEAN13Barcode(text: String): Bitmap? = try {
            val matrix = EAN13Writer().encode(text, BarcodeFormat.EAN_13, 900, 170)
            matrix.toBitmap()
        } catch (e: Exception) {
            kuInfoLog("generateEAN13Barcode", e.message.toString())
            e.printStackTrace()
            null
        }

    fun generateAZTECBarcode(text: String): Bitmap? = try {
        val matrix = AztecWriter().encode(text, BarcodeFormat.AZTEC, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generateDataMatrixBarcode(text: String): Bitmap? = try {
        val matrix = DataMatrixWriter().encode(text, BarcodeFormat.DATA_MATRIX, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }

    fun generatePDF417Barcode(text: String): Bitmap? = try {
        val matrix = PDF417Writer().encode(text, BarcodeFormat.PDF_417, 900, 170)
        matrix.toBitmap()
    } catch (e: Exception) {
        kuInfoLog("generateEAN13Barcode", e.message.toString())
        e.printStackTrace()
        null
    }


    fun generateEAN8Barcode(text: String, width: Int, height: Int): Bitmap? = try {
        val matrix = EAN8Writer().encode(text, BarcodeFormat.EAN_8, width, height)
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