package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.graphics.*
import android.util.Log
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PrintUtils : PrinterObserver {

    @BaseEnum.ConnectType
    private var checkedConType = BaseEnum.CON_BLUETOOTH
    @BaseEnum.CmdType
    private val currentCmdType = BaseEnum.CMD_ESC

    private var printerFactory: PrinterFactory? = null
    private var configObj: Any? = null
    private val printerInterfaceArrayList = ArrayList<PrinterInterface<*>>()
    private var curPrinterInterface: PrinterInterface<*>? = null
    private var iPrintTimes = 0

    private var mActivity: OneSingleActivity? = null

    private var txtDeviceSelected : String? = null
    private var txtDeviceSelectedTag : Int? = null

    private var bmpPrintWidth = 40


    var rtPrinterKotlin: RTPrinter<BluetoothEdrConfigBean>? = null
    var deviceSharedObject: BluetoothDevice? = null
    init {
        printerFactory = ThermalPrinterFactory()
        rtPrinterKotlin = printerFactory?.create() as RTPrinter<BluetoothEdrConfigBean>?
        rtPrinterKotlin?.setPrinterInterface(curPrinterInterface)
        PrinterObserverManager.getInstance().add(this)

    }

    fun initPrint(mActivity: OneSingleActivity){
        this.mActivity = mActivity
    }

    fun inflateConnectedListDialog() {
        val dialog = AlertDialog.Builder(mActivity)
        dialog.setTitle(mActivity?.getString(R.string.dialog_title_connected_devlist))
        val devList = arrayOfNulls<String>(printerInterfaceArrayList.size)
        for (i in devList.indices) {
            devList[i] = printerInterfaceArrayList[i].configObject.toString()
        }
        if (devList.isNotEmpty()) {
            dialog.setItems(devList) { _, i ->
                val printerInter = printerInterfaceArrayList[i]
                txtDeviceSelected = printerInter.configObject.toString()
                rtPrinterKotlin?.setPrinterInterface(printerInter) // Connection port settings
                txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
                curPrinterInterface = printerInter
                //  BaseApplication.getInstance().setRtPrinter(rtPrinter);//设置全局RTPrinter
                if (printerInter.connectState == ConnectStateEnum.Connected) {
                    mActivity?.kuToast("تم الإتصال بالطباعة")
                } else {
                    mActivity?.kuToast("غير متصل بالطباعة")
                }
            }
        } else {
            dialog.setMessage(R.string.pls_connect_printer_first)
        }
        dialog.setNegativeButton(R.string.dialog_cancel, null)
        dialog.show()
    }

    private fun doConnect() {
        if (txtDeviceSelectedTag == BaseEnum.NO_DEVICE) {
            mActivity?.showAlertDialog(mActivity?.getString(R.string.main_pls_choose_device))
            return
        }
        val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean
        iPrintTimes = 0
        connectBluetooth(bluetoothEdrConfigBean)
    }

    private fun connectBluetooth(bluetoothEdrConfigBean: BluetoothEdrConfigBean) {
        val piFactory: PIFactory = BluetoothFactory()
        val printerInterface = piFactory.create()
        printerInterface.configObject = bluetoothEdrConfigBean
        rtPrinterKotlin!!.setPrinterInterface(printerInterface)
        try {
            rtPrinterKotlin!!.connect(bluetoothEdrConfigBean)
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.let { Log.d("TAG", it) }
        } finally {
        }
    }

    private fun doDisConnect() {
        if (txtDeviceSelectedTag == BaseEnum.NO_DEVICE) {
            return
        }
        if (rtPrinterKotlin != null && rtPrinterKotlin!!.getPrinterInterface() != null) {
            rtPrinterKotlin!!.disConnect()
        }
        txtDeviceSelected = null
        txtDeviceSelectedTag = null
    }

    fun printLabel(device: String): Boolean {
        txtDeviceSelected = device
        configObj = BluetoothEdrConfigBean(deviceSharedObject)
        txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
        return isConfigPrintEnable(configObj as BluetoothEdrConfigBean)

    }

     fun showBluetoothDeviceChooseDialog(device: BluetoothDevice, label: LabelsPrinting) {
         configObj = BluetoothEdrConfigBean(device)
         txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
         isConfigPrintEnable(configObj as BluetoothEdrConfigBean)
         generate(label)
    }


    private fun isConfigPrintEnable(configObj: Any) :Boolean {
        val status: Boolean
        if (isInConnectList(configObj)) {
            status = true
            kuInfoLog("aaaaaaaaaaassssssssss","isConfigPrintEnable: $status")
            doConnect()
        } else{
            status = false
            doDisConnect()
            kuInfoLog("aaaaaaaaaaassssssssss","isConfigPrintEnable: $status")

        }
        return status
    }

    private fun isInConnectList(configObj: Any): Boolean {
        var isInList = false
        for (i in printerInterfaceArrayList.indices) {
            val printerInterface = printerInterfaceArrayList[i]
            if (configObj.toString() == printerInterface.configObject.toString()) {
                if (printerInterface.connectState == ConnectStateEnum.Connected) {
                    isInList = true
                    break
                }
            }
        }
        return isInList
    }

    fun generate(model : LabelsPrinting){
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(
                model.nameProduct,
                BarcodeFormat.CODE_128,
                242,
                71
            )
            val bitmap = Bitmap.createBitmap(242, 71, Bitmap.Config.RGB_565)
            for (i in 0 until 242) {
                for (j in 0 until 71) {
                    bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }

            convert(bitmap ,model)
        } catch (e: WriterException) {
            e.printStackTrace()
        }catch (e : SdkException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun convert(mBitmap: Bitmap, model: LabelsPrinting) {

        val width = 500
        val height = 220
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)


        val startX: Int = (canvas.width - mBitmap.width) / 2 //for horisontal position
        val startY: Int = (canvas.height - mBitmap.height) / 2 //for vertical position

        canvas.drawBitmap(mBitmap , startX.toFloat(), 0f, paint)

        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.textSize = 25f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("Arial", Typeface.BOLD)
        model.barCode?.let { canvas.drawText(it, width / 2f, 150f, paint) }
        model.nameProduct?.let { canvas.drawText(it, width / 2f, 180f, paint) }
        model.price?.let { canvas.drawText(it.toString(), width / 2f, 215f, paint) } // TODO covert to double
        print(bitmap)
    }

    @Throws(SdkException::class)
    private fun print(mBitmap : Bitmap?) {
        if (mBitmap == null) {
            mActivity?.kuToast(mActivity!!.getString(R.string.tip_upload_image))
            return
        }
        escPrint(mBitmap)
    }

    @Throws(SdkException::class)
    private fun escPrint(mBitmap : Bitmap) {
        val cmdFactory: CmdFactory = EscFactory()
        val cmd = cmdFactory.create()
        cmd.append(cmd.headerCmd)
        val commonSetting = CommonSetting()
        //  commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        cmd.append(cmd.getCommonSettingCmd(commonSetting))
        val bitmapSetting = BitmapSetting()

        bitmapSetting.bmpPrintMode = BmpPrintMode.MODE_SINGLE_COLOR

        bitmapSetting.bimtapLimitWidth = bmpPrintWidth * 8
        try {
            cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap))
        } catch (e: SdkException) {
            e.printStackTrace()
        }
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        if (rtPrinterKotlin != null) {
            rtPrinterKotlin?.writeMsg(cmd.appendCmds) //Sync Write
        }
    }



    private fun Activity.showAlertDialog(msg: String?) {
        runOnUiThread {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.dialog_tip)
            dialog.setMessage(msg)
            dialog.setNegativeButton(R.string.dialog_back, null)
            dialog.show()
        }
    }

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
       MainScope().launch {
           when (state) {
               CommonEnum.CONNECT_STATE_SUCCESS -> {
                   mActivity?.kuToast(printerInterface.configObject.toString() + mActivity?.getString(R.string._main_connected))
                   txtDeviceSelected = printerInterface.configObject.toString()
                   txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
                   curPrinterInterface = printerInterface //设置为当前连接， set current Printer Interface
                   printerInterfaceArrayList.add(printerInterface) //多连接-添加到已连接列表
                   rtPrinterKotlin!!.setPrinterInterface(printerInterface)
                   //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
                   doConnect()
               }
               CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                   if (printerInterface.configObject != null) {
                       mActivity?.kuToast(printerInterface.configObject.toString() + mActivity?.getString(R.string._main_disconnect))
                   } else {
                       mActivity?.kuToast( mActivity!!.getString(R.string._main_disconnect))
                   }
                   txtDeviceSelected = null
                   txtDeviceSelectedTag = null
                   curPrinterInterface = null
                   printerInterfaceArrayList.remove(printerInterface) //多连接-从已连接列表中移除
                   //  BaseApplication.getInstance().setRtPrinter(null);
                  doDisConnect()
               }
               else -> {}
           }
       }
    }

    override fun printerReadMsgCallback(printerInterface: PrinterInterface<*>?, bytes: ByteArray?) {
       MainScope().launch {
           val statusBean = PrinterStatusPareseUtils.parsePrinterStatusResult(bytes)
           if (statusBean.printStatusCmd == PrintStatusCmd.cmd_PrintFinish) {
               if (statusBean.blPrintSucc) {
                   mActivity?.kuToast("print ok")
               } else {
                   mActivity?.kuToast(PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
               }
           } else if (statusBean.printStatusCmd == PrintStatusCmd.cmd_Normal) {
               mActivity?.kuToast("print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))

           }
       }
    }



}