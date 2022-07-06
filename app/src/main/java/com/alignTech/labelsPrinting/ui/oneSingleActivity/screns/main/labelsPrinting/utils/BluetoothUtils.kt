package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.BluetoothDeviceChooseDialog
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.bean.Position
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.*
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BarcodeSetting
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.setting.TextSetting
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "PrintUtils"
class BluetoothUtils @Inject constructor(private val appPreferences: KUPreferences) : PrinterObserver {

    private var tvDeviceSelected: String? = null
    private var tvDeviceSelectedTag: Int = BaseEnum.NO_DEVICE
    private var printEnable: Boolean? = null
    private var printerFactory: PrinterFactory? = null
    private var configObj: Any? = null
    private val printerInterfaceArrayList = ArrayList<PrinterInterface<*>>()
    private var rtPrinterKotlin: RTPrinter<BluetoothEdrConfigBean>? = null
    private var curPrinterInterface: PrinterInterface<*>? = null
    private var iPrintTimes = 0

    @BaseEnum.ConnectType
    private var checkedConType = BaseEnum.CON_BLUETOOTH
    @BaseEnum.CmdType
    private val currentCmdType = BaseEnum.CMD_ESC

    private var mActivity :  BaseActivity<*>? = null
    private var onBluetoothUtilsListener : OnBluetoothUtilsListener? = null

    interface OnBluetoothUtilsListener {
        fun onBluetoothUtilsListener(name : String)
        fun onBluetoothUtilsListener(tag : Int)
        fun onBluetoothUtilsListener(enable : Boolean)
        fun onBluetoothUtilsProgress(visibility : Boolean)
    }

    fun setActivity(activity: BaseActivity<*>){
        mActivity = activity
    }

    fun setOnBluetoothUtilsListener(onBluetoothUtilsListener: OnBluetoothUtilsListener){
        this.onBluetoothUtilsListener = onBluetoothUtilsListener
    }

    private fun Activity.showAlertDialog(msg: String?) {
        MainScope().launch {
            val dialog = AlertDialog.Builder(this@showAlertDialog)
            dialog.setTitle(R.string.dialog_tip)
            dialog.setMessage(msg)
            dialog.setNegativeButton(R.string.dialog_back, null)
            dialog.show()
        }
    }

    init {
        printerFactory = ThermalPrinterFactory()
        rtPrinterKotlin = printerFactory?.create() as RTPrinter<BluetoothEdrConfigBean>?
        rtPrinterKotlin?.setPrinterInterface(curPrinterInterface)
        PrinterObserverManager.getInstance().add(this@BluetoothUtils)
    }

    fun isBluetoothConnected(): Boolean {
        return tvDeviceSelectedTag != BaseEnum.NO_DEVICE
    }

    @SuppressLint("MissingPermission")
    fun showBluetoothDeviceChooseDialog(name :(String?) -> Unit, tag :(Int?) -> Unit, enable:(Boolean) -> Unit) {
        val bluetoothDeviceChooseDialog = BluetoothDeviceChooseDialog()
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener { device ->
            tvDeviceSelected = if (TextUtils.isEmpty(device.name)) {
                device.address
            } else {
                device.name + " [" + device.address + "]"
            }
            configObj = BluetoothEdrConfigBean(device)
            tvDeviceSelectedTag = BaseEnum.HAS_DEVICE
            printEnable = isInConnectList(configObj as BluetoothEdrConfigBean)
            name(tvDeviceSelected!!)
            tag(tvDeviceSelectedTag)
            enable(printEnable!!)
        }
        bluetoothDeviceChooseDialog.show(mActivity!!.supportFragmentManager, bluetoothDeviceChooseDialog.tag)
    }

    fun showConnectedListDialog(name :(String?) -> Unit, tag :(Int?) -> Unit, enable:(Boolean) -> Unit){
        val dialog = AlertDialog.Builder(mActivity!!)
        dialog.setTitle(R.string.dialog_title_connected_devlist)
        val devList = arrayOfNulls<String>(printerInterfaceArrayList.size)
        for (i in devList.indices) {
            devList[i] = printerInterfaceArrayList[i].configObject.toString()
        }
        if (devList.isNotEmpty()) {
            dialog.setItems(devList) { _, i ->
                val printerInter = printerInterfaceArrayList[i]
                tvDeviceSelected = printerInter.configObject.toString()
                name(tvDeviceSelected!!)
                rtPrinterKotlin?.setPrinterInterface(printerInter) // Connection port settings
                tvDeviceSelectedTag = BaseEnum.HAS_DEVICE
                tag(tvDeviceSelectedTag)
                curPrinterInterface = printerInter
                //  BaseApplication.getInstance().setRtPrinter(rtPrinter);//设置全局RTPrinter
                if (printerInter.connectState == ConnectStateEnum.Connected) {
                    enable(true)
                } else {
                    enable(false)
                }
            }
        } else {
            dialog.setMessage(R.string.pls_connect_printer_first)
        }
        dialog.setNegativeButton(R.string.dialog_cancel, null)
        dialog.show()
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

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
       MainScope().launch {
           onBluetoothUtilsListener?.onBluetoothUtilsProgress(false)
           when (state) {
               CommonEnum.CONNECT_STATE_SUCCESS -> {
                   mActivity!!.kuToast(printerInterface.configObject.toString() + mActivity!!.getString(R.string._main_connected))
                   tvDeviceSelected = printerInterface.configObject.toString()
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(tvDeviceSelected!!)
                   tvDeviceSelectedTag = BaseEnum.HAS_DEVICE
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(tvDeviceSelectedTag)
                   curPrinterInterface = printerInterface //设置为当前连接， set current Printer Interface
                   printerInterfaceArrayList.add(printerInterface) //多连接-添加到已连接列表
                   rtPrinterKotlin!!.setPrinterInterface(printerInterface)
                   //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(true)

               }
               CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                   if (printerInterface.configObject != null) {
                       mActivity!!.kuToast(printerInterface.configObject.toString() +mActivity!!.getString(R.string._main_disconnect))
                   } else {
                       mActivity!!.kuToast(mActivity!!.getString(R.string._main_disconnect))
                   }
                   tvDeviceSelected = mActivity!!.getString(R.string.tip_have_no_found_bluetooth_device)
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(tvDeviceSelected!!)
                   tvDeviceSelectedTag= BaseEnum.NO_DEVICE
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(tvDeviceSelectedTag)
                   curPrinterInterface = null
                   printerInterfaceArrayList.remove(printerInterface) //多连接-从已连接列表中移除
                   //  BaseApplication.getInstance().setRtPrinter(null);
                   onBluetoothUtilsListener?.onBluetoothUtilsListener(false)
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
                    mActivity!!.kuToast("print ok")
                } else {
                    mActivity!!.kuToast(PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
                }
            } else if (statusBean.printStatusCmd == PrintStatusCmd.cmd_Normal) {
                mActivity!!.kuToast("print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
            }
        }
    }

    /************* doConnect ****************/
    fun doConnect(visibility : (Boolean) -> Unit) {
        if (tvDeviceSelectedTag == BaseEnum.NO_DEVICE) {
            mActivity!!.showAlertDialog(mActivity!!.getString(R.string.main_pls_choose_device))
            return
        }
        visibility(true)
        val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean
        iPrintTimes = 0
        connectBluetooth(bluetoothEdrConfigBean)
    }

    /************* connectBluetooth ****************/
    private fun connectBluetooth(bluetoothEdrConfigBean: BluetoothEdrConfigBean) {
        val piFactory: PIFactory = BluetoothFactory()
        val printerInterface = piFactory.create()
        printerInterface.configObject = bluetoothEdrConfigBean
        rtPrinterKotlin!!.setPrinterInterface(printerInterface)
        try {
            rtPrinterKotlin!!.connect(bluetoothEdrConfigBean)
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.let { Log.i(TAG, it) }
        } finally {
        }
    }

    /************* doDisConnect ****************/
    fun doDisConnect(name :(String?) -> Unit, tag :(Int?) -> Unit, enable:(Boolean) -> Unit) {
        if (tvDeviceSelectedTag == BaseEnum.NO_DEVICE) return
        if (rtPrinterKotlin != null && rtPrinterKotlin!!.getPrinterInterface() != null) {
            rtPrinterKotlin!!.disConnect()
        }
        tvDeviceSelected = mActivity!!.getString(R.string.tip_have_no_found_bluetooth_device)
        tvDeviceSelectedTag = BaseEnum.NO_DEVICE
        name(tvDeviceSelected)
        tag(tvDeviceSelectedTag)
        enable(false)
    }

    /************* printEscCommand ****************/
    fun printEscCommand(mBitmap : Bitmap ,vertical: Boolean? = false, result : (ResultOfPrint) -> Unit) {
        MainScope().launch {

            val bitmapSetting = BitmapSetting()
            bitmapSetting.bmpPrintMode = BmpPrintMode.MODE_SINGLE_COLOR

            val cmd = EscFactory().create().apply {

                append(getCpclHeaderCmd(10, 15, 1, 0))
                append(getPrintCopies(1))


                try {
                    append(getBitmapCmd(bitmapSetting, mBitmap)) // bitmap
                } catch (e: SdkException) {
                    e.printStackTrace()
                    return@launch
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@launch
                }
            }

//            //getCpclHeaderCmd(int pageHigh, int pageWidth, int printCopies, int offset)
//            cmd.getCpclHeaderCmd(20, 35, 2, 0)
////            cmd.getPageArea(0 , 0 , 100,100)
//            cmd.getPrintCopies(2)
////            cmd.getSetAreaWidth(30)


           // bitmapSetting.printPostion = Position(20, 20)
            //bitmapSetting.bitmapLimitWidth = mBitmap.width
            // set center




//            if (vertical == true){
//                cmd.append(cmd.lfcrCmd)
//                cmd.append(cmd.lfcrCmd)
//                cmd.append(cmd.lfcrCmd)
//                cmd.append(cmd.lfcrCmd)
//            }else{
//                cmd.append(cmd.lfcrCmd)
//            }


            try {
                if (rtPrinterKotlin != null|| cmd.appendCmds != null  || cmd.appendCmds.isNotEmpty()) {
                    rtPrinterKotlin!!.writeMsg(cmd.appendCmds) //Sync Write
                    result(ResultOfPrint(true, "تمت الطباعة"))
                }else{
                    result(ResultOfPrint(false, "تمت الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
                }
            } catch (e: SdkException) {
                e.printStackTrace()
                result(ResultOfPrint(false, "تمت الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
            } catch (e: Exception) {
                e.printStackTrace()
                result(ResultOfPrint(false, "لم تتم- الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
            }

        }
    }

    /************* printEscCommand ****************/
    fun printEscCommand(barcodeContent : String, barcodeType: BarcodeType? = null, label: LabelsPrinting, result : (ResultOfPrint) -> Unit) {
        MainScope().launch {
            val cmd = EscFactory().create()
            cmd.append(cmd.headerCmd) // header

            cmd.chartsetName = "UTF-8"

            val barcodeSetting = BarcodeSetting()
            barcodeSetting.barcodeStringPosition = BarcodeStringPosition.BELOW_BARCODE
            barcodeSetting.heightInDot = 72 //accept value:1~255
            barcodeSetting.barcodeWidth = 3 //accept value:2~6
            barcodeSetting.qrcodeDotSize = 5 //accept value: Esc(1~15), Tsc(1~10)
            barcodeSetting.escBarcodFont = ESCBarcodeFontTypeEnum.BARFONT_B_9x17

            val textSetting = TextSetting()
            textSetting.apply {
                escFontType = ESCFontTypeEnum.FONT_A_12x24
                isEscSmallCharactor = SettingEnum.Enable
                isAntiWhite = SettingEnum.Disable
                doubleWidth = SettingEnum.Enable
                doubleHeight = SettingEnum.Enable
                bold = SettingEnum.Enable
                underline = SettingEnum.Disable
                align = CommonEnum.ALIGN_MIDDLE
            }
            val commonSetting = CommonSetting()
            textSetting.txtPrintPosition = Position(0, 0)

            commonSetting.escLineSpacing = 1
            cmd.append(cmd.getCommonSettingCmd(commonSetting))

            try {
//                cmd.append(cmd.getBarcodeCmd(barcodeType, barcodeSetting , barcodeContent)) // barcodeContent
                cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE93, barcodeSetting , barcodeContent)) // barcodeContent

                cmd.append(cmd.getTextCmd(textSetting , label.nameProduct))
                cmd.append(cmd.lfcrCmd)
                cmd.append(cmd.getTextCmd(textSetting , label.price.toString()))
            } catch (e: SdkException) {
                e.printStackTrace()
                return@launch
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            }
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
//            cmd.append(cmd.lfcrCmd)
//            cmd.append(cmd.lfcrCmd)
//            cmd.append(cmd.lfcrCmd)
//            cmd.append(cmd.lfcrCmd)

            try {
                if (rtPrinterKotlin != null|| cmd.appendCmds != null  || cmd.appendCmds.isNotEmpty()) {
                    rtPrinterKotlin!!.writeMsg(cmd.appendCmds) //Sync Write
                    result(ResultOfPrint(true, "تمت الطباعة"))
                }else{
                    result(ResultOfPrint(false, "تمت الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
                }
            } catch (e: SdkException) {
                e.printStackTrace()
                result(ResultOfPrint(false, "تمت الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
            } catch (e: Exception) {
                e.printStackTrace()
                result(ResultOfPrint(false, "لم تتم- الطباعة , لا يوجد بيانات للطباعة او الطباعة غير متصلة"))
            }

        }
    }


    data class ResultOfPrint(val result: Boolean, val msg: String)


    /************* printEscCommand ****************/

    fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics): Float {
        return when (unit) {
            TypedValue.COMPLEX_UNIT_PX -> value
            TypedValue.COMPLEX_UNIT_DIP -> value * metrics.density
            TypedValue.COMPLEX_UNIT_SP -> value * metrics.scaledDensity
            TypedValue.COMPLEX_UNIT_PT -> value * metrics.xdpi * (1.0f / 72)
            TypedValue.COMPLEX_UNIT_IN -> value * metrics.xdpi
            TypedValue.COMPLEX_UNIT_MM -> value * metrics.xdpi * (1.0f / 25.4f)
            else -> 0f
        }
    }

    private fun convertCmToPx(cm: Float, metrics: DisplayMetrics): Int {
        return (cm * metrics.xdpi * (1.0f / 2.54f)).toInt()
    }

    private fun convertMmToPx(mm: Float, metrics: DisplayMetrics): Int {
        return (mm * metrics.xdpi * (1.0f / 25.4f)).toInt()
    }

    private fun convertInToPx(inch: Float, metrics: DisplayMetrics):Int{
        return (inch * metrics.xdpi).toInt()
    }

    fun convertToPx(unit: Int, value: Float, metrics: DisplayMetrics):Int{
        return when(unit){
            3 -> convertMmToPx(value, metrics)
            2 -> convertInToPx(value, metrics)
            else -> convertCmToPx(value, metrics)
        }
    }




}