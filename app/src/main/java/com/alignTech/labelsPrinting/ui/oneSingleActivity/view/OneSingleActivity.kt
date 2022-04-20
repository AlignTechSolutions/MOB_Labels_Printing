package com.alignTech.labelsPrinting.ui.oneSingleActivity.view

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.R.id
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.alignTech.labelsPrinting.core.util.PermissionUtil
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.databinding.ActivityOneSingleBinding
import com.alignTech.labelsPrinting.domain.model.auth.LogoutData
import com.alignTech.labelsPrinting.domain.network.networkSettings.ServerCallBack
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.BluetoothDeviceChooseDialog
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.TAG
import com.alignTech.labelsPrinting.ui.dialog.changePassword.view.ChangePasswordDialog
import com.alignTech.labelsPrinting.ui.dialog.logout.view.DialogLogoutFragment
import com.alignTech.labelsPrinting.ui.dialog.resetDatabase.view.DialogResetDatabaseFragment
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BaseEnum
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import com.anggrayudi.storage.SimpleStorageHelper
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException
import java.lang.reflect.InvocationTargetException

@AndroidEntryPoint
class OneSingleActivity : BaseActivity<ActivityOneSingleBinding>() , DialogCallBack ,  PermissionUtil.PermissionListener ,
    PrinterObserver {

    val activityViewModel : OneSingleViewModel by viewModels()
    private val storageHelper = SimpleStorageHelper(this)
    private var closeStatus : Boolean = false

    private val permissionsList = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.BLUETOOTH_PRIVILEGED,
        Manifest.permission.BLUETOOTH,
    ).also {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             Manifest.permission.BLUETOOTH_CONNECT ;  Manifest.permission.BLUETOOTH_SCAN
        }
    }

    fun checkPermissions() {
        activityViewModel.apply {
            currentRequestCode = PERMISSIONS_REQUEST_CODE
            PermissionUtil.checkPermission(this@OneSingleActivity , permissionsList , PERMISSIONS_REQUEST_CODE ,this@OneSingleActivity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(activityViewModel.currentRequestCode, requestCode, permissions, grantResults, this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Call database and get all labels */
        activityViewModel.getAllLabelsPrinting()

        storageHelper.onFileSelected = { requestCode, files ->
            // do stuff
            val contentResolver = applicationContext.contentResolver
            if (files.isNotEmpty()){
                contentResolver.openInputStream(files[0].uri)?.use { inputStream ->
                    try {
                        val workBook = WorkbookFactory.create(inputStream)
                        activityViewModel.workbookMutableLiveData.value = workBook
                    }catch (ex : InvocationTargetException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex : FileFormatException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex : NoSuchFileException){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }catch (ex: Exception){
                        print(ex.message)
                        activityViewModel.workbookMutableLiveData.value = null
                    }finally {
                        print("finally Exception")
                        activityViewModel.workbookMutableLiveData.value = null
                    }
                }
            }

            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }

        }
    }

    override val layoutResourceId: Int
        get() = R.layout.activity_one_single

    override fun onActivityCreated(dataBinder: ActivityOneSingleBinding) {
       dataBinder.apply {
           activity = this@OneSingleActivity
           lifecycleOwner = this@OneSingleActivity

           val navHostFragment = supportFragmentManager.findFragmentById(id.navGraph) as NavHostFragment
           navController = navHostFragment.navController

           appBarConfiguration = AppBarConfiguration(topLevelDestinationIds = setOf(id.labelsPrintingFragment), drawerLayout ) // include your drawer_layout
           drawNavigationView.setupWithNavController(navController)

           drawNavigationView.setNavigationItemSelectedListener { item ->
               when (item.itemId) {
                   id.reset -> {
                       drawerLayout.closeDrawer(GravityCompat.START)
                       inflateResetDatabaseDialog()
                   }
                   id.labelsPrintingFragment -> {
                       drawerLayout.closeDrawer(GravityCompat.START)
                       navController.navigate(id.labelsPrintingFragment)
                   }
                   id.changePassword ->{
                       drawerLayout.closeDrawer(GravityCompat.START)
                       inflateChangePasswordDialog()
                   }
                   id.connectPrinter ->{
                       drawerLayout.closeDrawer(GravityCompat.START)
                       showBluetoothDeviceChooseDialog()
                   }


               }
               true
           }


       }
    }

    fun startSessionCounter() {
        if (!activityViewModel.startCountDown()) inflateLogOutDialog(kuRes.getString(R.string.session_is_finished))
        else setCounterDownText()
    }

    private fun setCounterDownText() {
        dataBinder.apply {
            try {
                val remainingTextView = drawNavigationView.getHeaderView(0).findViewById<TextView>(R.id.remain_time)
                activityViewModel.remainingTime.observe(this@OneSingleActivity) {
                    if (it == "00:00:00") {
                        remainingTextView.setTextColor(ContextCompat.getColor(this@OneSingleActivity, R.color.TemplateRed))
                        inflateLogOutDialog(null)
                    }
                    remainingTextView.text = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun inflateResetDatabaseDialog(){
        val dialog = DialogResetDatabaseFragment()
        dialog.init(dataBinder.drawerLayout )
        dialog.show(supportFragmentManager ,dialog.tag )
    }

    private fun inflateChangePasswordDialog(){
        val dialog = ChangePasswordDialog()
        dialog.show(supportFragmentManager, dialog.tag)
    }

    fun inflateLogOutDialog(message :String?){
        val dialog = DialogLogoutFragment()
        dialog.initDialogCallBack(this)
        dialog.initMessage(message)
        dialog.show(supportFragmentManager ,dialog.tag )
    }


    /************* showConnectDialog ****************/
    @SuppressLint("MissingPermission")
    fun showBluetoothDeviceChooseDialog() {
        val bluetoothDeviceChooseDialog = BluetoothDeviceChooseDialog()
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener { device ->
            dataBinder.apply {
                txtDeviceSelected = if (TextUtils.isEmpty(device.name)) {
                    device.address
                } else {
                    device.name + " [" + device.address + "]"
                }
                staticDevice = device
                configObj = BluetoothEdrConfigBean(staticDevice)
                txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
                isConfigPrintEnable(configObj as BluetoothEdrConfigBean)
                connectBluetooth(configObj as BluetoothEdrConfigBean)
            }

        }
        bluetoothDeviceChooseDialog.show(supportFragmentManager, null)
    }

    private fun isConfigPrintEnable(configObj: Any) {
        printEnable = isInConnectList(configObj)
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
            e.message?.let { Log.d(TAG, it) }
        } finally {
        }

    }


    fun snackBarError(msg:String , bgColor : Int , tvColor : Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            kuSnackBarError(msg, kuRes.getColor(bgColor ,theme), kuRes.getColor(tvColor ,theme))
        }else{
            kuSnackBarError(msg, kuRes.getColor(bgColor), kuRes.getColor(tvColor))
        }
    }

    fun loadingProgress(status : Boolean){
        if (status){
            loadingProgress.show(supportFragmentManager, loadingProgress.tag)
        }else{
            loadingProgress.dismiss()
        }
    }

    fun logOut() {
        val logoutData = LogoutData(TokenUtil.getTokenId(appPreferences), TokenUtil.getJobId(appPreferences))
        loadingProgress(true)
        activityViewModel.requestLogout(logoutData).observe(this) {
            if (it.status == ServerCallBack.Status.SUCCESS) {
                TokenUtil.removeToken(appPreferences)
                navController.navigate(R.id.authFragment)
            } else if (it.status == ServerCallBack.Status.ERROR) {
                kuToast(kuRes.getString(R.string.network_connection_failed))
            }
            loadingProgress(false)
        }
    }

    fun openFilePicker() =  storageHelper.openFilePicker()

    override fun dialogOnClick(idView: Int) {
        when (idView) {
            R.id.btnLogout -> {
                logOut()
            }
        }
    }

    companion object {

        private const val PERMISSIONS_REQUEST_CODE = 5

        var rtPrinterKotlin: RTPrinter<BluetoothEdrConfigBean>? = null
        var printerFactory: PrinterFactory? = null
        var configObj: Any? = null
        val printerInterfaceArrayList = ArrayList<PrinterInterface<*>>()
        var curPrinterInterface: PrinterInterface<*>? = null
        var txtDeviceSelected: String? = null
        var txtDeviceSelectedTag: Int? = null
        var staticDevice: BluetoothDevice? = null
        var printEnable: Boolean? = null

        init {
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
            )
        }
    }

    override fun onBackPressed() {
        val destination = navController.currentDestination
        when (destination?.id) {
            R.id.labelsPrintingFragment ->{
                if (closeStatus){
                    finish()
                }else{
                    kuToast("Please press back again")
                    closeStatus = true
                    kuRunDelayed(2500) { closeStatus = false }
                }
            }
            else ->{ super.onBackPressed()}
        }

    }

    override fun permissionGranted() {}

    override fun permissionDenied() {}

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
        when (state) {
            CommonEnum.CONNECT_STATE_SUCCESS -> {
                kuErrorLog("RT连接end：", System.currentTimeMillis().toString())
                kuToast(printerInterface.configObject.toString() + getString(R.string._main_connected))
                txtDeviceSelected = printerInterface.configObject.toString()
                txtDeviceSelectedTag = BaseEnum.HAS_DEVICE
                curPrinterInterface = printerInterface //设置为当前连接， set current Printer Interface
                printerInterfaceArrayList.add(printerInterface) //多连接-添加到已连接列表
                rtPrinterKotlin!!.setPrinterInterface(printerInterface)
                //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
//                        setPrintEnable(true)
            }
            CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                if (printerInterface.configObject != null) {
                    kuToast(printerInterface.configObject.toString() + getString(R.string._main_disconnect))
                } else {
                    kuToast(getString(R.string._main_disconnect))
                }
//                        kuErrorLog("RT连接断开：", System.currentTimeMillis().toString())
//                        tvDeviceSelected.setText(R.string.please_connect)
//                        txtDeviceSelectedTag = BaseEnum.NO_DEVICE
//                        curPrinterInterface = null
//                        printerInterfaceArrayList.remove(printerInterface) //多连接-从已连接列表中移除
                //  BaseApplication.getInstance().setRtPrinter(null);
//                        setPrintEnable(false)
            }
            else -> {}
        }
    }

    override fun printerReadMsgCallback(printerInterface: PrinterInterface<*>?, bytes: ByteArray?) {
        MainScope().launch {
            val statusBean = PrinterStatusPareseUtils.parsePrinterStatusResult(bytes)
            if (statusBean.printStatusCmd == PrintStatusCmd.cmd_PrintFinish) {
                if (statusBean.blPrintSucc) {
                    kuToast("print ok")
                } else {
                    kuToast("print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
                }
            } else if (statusBean.printStatusCmd == PrintStatusCmd.cmd_Normal) {
                kuToast("print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
            }
        }
    }



}