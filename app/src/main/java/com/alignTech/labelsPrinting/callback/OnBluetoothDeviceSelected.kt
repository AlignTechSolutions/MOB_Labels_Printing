package com.alignTech.labelsPrinting.callback

import android.bluetooth.BluetoothDevice

interface OnBluetoothDeviceSelected {

    fun onItemSelected(device:BluetoothDevice)
}