package com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.alfayedoficial.kotlinutils.kuInfoLog
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.OnBluetoothDeviceSelected
import com.alignTech.labelsPrinting.core.base.view.BaseDialogFragment
import com.alignTech.labelsPrinting.core.util.ExtensionsApp
import com.alignTech.labelsPrinting.core.util.setBindString
import com.alignTech.labelsPrinting.databinding.FragmentDialogBluetoothDeviceChooseBinding
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.adapter.BluetoothAdapterKt
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.viewModel.PrinterConfigViewModel
import com.alignTech.labelsPrinting.ui.dialog.configPrint.view.CustomBluetoothDevice
import com.alignTech.labelsPrinting.ui.oneSingleActivity.view.OneSingleActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class DialogBluetoothDeviceChooseFragment : BaseDialogFragment<FragmentDialogBluetoothDeviceChooseBinding>() , OnBluetoothDeviceSelected {

    private val connectedDevicesAdapter: BluetoothAdapterKt by lazy { BluetoothAdapterKt(requireContext(), setOf(), this) }
    private val availableDevicesAdapter: BluetoothAdapterKt by lazy { BluetoothAdapterKt(requireContext(), setOf(), this) }
    private lateinit var discoverLauncher: ActivityResultLauncher<Intent>
    private val viewModel: PrinterConfigViewModel by viewModels()
    private var onBluetoothDeviceSelected : OnBluetoothDeviceSelected? =null

    interface OnBluetoothDeviceSelected {
        fun onBluetoothDeviceSelected(device: BluetoothDevice)
    }

    fun initInterface(onBluetoothDeviceSelected : OnBluetoothDeviceSelected){
        this.onBluetoothDeviceSelected = onBluetoothDeviceSelected
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (requireActivity() as OneSingleActivity).checkPermissions()
            kuToast("من فضلك قم بتفعيل الصلاحيات لإستخدام البلوتوث")
        }else{
            initActivityLaunchers()
            registerBluetoothReceiver()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        super.onCreateView(inflater, container, savedInstanceState)

        return rootView
    }

    override val layoutResourceLayout: Int
        get() = R.layout.fragment_dialog_bluetooth_device_choose

    override fun onFragmentCreated(dataBinder: FragmentDialogBluetoothDeviceChooseBinding) {
        dataBinder.apply {
            fragment = this@DialogBluetoothDeviceChooseFragment
            lifecycleOwner = this@DialogBluetoothDeviceChooseFragment

            setUpViewModelStateObservers()
            rvBluetoothPairedDevices.adapter = connectedDevicesAdapter
            rvBluetoothAvailableDevices.adapter = availableDevicesAdapter

            if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (requireActivity() as OneSingleActivity).checkPermissions()
                kuToast("من فضلك قم بتفعيل الصلاحيات لإستخدام البلوتوث")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
                return
            }


            bluetoothDeviceName.setBindString(viewModel.bluetoothAdapter?.name)

            Glide.with(requireContext()).asGif().load(R.drawable.reload2)
                .into(pbBluetoothSearchForDevices)

            setViewsClickListeners()

        }
    }

    private fun setUpViewModelStateObservers() {

        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (requireActivity() as OneSingleActivity).checkPermissions()
            kuToast("من فضلك قم بتفعيل الصلاحيات لإستخدام البلوتوث")
        }else{
            viewModel.apply {
                dataBinder.apply {
                    isBluetoothEnabled.observe(viewLifecycleOwner) { isEnabled ->
                        switchEnableBluetooth.isChecked = isEnabled
                        requestEnableBluetooth(isEnabled, requireContext())
                        if (!isEnabled) clearAllData()
                    }
                    boundedDevicesLiveData.observe(viewLifecycleOwner) {
                        connectedDevicesAdapter.updateData(it.toSet())
                    }

                    viewModel.unboundDevicesLiveData.observe(viewLifecycleOwner) {
                        availableDevicesAdapter.updateData(it.toSet())
                    }
                    viewModel.isBluetoothDiscovering.observe(viewLifecycleOwner) {
                        if (it && !switchEnableBluetooth.isChecked) switchEnableBluetooth.isChecked =
                            true
                        stopDiscoveryGif(it)
                    }
                }
            }
        }

    }


    private fun setViewsClickListeners() {
        dataBinder.apply {

            pbBluetoothSearchForDevices.setOnClickListener {
                val b = !(pbBluetoothSearchForDevices.drawable as GifDrawable).isRunning
                viewModel.isBluetoothDiscovering.value = b
                viewModel.requestEnableBluetooth(b, requireContext())
            }
            switchEnableBluetooth.setOnClickListener {
                if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (requireActivity() as OneSingleActivity).checkPermissions()
                    kuToast("من فضلك قم بتفعيل الصلاحيات لإستخدام البلوتوث")
                }else{
                    viewModel.isBluetoothEnabled.value = !viewModel.isBluetoothEnabled.value!!
                }
            }
        }

    }

    private fun initActivityLaunchers() { discoverLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    viewModel.isBluetoothEnabled.value = false
                    kuToast(resources.getString(R.string.must_give_permission_to_continue))
                } else viewModel.requestEnableBluetooth(true, requireContext())
            }
    }


    private fun stopDiscoveryGif(isEnabled: Boolean) {
        dataBinder.apply {
            if (pbBluetoothSearchForDevices.drawable is GifDrawable) {
                val drawable = pbBluetoothSearchForDevices.drawable as GifDrawable
                if (isEnabled) drawable.start()
                else drawable.stop()
            }
        }
    }

    override fun onItemSelected(device: BluetoothDevice) {
        appPreferences.setValue("CustomBluetoothDevice" , CustomBluetoothDevice(macAddressPrinter = device.address, connectDate = Date()))
        kuInfoLog("TestLog", "staticDevice : ${device}")
        val device2 = ExtensionsApp.kuGetCustomModel(appPreferences, CustomBluetoothDevice::class.java, "CustomBluetoothDevice")
        kuInfoLog("TestLog", "staticDevice : ${device2}")

        viewModel.handleDeviceSelected(device, requireActivity() as OneSingleActivity)
        onBluetoothDeviceSelected?.onBluetoothDeviceSelected(device)
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (!viewModel.receiver.isOrderedBroadcast) requireActivity().unregisterReceiver(viewModel.receiver)
            viewModel.isBluetoothEnabled.value = true
        }catch (e:Exception){
            e.printStackTrace()
        }
    }



    private fun registerBluetoothReceiver() {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            requireActivity().registerReceiver(viewModel.receiver, intentFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }






}