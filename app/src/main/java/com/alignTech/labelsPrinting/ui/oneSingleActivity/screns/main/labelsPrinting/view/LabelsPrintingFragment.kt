package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.callback.OnDelete
import com.alignTech.labelsPrinting.callback.SaveDBCallBack
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.AppConstants.PICK_FILE_RESULT_CODE
import com.alignTech.labelsPrinting.core.util.setBaseActivityFragmentsToolbar
import com.alignTech.labelsPrinting.databinding.FragmentLabelsPrintingBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.dialog.addNewRow.view.AddNewRowToDbFragmentDialog
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view.KotlinMainFragmentDialog
import com.alignTech.labelsPrinting.ui.dialog.importExcel.view.DialogImportExcelFragment
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.LabelsPrintingTableRvAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.NameProductAutoCompleteAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.viewModel.LabelsPrintingViewModel
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.configObj
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.curPrinterInterface
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.printEnable
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.printerFactory
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.rtPrinterKotlin
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.staticDevice
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.txtDeviceSelected
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity.Companion.txtDeviceSelectedTag
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.printer.RTPrinter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import pub.devrel.easypermissions.EasyPermissions
import java.io.FileNotFoundException
import java.io.IOException


@AndroidEntryPoint
class LabelsPrintingFragment : BaseFragment<FragmentLabelsPrintingBinding>() , DialogCallBack ,
    LabelsPrintingTableRvAdapter.RecyclerviewPosition  , OnDelete  {

    private val activityViewModel : OneSingleViewModel by activityViewModels()
    private val mViewModel : LabelsPrintingViewModel by viewModels()
    private val labelsPrintingModels: ArrayList<LabelsPrinting> = arrayListOf()
    private val adapterLabelsPrintingTableRv by lazy { LabelsPrintingTableRvAdapter() }
    private lateinit var adapterNameProductAutoComplete: NameProductAutoCompleteAdapter
    private var statusInsert = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

            initPrinter()

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
            inflateImportBarcodeDialog()
        }
    }

    fun inflateImportBarcodeDialog(){
        val dialog = DialogImportExcelFragment()
        dialog.initDialogCallBack(this)
        dialog.show(requireActivity().supportFragmentManager ,dialog.tag )
    }

    override fun dialogOnClick(idView: Int) {
        importExcelFile()
    }

    private fun importExcelFile() {
        MainScope().launch {
            (activity as OneSingleActivity).activityViewModel.clearData()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 33 and higher
            // Request MANAGE_EXTERNAL_STORAGE (more modern approach)
            if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                EasyPermissions.requestPermissions(requireActivity(), "Needed for file access",
                    PICK_FILE_RESULT_CODE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE);
                return;
            } else {
                // Use Storage Access Framework (SAF) for Android 33 and above
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "*/*"
//                startActivityForResult(intent, PICK_FILE_RESULT_CODE)
                (activity as OneSingleActivity).openFilePicker()
                activityViewModel.workbookMutableLiveData.observe(viewLifecycleOwner) {
                    it?.let {
                        onSuccess(it)
                    }
                }
            }
        }else{
            if (!EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )) {
                EasyPermissions.requestPermissions(
                    requireActivity(),
                    "Needed for the ${getString(R.string.app_name)}",
                    PICK_FILE_RESULT_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                return
            }else{
                (activity as OneSingleActivity).openFilePicker()
                activityViewModel.workbookMutableLiveData.observe(viewLifecycleOwner){
                    it?.let {
                        onSuccess(it)
                    }
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
        (activity as OneSingleActivity).startSessionCounter()
    }

    override fun printLabel(label: LabelsPrinting) {

        if (configObj == null || rtPrinterKotlin == null || printerFactory == null
            || txtDeviceSelected == null || txtDeviceSelectedTag == null || staticDevice == null || printEnable == null) {
            (activity as OneSingleActivity).showBluetoothDeviceChooseDialog()
            return
        } else {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!mBluetoothAdapter.isEnabled) {
                Toast.makeText(requireContext(), "من فضلك قم بفتح البلوتوث", Toast.LENGTH_LONG).show()
                return
            } else {

                configObj = BluetoothEdrConfigBean(staticDevice)
                val kotlinMainFragmentDialog = KotlinMainFragmentDialog().apply {
                    setLabelsPrinting(label)
                }
                kotlinMainFragmentDialog.show(childFragmentManager, null)
            }

        }



    }







}