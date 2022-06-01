package com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.viewModel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.core.base.view.BaseActivity
import com.alignTech.labelsPrinting.core.util.AppConstants
import com.alignTech.labelsPrinting.core.util.AppConstants.PRINTER_MAC_ADDRESS
import com.alignTech.labelsPrinting.core.util.AppConstants.PRINTER_NAME
import com.alignTech.labelsPrinting.core.util.TemplateEnums
import com.alignTech.labelsPrinting.local.model.bluetooth.BluetoothModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PrinterConfigViewModel @Inject  constructor() : ViewModel() {

  val isBluetoothEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData(true) }
  val isBluetoothDiscovering: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }
  private val unboundedDevices: MutableLiveData<ArrayList<BluetoothModel>> = MutableLiveData(arrayListOf())
  val unboundDevicesLiveData: LiveData<ArrayList<BluetoothModel>> = unboundedDevices
  private val boundedDevices: MutableLiveData<ArrayList<BluetoothModel>> = MutableLiveData(arrayListOf())
  val boundedDevicesLiveData: LiveData<ArrayList<BluetoothModel>> = boundedDevices
  val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

  @Inject lateinit var appPreferences: KUPreferences

  val receiver by lazy {
    object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
          BluetoothDevice.ACTION_FOUND -> {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if (device?.name != null) addUnboundedDevice(device)
          }
          BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> isBluetoothDiscovering.value = false

          BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if (device?.bondState == BluetoothDevice.BOND_BONDED) {
              saveDeviceMacAddress(device.address, device.name, true, context)
              appPreferences.setValue("BluetoothDevice" , device)
            }
          }
        }
      }
    }
  }

  private fun addUnboundedDevice(device: BluetoothDevice) {
    if (isDeviceBounded(device)) unboundedDevices.value = unboundedDevices.value?.apply { add(BluetoothModel(device, false)) }
  }

  private fun isDeviceBounded(device: BluetoothDevice) =
    boundedDevices.value?.filter { it.bluetoothDevice.address == device.address }.isNullOrEmpty()


  fun saveDeviceMacAddress(address: String, printerName: String, restartDiscovery: Boolean, context: Context) {

    appPreferences.setValue(PRINTER_MAC_ADDRESS, address)
    appPreferences.setValue(PRINTER_NAME, printerName)
    Toast.makeText(context, context.resources.getString(R.string.added_device_successfully), Toast.LENGTH_SHORT).show()
    appPreferences.setValue(AppConstants.PRINTER_BLUETOOTH_DEVICE,address )

    if (restartDiscovery) {
      unboundedDevices.value = arrayListOf()
      requestEnableBluetooth(restartDiscovery, context)
    } else {
      boundedDevices.value = arrayListOf()
      addBoundedDevices(bluetoothAdapter?.bondedDevices, context)
    }
  }

  private fun addBoundedDevices(bondedDevices: Set<BluetoothDevice>?, context: Context) {
    val currentMac = appPreferences.getStringValue(PRINTER_MAC_ADDRESS, "")
    val data: ArrayList<BluetoothModel> = arrayListOf()
    bondedDevices?.forEach { data.add(BluetoothModel(it, it.address == currentMac)) }
    boundedDevices.value = data
  }

  fun requestEnableBluetooth(isEnabled: Boolean, context: Context) {
    if (isEnabled) {
      if (!bluetoothAdapter!!.isEnabled) bluetoothAdapter?.enable()
      unboundedDevices.value = arrayListOf()
      startDiscovery(context)
    } else bluetoothAdapter?.cancelDiscovery()
    isBluetoothDiscovering.value = isEnabled
  }

  private fun startDiscovery(context: Context) {
    bluetoothAdapter?.startDiscovery()
    addBoundedDevices(bluetoothAdapter?.bondedDevices, context)
  }


  fun handleDeviceSelected(device: BluetoothDevice, baseActivity: BaseActivity<*>) { // Version 1.0.13.1
    if (device.bluetoothClass.majorDeviceClass == TemplateEnums.DevicesTypes.IMAGING.type) {
      if (device.bondState == BluetoothDevice.BOND_NONE) {
        CoroutineScope(Dispatchers.IO).launch {
          if (!device.createBond()) withContext(Dispatchers.Main) { baseActivity.kuToast(baseActivity.resources.getString(R.string.could_not_pair)) }
        }
      } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
        saveDeviceMacAddress(device.address, device.name, false, baseActivity)
        appPreferences.setValue("BluetoothDevice" , device)

      }

    } else baseActivity.kuToast(baseActivity.resources.getString(R.string.please_select_printer))

  }


  fun clearAllData() {
    boundedDevices.value = arrayListOf()
    unboundedDevices.value = arrayListOf()
  }
}