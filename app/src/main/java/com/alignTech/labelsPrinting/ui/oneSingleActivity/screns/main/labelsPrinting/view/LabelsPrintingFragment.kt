package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.view

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.alfayedoficial.kotlinutils.*
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.DialogCallBack
import com.alignTech.labelsPrinting.callback.OnDelete
import com.alignTech.labelsPrinting.core.base.view.BaseFragment
import com.alignTech.labelsPrinting.core.util.AppConstants.PICK_FILE_RESULT_CODE
import com.alignTech.labelsPrinting.core.util.setBaseActivityFragmentsToolbar
import com.alignTech.labelsPrinting.databinding.FragmentLabelsPrintingBinding
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.ui.dialog.importExcel.view.DialogImportExcelFragment
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.LabelsPrintingTableRvAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter.NameProductAutoCompleteAdapter
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.viewModel.LabelsPrintingViewModel
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.alignTech.labelsPrinting.ui.oneSingleActivity.viewModel.OneSingleViewModel
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
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
    LabelsPrintingTableRvAdapter.RecyclerviewPosition  , OnDelete , PrinterObserver {

    private val activityViewModel : OneSingleViewModel by activityViewModels()
    private val mViewModel : LabelsPrintingViewModel by viewModels()
    private val labelsPrintingModels: ArrayList<LabelsPrinting> = arrayListOf()
    private val adapterLabelsPrintingTableRv by lazy { LabelsPrintingTableRvAdapter() }
    private lateinit var adapterNameProductAutoComplete: NameProductAutoCompleteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        super.onCreateView(inflater, container, savedInstanceState)

        PrinterObserverManager.getInstance().add(this@LabelsPrintingFragment)
        return rootView
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_labels_printing

    override fun onFragmentCreated(dataBinder: FragmentLabelsPrintingBinding) {
        dataBinder.apply {
            fragment = this@LabelsPrintingFragment
            lifecycleOwner = this@LabelsPrintingFragment

            (activity as OneSingleActivity).loadingProgress(true)
            initRecyclerView()

            toolbar.apply {
                setBaseActivityFragmentsToolbar(kuRes.getString(R.string.app_name), baseToolbar, tvToolbar)
            }
        }
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
            activityViewModel.labelsPrintingMutableLiveData.observe(viewLifecycleOwner){
                (activity as OneSingleActivity).loadingProgress(false)
                dataBinder.apply {
                    if (it.isEmpty()){
                        vAParent.displayedChild = vAParent.indexOfChild(vAImport)
                    }else{
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


    fun saveDB(){

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
        if (!EasyPermissions.hasPermissions(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, )) {
            EasyPermissions.requestPermissions(requireActivity(), "Needed for the ${getString(R.string.app_name)}", PICK_FILE_RESULT_CODE,
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

    private fun onSuccess(workbook: Workbook) {
        MainScope().launch (Dispatchers.IO) {
            delay(300)

            (activity as OneSingleActivity).loadingProgress(true)

            if (workbook.numberOfSheets > 0) {

                //Return first sheet of excel. You can get all existing sheets
                val number = workbook.numberOfSheets-1
                for (i in 0 ..number ){
                    getRow(workbook.getSheetAt(i) , i)
                }

            }

            MainScope().launch {
                mViewModel.apply {
                    saveLabelLists(labelsPrintingModels)
                    activityViewModel.workbookMutableLiveData.value = null

                    //update the UI
                    delay(1000)
                    (activity as OneSingleActivity).apply {
                        snackBarError("تم استراد الملف بنجاح" , R.color.TemplateGreen, R.color.white)
                        loadingProgress(false)
                    }

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
                                                    labelsPrintingModel.price = cell.numericCellValue.toInt()
                                                }
                                                labelsPrintingModels.add(labelsPrintingModel)

                                            }
                                        }

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

    override fun printLabel(label: LabelsPrinting){
       kuToast("print" + label.nameProduct)
    }

    override fun printerObserverCallback(p0: PrinterInterface<*>?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun printerReadMsgCallback(p0: PrinterInterface<*>?, p1: ByteArray?) {
        TODO("Not yet implemented")
    }


}