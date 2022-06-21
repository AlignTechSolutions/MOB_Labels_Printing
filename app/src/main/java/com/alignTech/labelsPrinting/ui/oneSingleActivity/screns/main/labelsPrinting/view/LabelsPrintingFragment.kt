package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.callback.OnDelete
import com.alignTech.labelsPrinting.callback.SaveDBCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.ExtensionsApp
import com.alignTech.labelsPrinting.core.util.setBaseActivityFragmentsToolbar
import com.alignTech.labelsPrinting.databinding.FragmentLabelsPrintingBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.dialog.addNewRow.view.AddNewRowToDbFragmentDialog
import com.alignTech.labelsPrinting.ui.dialog.configPrint.view.PrintConfig
import com.alignTech.labelsPrinting.ui.dialog.countPrint.view.DialogCountPrinterFragment
import com.alignTech.labelsPrinting.ui.dialog.importExcel.view.DialogImportExcelFragment
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.LabelsPrintingTableRvAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.NameProductAutoCompleteAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BarcodeUtils.generateBarcode
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BarcodeUtils.generateBarcodeWriter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BarcodeUtils.generateCode39Code
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BarcodeUtils.generateQRCode
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BitmapUtils.createBitmap
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.utils.BluetoothUtils
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.viewModel.LabelsPrintingViewModel
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.curPrinterInterface
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.printerFactory
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.rtPrinterKotlin
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.inflateConfigPrinterDialogWithPermissionCheck
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.printer.RTPrinter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import permissions.dispatcher.*
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class LabelsPrintingFragment : BaseFragment<FragmentLabelsPrintingBinding>() , DialogCallBack ,
    LabelsPrintingTableRvAdapter.RecyclerviewPosition  , OnDelete ,
    BluetoothUtils.OnBluetoothUtilsListener  {

    private val activityViewModel : OneSingleViewModel by activityViewModels()
    private val mViewModel : LabelsPrintingViewModel by viewModels()
    private val labelsPrintingModels: ArrayList<LabelsPrinting> = arrayListOf()
    private val adapterLabelsPrintingTableRv by lazy { LabelsPrintingTableRvAdapter() }
    private lateinit var adapterNameProductAutoComplete: NameProductAutoCompleteAdapter
    private var statusInsert = true

    @Inject
    lateinit var bluetoothUtils : BluetoothUtils

    // handle bluetooth config
    /************* showConnectDialog ****************/
    fun showBluetoothDeviceChooseDialog(){
        dataBinder.apply {
            bluetoothUtils.showBluetoothDeviceChooseDialog({name ->
                tvDeviceSelected.text = name
            },{tag ->
                tvDeviceSelected.tag = tag
            },{ enable->
                btnConnect.isEnabled = !enable
                btnDisConnect.isEnabled = enable
            })
        }

    }

    /************* showConnectedListDialog ****************/
    fun showConnectedListDialog(){
        dataBinder.apply {
            bluetoothUtils.showConnectedListDialog(
                {name ->
                    tvDeviceSelected.text = name
                },{tag ->
                    tvDeviceSelected.tag = tag
                },{ enable->
                    btnConnect.isEnabled = !enable
                    btnDisConnect.isEnabled = enable
                })
        }

    }

    /************* showConnectedListDialog ****************/
    fun doConnect(){
        dataBinder.apply {
            bluetoothUtils.doConnect {
                pbConnect.kuShow()
            }
        }

    }

    /************* showConnectedListDialog ****************/
    fun doDisConnect(){
        dataBinder.apply {
            bluetoothUtils.doDisConnect({name ->
                tvDeviceSelected.text = name
            },{tag ->
                tvDeviceSelected.tag = tag
            },{ enable->
                btnConnect.isEnabled = !enable
                btnDisConnect.isEnabled = enable
            })
        }

    }

    // handle bluetooth config

    private val applicationSettingsScreen = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            inflateImportBarcodeDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View{
        super.onCreateView(inflater, container, savedInstanceState)


        return rootView
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_labels_printing

    override fun onFragmentCreated(dataBinder: FragmentLabelsPrintingBinding) {
        dataBinder.apply {
            fragment = this@LabelsPrintingFragment
            lifecycleOwner = this@LabelsPrintingFragment


            initRecyclerView()

            toolbar.apply {
                setBaseActivityFragmentsToolbar(kuRes.getString(R.string.app_name), baseToolbar, tvToolbar)
            }

            swipeRefreshLayout.setOnRefreshListener {
                /* Call database and get all labels */
                activityViewModel.getAllLabelsPrinting()
            }


            vAImport.setOnClickListener {
                inflateImportBarcodeDialogWithPermissionCheck()
            }

            bluetoothUtils.setActivity(activity as OneSingleActivity)
            bluetoothUtils.setOnBluetoothUtilsListener(this@LabelsPrintingFragment)


        }
    }

    fun initPrinter(){
        printerFactory = ThermalPrinterFactory()
        rtPrinterKotlin = printerFactory?.create() as RTPrinter<BluetoothEdrConfigBean>?
        rtPrinterKotlin?.setPrinterInterface(curPrinterInterface)
    }

    private fun initRecyclerView() {
        adapterLabelsPrintingTableRv.initInterface(this , requireContext() , this)

        dataBinder.rvLabelsPrinting.recyclerviewBase.apply {

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    dataBinder.fABImportFile.isVisible = dy <= 0
                }
            })

            kuInitLinearLayoutManager(RecyclerView.VERTICAL, adapterLabelsPrintingTableRv)
        }

    }

    private fun setSearchNameProduct(list: List<LabelsPrinting>?) {
        list?.let {
            adapterNameProductAutoComplete = NameProductAutoCompleteAdapter(requireContext(), R.layout.item_auto_complete, list as java.util.ArrayList<LabelsPrinting>)
            dataBinder.apply {

                lySearch.kuShow()

                tvSearchNameProduct.apply {

                    setAdapter(adapterNameProductAutoComplete)
                    setOnItemClickListener { parent, view, position, id ->
                        try {
                            adapterNameProductAutoComplete.getItem(position).let { item ->

                                tvSearchNameProduct.setText("")
                                kuHideSoftKeyboard()
                                list.forEachIndexed { index, LabelsPrinting ->
                                    if (item.localId == LabelsPrinting.localId){
                                        adapterPosition(item.localId!! , index)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun adapterPosition(id: Int, position: Int) {
        adapterLabelsPrintingTableRv.smoothScrollToPosition(id , position)
        scrollToPosition(position)
    }

    private fun scrollToPosition(position: Int) {
        dataBinder.rvLabelsPrinting.recyclerviewBase.apply {
            removeOnScrollListener(onScrollListener)
            addOnScrollListener(onScrollListener)
            smoothScrollToPosition(position)
        }
    }

    private var onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE ->
                    //we reached the target position
                    recyclerView.removeOnScrollListener(this)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_header_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.log_out) {
            (activity as OneSingleActivity).inflateLogOutDialog(null)
        }
        return true
    }

    override fun setUpViewModelStateObservers() {
        try {
            (activity as OneSingleActivity).loadingProgress(true)
            activityViewModel.labelsPrintingMutableLiveData.observe(viewLifecycleOwner){
                dataBinder.swipeRefreshLayout.isRefreshing = false
                (activity as OneSingleActivity).loadingProgress(false)
                dataBinder.apply {
                    if (it.isEmpty()){
                        vAParent.displayedChild = vAParent.indexOfChild(vAImport)
                        statusInsert = false

                    }else{
                        statusInsert = true
                        vAParent.displayedChild = vAParent.indexOfChild(vAHorizontalScrollView)
                        adapterLabelsPrintingTableRv.setDataList(it)
                        setSearchNameProduct(it)
                    }
                }

            }

        }catch (e:Exception){
            e.printStackTrace()
        }

    }


    private fun saveDB(){
        val dialog = AddNewRowToDbFragmentDialog()
        dialog.setOnSaveItemClickListener(object :SaveDBCallBack{

            override fun saveNewLabelsPrinting(model: LabelsPrinting) {
                MainScope().launch {
                    mViewModel.saveLabel(model)
                    activityViewModel.getAllLabelsPrinting()
//                    navController.navigate(R.id.labelsPrintingFragment)
                    initPrinter()
                }
            }

        })
        dialog.show(requireActivity().supportFragmentManager, dialog.tag)
    }

    fun onClickAdd(){
        if (statusInsert){
            saveDB()
        }else{
            inflateImportBarcodeDialogWithPermissionCheck()
        }
    }

    // Mark -*- handle Permissions
    // NeedsPermission method is called when the user has not granted the permission to the app.
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE,)
    fun inflateImportBarcodeDialog(){
        val dialog = DialogImportExcelFragment()
        dialog.initDialogCallBack(this)
        dialog.show(requireActivity().supportFragmentManager ,dialog.tag )
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onRationaleAskStoragePermission(request: PermissionRequest) {
        // Show the rationale
        MaterialAlertDialogBuilder(dataBinder.root.context)
            .setTitle("إذن الصلاحيات")
            .setMessage("إذن تصريح الوصول إلى الملفات.")
            .setPositiveButton("موافق") { dialog, _ ->
                request.proceed()
                dialog.dismiss()
            }
            .setNegativeButton("إلغاء") { dialog, _ ->
                // Do nothing
                request.cancel()
                dialog.dismiss()
            }
            .show()
    }

    // OnPermissionDenied method is called if the user has denied the permission
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onDeniedAskStoragePermission() {
        // Do nothing
        Toast.makeText(dataBinder.root.context, "إذن تصريح الوصول إلى الملفات مرفوض.", Toast.LENGTH_SHORT).show()
    }

    // OnNeverAskAgain method is called if the user has denied the permission and checked "Never ask again"
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onNeverAskAgainAskStoragePermission() {
        // Do nothing
        kuInfoLog("TestPermission","onNeverAskAgainAskStoragePermission")
        val onApplicationSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        onApplicationSettings.data = Uri.parse("package:${requireActivity().packageName}")
        applicationSettingsScreen.launch(onApplicationSettings)
    }


    // Mark -*- handle Permissions
    // NeedsPermission method is called when the user has not granted the permission to the app.

    override fun dialogOnClick(idView: Int) {
        importExcelFile()
    }

    private fun importExcelFile() {
        MainScope().launch {
            activityViewModel.clearData()
            (activity as OneSingleActivity).openFilePicker()
            activityViewModel.workbookMutableLiveData.observe(viewLifecycleOwner){
                it?.let {
                    onSuccess(it)
                }
            }
        }

    }

    var x = 0
    private fun onSuccess(workbook: Workbook) {
        MainScope().launch (Dispatchers.IO) {
            delay(300)

            kuInfoLog("loadingProgress", "onSuccess == true")
            if (x == 0) {
                x =1
                (activity as OneSingleActivity).loadingProgress(true)
            }


            kuInfoLog("loadingProgress", "onSuccess == ${ workbook.getSheetAt(0).getRow(0).physicalNumberOfCells}")

            if (workbook.numberOfSheets > 0 && workbook.getSheetAt(0).getRow(0).physicalNumberOfCells == 5) {

                //Return first sheet of excel. You can get all existing sheets
                val number = workbook.numberOfSheets-1
                for (i in 0 ..number ){
                    getRow(workbook.getSheetAt(i) , i)
                }

                MainScope().launch {
                    mViewModel.apply {
                        saveLabelLists(labelsPrintingModels)
                        activityViewModel.workbookMutableLiveData.value = null

                        //update the UI
                        delay(1500)
                        (activity as OneSingleActivity).apply {
                            snackBarError("تم استراد الملف بنجاح" , R.color.TemplateGreen, R.color.white)
                            loadingProgress(false)
                            x = 0
                            activityViewModel.getAllLabelsPrinting()
                            initPrinter()
                        }

                    }
                }

            }else{
                (activity as OneSingleActivity).apply {
                    snackBarError("حدث خطأ ما فى ملف الاكسيل" , R.color.TemplateRed, R.color.white)
                    loadingProgress(false)
                    x = 0
                    activityViewModel.getAllLabelsPrinting()
                }

            }



        }
    }

    //Read Cell
    //Using sheet object, you can get sheet name, all rows, all columns and value of each cell of the sheet.
    // This sheet object will give you each and every information about the sheet.
    // columns run vertically
    // row run horizontally
    private fun getRow(sheet: Sheet?, numberOfSheets: Int): Boolean {
        var status = false
        if (sheet != null) {
            try {
                val rowIterator: Iterator<Row> = sheet.iterator()
                while (rowIterator.hasNext()) {
                    val row = rowIterator.next()
                    val cellIterator = row.cellIterator()
                    val labelsPrintingModel = LabelsPrinting()
                    while (cellIterator.hasNext()) {

                        val cell = cellIterator.next()
                        if (cell != null && cell.cellType != Cell.CELL_TYPE_BLANK) {
                            // This cell is not empty
                            if (cell.columnIndex > 0 && cell.rowIndex > 0) {
                                when (numberOfSheets) {
                                    0 -> {
                                      try {
                                          when (cell.columnIndex) {
                                              1 -> {
                                                  if (cell.cellType == Cell.CELL_TYPE_NUMERIC){
                                                      labelsPrintingModel.barCode = cell.numericCellValue.toInt().toString()
                                                  }else{
                                                      labelsPrintingModel.barCode = cell.toString()
                                                  }
                                              }
                                              2 -> labelsPrintingModel.nameProduct = cell.toString()
                                              3 -> {
                                                  if (cell.cellType == Cell.CELL_TYPE_NUMERIC){
                                                      labelsPrintingModel.price = cell.numericCellValue
                                                  }
                                                  labelsPrintingModels.add(labelsPrintingModel)

                                              }
                                          }
                                      }catch (e:Exception){}

                                    }
                                }
                            }
                        }
                    }
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                status = false
            } catch (e: IOException) {
                e.printStackTrace()
                status = false
            }
        }

        return status
    }

    override fun onPosition(position: Int) {
        scrollToPosition(position)
    }

    override fun deleteLabel(localId: Int, barcode: String) {
        runBlocking {
            val job = launch { mViewModel.deleteLabel(localId) }
            job.join()
            (activity as OneSingleActivity).activityViewModel.getAllLabelsPrinting()
        }

    }

    override fun onResume() {
        super.onResume()
        //TODO back session
        (activity as OneSingleActivity).startSessionCounter()
    }

    override fun printLabel(label: LabelsPrinting) {
        val config = ExtensionsApp.kuGetCustomModel(appPreferences, PrintConfig::class.java, "printConfig")
//        setStaticDevice(getBluetoothNameDevice(device?.macAddressPrinter))
        (activity as OneSingleActivity).apply {
            if(bluetoothUtils.isBluetoothConnected() && config != null &&  config.barcodeFormatConfig != null){

                MainScope().launch {
                    val dialog = DialogCountPrinterFragment()
                    dialog.show(supportFragmentManager ,dialog.tag )
                    delay(500 )
                    dialog.setCount(config.countPrint){countPrint ->
                        for (i in 0 until countPrint){
                            printLabelOrder(label , config)
                        }
                    }
                }
            }else{
                inflateConfigPrinterDialogWithPermissionCheck()
            }

        }

    }

    private fun printLabelOrder(label: LabelsPrinting, config: PrintConfig) {
        try {
            val width = bluetoothUtils.convertToPx(config.unitType , config.width , resources.displayMetrics)
            val height = bluetoothUtils.convertToPx(config.unitType , config.height , resources.displayMetrics)

            var bitmap = generateBarcode(  label.barCode!! , config.barcodeFormatConfig!!.zxingBarcodeType!!)

            try {
                bitmap = bitmap?.createBitmap(label , config.isVertical!!)
            }catch (e:Exception){
                snackBarError(e.message.toString() , R.color.TemplateRed, R.color.white)
                return
            }

            if (bitmap == null) {
                snackBarError("يوجد مشكله فى الباركود" , R.color.TemplateRed, R.color.white)
                return
            }

            val resized: Bitmap?
            try {
                resized = Bitmap.createScaledBitmap(bitmap, (width* 0.1).toInt(), (height* 0.1).toInt(), true)
            }catch (e:Exception){
                snackBarError(e.message.toString() , R.color.TemplateRed, R.color.white)
                return
            }
            dataBinder.mgBarCode.apply {
                setImageBitmap(resized)
                kuShow()
            }

            bluetoothUtils.printEscCommand( resized , config.isVertical){
                if (it.result){
                    snackBarError(it.msg , R.color.TemplateGreen, R.color.white)
                }else{
                    snackBarError(it.msg , R.color.TemplateRed, R.color.white)
                }
                dataBinder.mgBarCode.kuHide()
            }

        }catch (e:Exception){
            snackBarError("حدث خطأ ما فى الطباعة, الرجاء اتباع تعليمات الطباعة السليمه52" , R.color.TemplateRed, R.color.white)
        }

    }

    override fun onBluetoothUtilsListener(name: String) {
        dataBinder.tvDeviceSelected.text = name
    }

    override fun onBluetoothUtilsListener(tag: Int) {
        dataBinder.tvDeviceSelected.tag = tag
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


}