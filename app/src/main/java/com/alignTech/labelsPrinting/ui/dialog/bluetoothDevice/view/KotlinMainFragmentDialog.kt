package com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfayedoficial.kotlinutils.kuErrorLog
import com.alfayedoficial.kotlinutils.kuHide
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.databinding.FragmentDialogKotlinMainBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BaseEnum
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.configObj
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.curPrinterInterface
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.printerFactory
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.printerInterfaceArrayList
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.rtPrinterKotlin
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.txtDeviceSelected
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.txtDeviceSelectedTag
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Font.COLOR_RED


const  val TAG = "TAG_TEST"
@AndroidEntryPoint
class KotlinMainFragmentDialog :BaseDialogFragment<FragmentDialogKotlinMainBinding>()  {

    private var label: LabelsPrinting? = null

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_kotlin_main

    override fun onFragmentCreated(dataBinder: FragmentDialogKotlinMainBinding) {
        dataBinder.apply {
            //printer
            fragment = this@KotlinMainFragmentDialog
            lifecycleOwner = this@KotlinMainFragmentDialog

            generate()

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }
    
    fun setLabelsPrinting(label: LabelsPrinting) {
        this.label = label
    }

    private fun generate(){
        dataBinder.apply {
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(
                    label?.barCode,
                    BarcodeFormat.CODE_128,
                    450,
                    110
                )
                val bitmap = Bitmap.createBitmap(450, 110, Bitmap.Config.RGB_565)
                for (i in 0 until 450) {
                    for (j in 0 until 110) {
                        bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                convert(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }catch (e : SdkException){
                e.printStackTrace()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun convert(mBitmap: Bitmap) {

        val width = 500
        val height = 250
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // x -> horizontal, y -> vertical

        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)


        val startX: Int = (canvas.width - mBitmap.width) / 2 //for horisontal position
        val startY: Int = (canvas.height - mBitmap.height) / 2 //for vertical position

        canvas.drawBitmap(mBitmap , startX.toFloat(), 0f, paint)
        drawStringRectangle(canvas ,10,10,width-10,height-10 , label?.barCode!!)
        drawStringRectangle(canvas ,10,35,width-10,height-10 , label?.nameProduct!!)
        drawStringRectangle(canvas ,10,70,width-10,height-10 , label?.price!!.toDouble().toString())



        dataBinder.mgBarCode2.setImageBitmap(bitmap)

        print(bitmap)
    }


    private fun drawStringRectangle(c: Canvas, topLeftX: Int, topLeftY: Int, width: Int, height: Int, textToDraw: String) {
        val mPaint = Paint()
        // height of 'Hello World'; height*0.7 looks good
        val fontHeight = (height * 0.7).toInt()
        mPaint.color = COLOR_RED.toInt()
        mPaint.style = Paint.Style.FILL
        c.drawRect(
            topLeftX.toFloat(), topLeftY.toFloat(),
            (topLeftX + width).toFloat(), (topLeftY + height).toFloat(), mPaint
        )
        mPaint.textSize = 25f
        mPaint.color = Color.BLACK
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.typeface = Typeface.create("Arial",Typeface.BOLD)
        val bounds = Rect()
        mPaint.getTextBounds(textToDraw, 0, textToDraw.length, bounds)
        c.drawText(textToDraw, (topLeftX + width / 2).toFloat(), (topLeftY + height / 2 + (bounds.bottom - bounds.top) / 2).toFloat(), mPaint)
    }

    @Throws(SdkException::class)
    private fun print(mBitmap : Bitmap?) {
        if (mBitmap == null) {
            kuToast(R.string.tip_upload_image)
            return
        }
        escPrint(mBitmap)
    }

    @Throws(SdkException::class)
    private fun escPrint(mBitmap : Bitmap) {
        Thread {
            val cmdFactory: CmdFactory = EscFactory()
            val cmd = cmdFactory.create()
            cmd.append(cmd.headerCmd)
            val commonSetting = CommonSetting()
            //  commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            cmd.append(cmd.getCommonSettingCmd(commonSetting))
            val bitmapSetting = BitmapSetting()

            bitmapSetting.bmpPrintMode = BmpPrintMode.MODE_SINGLE_COLOR

            bitmapSetting.bimtapLimitWidth = mBitmap.width
            // set bitmapSetting print in center of the paper
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
        }.start()

        MainScope().launch {
            delay(2000)
            dismiss()
        }



        //将指令保存到bin文件中，路径地址为sd卡根目录
//        final byte[] btToFile = cmd.getAppendCmds();
//        TonyUtils.createFileWithByte(btToFile, "Esc_imageCmd.bin");
//        TonyUtils.saveFile(FuncUtils.ByteArrToHex(btToFile), "Esc_imageHex");
    }


//    /************* showConnectedListDialog ****************/
//    fun showConnectedListDialog() {
//        val dialog = AlertDialog.Builder(requireContext())
//        dialog.setTitle(R.string.dialog_title_connected_devlist)
//        val devList = arrayOfNulls<String>(printerInterfaceArrayList.size)
//        for (i in devList.indices) {
//            devList[i] = printerInterfaceArrayList[i].configObject.toString()
//        }
//        if (devList.isNotEmpty()) {
//            dialog.setItems(devList) { _, i ->
//                val printerInter = printerInterfaceArrayList[i]
//                txtDeviceSelected = printerInter.configObject.toString()
//                rtPrinterKotlin?.setPrinterInterface(printerInter) // Connection port settings
//                txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
//                curPrinterInterface = printerInter
//                //  BaseApplication.getInstance().setRtPrinter(rtPrinter);//设置全局RTPrinter
//                if (printerInter.connectState == ConnectStateEnum.Connected) {
//                    setPrintEnable(true)
//                } else {
//                    setPrintEnable(false)
//                }
//            }
//        } else {
//            dialog.setMessage(R.string.pls_connect_printer_first)
//        }
//        dialog.setNegativeButton(R.string.dialog_cancel, null)
//        dialog.show()
//    }
//
//    /************* setPrintEnable ****************/
//    private fun setPrintEnable(isEnable: Boolean) {
//        dataBinder.apply {
//            btnConnect.isEnabled = !isEnable
//            btnDisConnect.isEnabled = isEnable
//        }
//    }
//

    //    /************* doConnect ****************/
//    fun doConnect() {
//        dataBinder.apply {
//            if (txtDeviceSelectedTag.toString().toInt() == BaseEnum.NO_DEVICE) { //未选择设备
//                kuToast(getString(R.string.main_pls_choose_device))
//                return
//            }
//            pbConnect.kuShow()
//            val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean
//            connectBluetooth(bluetoothEdrConfigBean)
//        }
//
//    }
//
//    /************* doDisConnect ****************/
//    fun doDisConnect() {
//        dataBinder.apply {
//            if (txtDeviceSelectedTag.toString().toInt() == BaseEnum.NO_DEVICE) { //未选择设备 n_click_repeatedly));
//                return
//            }
//            if (rtPrinterKotlin != null && rtPrinterKotlin!!.getPrinterInterface() != null) {
//                rtPrinterKotlin!!.disConnect()
//            }
////            txtDeviceSelected = getString(R.string.please_connect)
////            txtDeviceSelectedTag = BaseEnum.NO_DEVICE
//            setPrintEnable(false)
//        }
//
//    }
//
//    /************* showConnectDialog ****************/
//    @SuppressLint("MissingPermission")
//    fun showBluetoothDeviceChooseDialog() {
//        val bluetoothDeviceChooseDialog = BluetoothDeviceChooseDialog()
//        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener { device ->
//            dataBinder.apply {
//                if (TextUtils.isEmpty(device.name)) {
//                    txtDeviceSelected = device.address
//                } else {
//                    txtDeviceSelected = device.name + " [" + device.address + "]"
//                }
//                configObj = BluetoothEdrConfigBean(device)
//                txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
//                isConfigPrintEnable(configObj as BluetoothEdrConfigBean)
//            }
//
//        }
//        bluetoothDeviceChooseDialog.show(childFragmentManager, null)
//    }
//
//    private fun isConfigPrintEnable(configObj: Any) {
//        if (isInConnectList(configObj)) {
//            setPrintEnable(true)
//        } else {
//            setPrintEnable(false)
//        }
//    }
//
//    private fun isInConnectList(configObj: Any): Boolean {
//        var isInList = false
//        for (i in printerInterfaceArrayList.indices) {
//            val printerInterface = printerInterfaceArrayList[i]
//            if (configObj.toString() == printerInterface.configObject.toString()) {
//                if (printerInterface.connectState == ConnectStateEnum.Connected) {
//                    isInList = true
//                    break
//                }
//            }
//        }
//        return isInList
//    }




}