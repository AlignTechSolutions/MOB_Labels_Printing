package com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.callback.OnBluetoothDeviceSelected
import com.alignTech.labelsPrinting.core.util.TemplateEnums
import com.alignTech.labelsPrinting.databinding.ItemRvBluetoothDeviceBinding
import com.alignTech.labelsPrinting.local.model.bluetooth.BluetoothModel

class BluetoothAdapterKt(
    private val context: Context,
    private var mDevices: Set<BluetoothModel>,
    private val clickListener: OnBluetoothDeviceSelected
) :
    RecyclerView.Adapter<ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ItemRvBluetoothDeviceBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.item_rv_bluetooth_device,
                parent,
                false
            )
        return ViewHolder(binding, context, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mDevices.elementAt(position))

    override fun getItemCount(): Int = mDevices.size

    fun updateData(device: Set<BluetoothModel>) {
        mDevices = device
        notifyDataSetChanged()
    }

    fun clear() {
        mDevices = setOf()
        notifyDataSetChanged()
    }

    fun getData() = mDevices
}

class ViewHolder(
    private val binding: ItemRvBluetoothDeviceBinding,
    private val context: Context,
    private val clickListener: OnBluetoothDeviceSelected
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(device: BluetoothModel) {
        binding.tvBltDeviceName.text = device.bluetoothDevice.name
        binding.tvBltDeviceName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDeviceDrawable(
                device.bluetoothDevice.bluetoothClass.majorDeviceClass
            ), 0, 0, 0
        )

        if (device.isSelected){
            binding.layoutContainer.setBackgroundResource(R.drawable.bluetooth_device_selected_background)
        }else binding.layoutContainer.setBackgroundResource(R.color.TemplateLightGrey)
        binding.layoutContainer.setOnClickListener {
            clickListener.onItemSelected(device.bluetoothDevice)
        }
    }

    private fun getDeviceDrawable(deviceClass: Int): Int {
        return when (deviceClass) {
            TemplateEnums.DevicesTypes.AUDIO_VIDEO.type -> R.drawable.ic_baseline_headset_mic_24
            TemplateEnums.DevicesTypes.PHONE.type -> R.drawable.ic_baseline_phone_iphone_24
            TemplateEnums.DevicesTypes.COMPUTER.type -> R.drawable.ic_baseline_desktop_mac_24
            TemplateEnums.DevicesTypes.IMAGING.type -> R.drawable.ic_baseline_local_printshop_24
            else -> R.drawable.ic_baseline_device_unknown_24
        }
    }

    companion object{
        private const val TAG = "BluetoothAdapterKt"
    }

}