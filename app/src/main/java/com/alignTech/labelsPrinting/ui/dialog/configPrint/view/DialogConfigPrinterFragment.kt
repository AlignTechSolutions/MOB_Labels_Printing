package com.alignTech.labelsPrinting.ui.dialog.configPrint.view

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.core.util.DateUtil.dashLongDateTimeFormat
import com.alignTech.labelsPrinting.core.util.DateUtil.formatDate
import com.alignTech.labelsPrinting.core.util.ExtensionsApp.kuGetCustomModel
import com.alignTech.labelsPrinting.databinding.FragmentDialogConfigPrinterBinding
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.DialogBluetoothDeviceChooseFragment
import com.alignTech.labelsPrinting.ui.dialog.configPrint.adapter.NameBarcodeFormatAutoCompleteAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BluetoothUtils
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.google.zxing.BarcodeFormat
import com.rt.printerlibrary.enumerate.BarcodeType
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DialogConfigPrinterFragment : BaseDialogFragment<FragmentDialogConfigPrinterBinding>(), BluetoothUtils.OnBluetoothUtilsListener {

    private val printConfig by lazy {
        kuGetCustomModel(appPreferences,  PrintConfig::class.java , "printConfig")
    }
    private val bluetoothDevice by lazy {
        kuGetCustomModel(appPreferences,  CustomBluetoothDevice::class.java , "CustomBluetoothDevice")
    }
    private lateinit var adapterNameBarcodeFormatAutoComplete: NameBarcodeFormatAutoCompleteAdapter

    private var barcodeFormatConfig : BarcodeFormatConfig? = null
    private var newPrintConfig : PrintConfig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        newPrintConfig = PrintConfig()
        setSearchNameBarcode(fetchBarcodeFormat())

        (activity as OneSingleActivity).bluetoothUtils.setOnBluetoothUtilsListener(this@DialogConfigPrinterFragment)


        return rootView
    }



    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_config_printer

    override fun onFragmentCreated(dataBinder: FragmentDialogConfigPrinterBinding) {
        dataBinder.apply {
            fragment = this@DialogConfigPrinterFragment
            lifecycleOwner = this@DialogConfigPrinterFragment

            printConfig?.let { config ->
                newPrintConfig = config
                if ((activity as OneSingleActivity).bluetoothUtils.isBluetoothConnected()){
                    config.namePrinter?.let {tvPName.text  = String.format(getString(R.string.p_name), it)  }
                    config.connectDate?.let {tvPConnectedDate.text = String.format(getString(R.string.p_date), formatDate(it , dashLongDateTimeFormat)) }
                    tvPConnectedDate.kuShow()
                    lyConnection.kuShow()
                }else{
                    tvPName.text  = String.format(getString(R.string.p_name), getString(R.string.tip_have_no_found_bluetooth_device))
                    tvPConnectedDate.kuHide()
                    lyConnection.kuHide()
                    tvPName.kuHide()
                }
                config.isVertical?.let {
                    if (it) rvVertical.apply { isActivated = true ; isChecked = true }
                    else rvHorizontal.apply { isActivated = true ; isChecked = true }
                }
                config.unitType.let {
                    if (it == TypedValue.COMPLEX_UNIT_MM) rbPMeasurementMM.apply { isActivated = true ; isChecked = true }
                    if (it == TypedValue.COMPLEX_UNIT_IN) rbPMeasurementIN.apply { isActivated = true ; isChecked = true }
                    else rbPMeasurementCm.apply { isActivated = true ; isChecked = true }
                }
                config.height.let { heightEt.setText(it.toString()) }
                config.width.let { widthEt.setText(it.toString()) }
                config.countPrint.let { countPrintEt.setText(it.toString()) }
                config.barcodeFormatConfig?.let {
                    barcodeFormatConfig = it
                    tvSearchNamePrint.setText(it.barcodeFormatName)
                }
            }


        }
    }

    fun dialogCancel() {
        dismiss()
    }

    private fun returnUnitType(): Int {
       dataBinder.apply {
           return when {
               rbPMeasurementCm.isChecked -> TypedValue.COMPLEX_UNIT_SHIFT
               rbPMeasurementMM.isChecked -> TypedValue.COMPLEX_UNIT_MM
               else -> TypedValue.COMPLEX_UNIT_IN
           }
       }
    }

    @SuppressLint("MissingPermission")
    fun saveConfig() {
        dataBinder.apply {
            when {
                heightEt.text.toString().toFloatOrNull() == null -> {
                    heightContainer.error = getString(R.string.required)
                    return
                }
                widthEt.text.toString().toFloatOrNull() == null -> {
                    widthContainer.error = getString(R.string.required)
                    return
                }
                barcodeFormatConfig == null -> {
                    tvSearchNamePrint.error = getString(R.string.required)
                    return
                }
                else -> newPrintConfig?.also {
                    it.isVertical = rvVertical.isChecked
                    it.unitType = returnUnitType()
                    it.height = heightEt.text.toString().toFloat()
                    it.width = widthEt.text.toString().toFloat()
                    it.countPrint = countPrintEt.text.toString().toIntOrNull() ?: 1
                    it.barcodeFormatConfig = barcodeFormatConfig
                    it.connectDate = Date()
                    it.connectDate.also { connectDate ->
                        if (connectDate == null) {
                            it.connectDate = Date()
                        }
                    }
                    it.namePrinter.also { namePrinter ->
                        if (namePrinter == null) {
                            it.namePrinter = bluetoothDevice?.macAddressPrinter
                        }
                    }
                    it.macAddressPrinter.also { macAddressPrinter ->
                        if (macAddressPrinter == null) {
                            it.macAddressPrinter = bluetoothDevice?.macAddressPrinter
                        }
                    }
                }
            }
        }
        appPreferences.setValue("printConfig", newPrintConfig)
        dismiss()
    }


    fun inflateConnectPrinterDialog(){
        val dialog = DialogBluetoothDeviceChooseFragment()
        dialog.initInterface(object : DialogBluetoothDeviceChooseFragment.OnBluetoothDeviceSelected {
            @SuppressLint("MissingPermission")
            override fun onBluetoothDeviceSelected(device: BluetoothDevice) {
                dataBinder.apply {
                    device.name?.let {tvPName.text  = String.format(getString(R.string.p_name), it)  }
                    Date().let {tvPConnectedDate.text = String.format(getString(R.string.p_date), formatDate(it , dashLongDateTimeFormat)) }
                }
                newPrintConfig?.let {
                    it.namePrinter = device.name
                    it.macAddressPrinter = device.address
                    it.connectDate = Date()
                }
            }
        })
        dialog.show(childFragmentManager ,dialog.tag )
    }


    private fun setSearchNameBarcode(list: ArrayList<BarcodeFormatConfig>) {
        list.let {
            adapterNameBarcodeFormatAutoComplete = NameBarcodeFormatAutoCompleteAdapter(requireContext(), R.layout.item_auto_complete, list )
            dataBinder.apply {

                lySearch.kuShow()

                tvSearchNamePrint.apply {

                    setAdapter(adapterNameBarcodeFormatAutoComplete)
                    setOnItemClickListener { parent, view, position, id ->
                        try {
                            adapterNameBarcodeFormatAutoComplete.getItem(position).let { item ->

                                tvSearchNamePrint.setText(item.barcodeFormatName)
                                kuHideSoftKeyboard()
                                barcodeFormatConfig = item
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // handle bluetooth config
    /************* showConnectDialog ****************/
    fun showBluetoothDeviceChooseDialog(){
        dataBinder.apply {
            (activity as OneSingleActivity).bluetoothUtils.showBluetoothDeviceChooseDialog({name ->
                tvPName.text = String.format(getString(R.string.p_name), name)
                val date = Date()
                date.let {tvPConnectedDate.text = String.format(getString(R.string.p_date), formatDate(it , dashLongDateTimeFormat)) }
                appPreferences.setValue("CustomBluetoothDevice" , CustomBluetoothDevice(macAddressPrinter = name, connectDate = date))
                tvPConnectedDate.kuShow()
                lyConnection.kuShow()
                tvPName.kuShow()
            },{tag ->
                tvPName.tag = tag
            },{ enable->
                btnConnect.isEnabled = !enable
                btnDisConnect.isEnabled = enable
            })
        }

    }

    /************* showConnectedListDialog ****************/
    fun showConnectedListDialog(){
        dataBinder.apply {
            (activity as OneSingleActivity).bluetoothUtils.showConnectedListDialog(
                {name ->
                    tvPName.text = String.format(getString(R.string.p_name), name)
                    val date = Date()
                    date.let {tvPConnectedDate.text = String.format(getString(R.string.p_date), formatDate(it , dashLongDateTimeFormat)) }
                    appPreferences.setValue("CustomBluetoothDevice" , CustomBluetoothDevice(macAddressPrinter = name, connectDate = date))                },{tag ->
                    tvPName.tag = tag
                    tvPConnectedDate.kuShow()
                    lyConnection.kuShow()
                    tvPName.kuShow()
                },{ enable->
                    btnConnect.isEnabled = !enable
                    btnDisConnect.isEnabled = enable
                })
        }

    }


    /************* showConnectedListDialog ****************/
    fun doConnect(){
        dataBinder.apply {
            (activity as OneSingleActivity).bluetoothUtils.doConnect {
                pbConnect.kuShow()
            }
        }

    }

    /************* showConnectedListDialog ****************/
    fun doDisConnect(){
        dataBinder.apply {
            (activity as OneSingleActivity).bluetoothUtils.doDisConnect({name ->
                tvPName.text = String.format(getString(R.string.p_name), name)
                tvPConnectedDate.kuHide()
                lyConnection.kuHide()
                tvPName.kuHide()
                appPreferences.setValue("CustomBluetoothDevice" , CustomBluetoothDevice()) }
                ,{tag ->
                tvPName.tag = tag
            },{ enable->
                btnConnect.isEnabled = !enable
                btnDisConnect.isEnabled = enable
            })
        }

    }

    override fun onBluetoothUtilsListener(name: String) {
        if (name == getString(R.string.tip_have_no_found_bluetooth_device)){
            dataBinder.tvPConnectedDate.kuHide()
            dataBinder.tvPName.kuHide()
            dataBinder.lyConnection.kuHide()
            return
        }
        dataBinder.tvPName.text = String.format(getString(R.string.p_name), name)
        val date = Date()
        date.let {dataBinder.tvPConnectedDate.text = String.format(getString(R.string.p_date), formatDate(it , dashLongDateTimeFormat)) }
        appPreferences.setValue("CustomBluetoothDevice" , CustomBluetoothDevice(macAddressPrinter = name, connectDate = date))
        dataBinder.tvPConnectedDate.kuShow()
        dataBinder.tvPName.kuShow()
        dataBinder.lyConnection.kuShow()
    }

    override fun onBluetoothUtilsListener(tag: Int) {
        dataBinder.tvPName.tag = tag
    }

    override fun onBluetoothUtilsListener(enable: Boolean) {
        dataBinder.apply {
            btnConnect.isEnabled = !enable
            btnDisConnect.isEnabled = enable
        }
    }

    override fun onBluetoothUtilsProgress(visibility: Boolean) {
        dataBinder.apply {
            if (visibility) pbConnect.kuShow() else pbConnect.kuHide()
        }
    }


    // handle bluetooth config

    private fun fetchBarcodeFormat():ArrayList<BarcodeFormatConfig> {
        val stringZxingFormat = arrayListOf(
            "CODABAR","CODE_39","CODE_93","CODE_128","ITF","QR_CODE","UPC_A","UPC_E","EAN_8", "EAN_13" , "AZTEC","DATA_MATRIX" , "PDF_417")

        val stringFormat = arrayListOf(
            "CODABAR","CODE39","CODE93","CODE128","ITF" ,"QR_CODE","UPC_A","UPC_E","EAN8", "EAN13", "EAN14" , "GS1")

        val list = arrayListOf<BarcodeFormatConfig>()
        stringZxingFormat.forEachIndexed { index, format ->
            list.add(BarcodeFormatConfig(barcodeFormatId = index, barcodeFormatName = format ,zxingBarcodeType = BarcodeFormat.valueOf(format) , BarcodeType.valueOf("CODABAR")))
        }
         return list

    }

    fun clearBarcode(){
        dataBinder.apply {
//            tvSearchNamePrint.setText("")
            tvSearchNamePrint.kuShowDropDown()
            kuTakeFocus(imgBtnClose)
        }
    }

}

data class PrintConfig(
    var namePrinter: String? = null,
    var macAddressPrinter: String? = null,
    var connectDate: Date? = null,
    var isVertical: Boolean? = null,
    var unitType: Int = TypedValue.COMPLEX_UNIT_MM, // cMeter
    var height: Float = 66.145833333F,
    var width: Float = 158.75F,
    var countPrint : Int = 1,
    var barcodeFormatConfig : BarcodeFormatConfig? = null,
)

data class CustomBluetoothDevice(
    var connectDate: Date? = null,
    var macAddressPrinter: String? = null,
)

data class BarcodeFormatConfig(
    var barcodeFormatId: Int? = null,
    var barcodeFormatName: String? = null,
    var zxingBarcodeType: BarcodeFormat? = null,
    var printerLibraryBarcodeType: BarcodeType? = null
)

