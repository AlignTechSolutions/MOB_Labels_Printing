package com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.alignTech.labelsPrinting.R;
import com.alignTech.labelsPrinting.ui.dialog.bluetoothDevice.adapter.BluetoothDeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/6/30.
 */
public class BluetoothDeviceChooseDialog extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private onDeviceItemClickListener mListener;
    private ListView lvPairedDevices, lvFoundDevices;
    private TextView tvPairedDeviceEmpty, tvFoundDeviceEmpty, tvSearchDevice;
    private ProgressBar progressBar;
    private Button btn_hide;

    private BroadcastReceiver mBluetoothReceiver;
    private IntentFilter mBluetoothIntentFilter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDeviceAdapter pairedDeviceAdapter, foundDeviceAdapter;
    private List<BluetoothDevice> pairedDeviceList, foundDeviceList;
    private boolean mSearchInited = false;// 若为true表示搜索设备按钮已按下过，数据已初始化
    private boolean mRegistered = false;// 若为true表示接收器已注册
    private boolean isHidePairedDevlist = false;//是否隐藏配对列表
    private SwitchCompat switch_enable_bluetooth;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_bluetooth_device, null);
        initView(view);
        setListener();
        initData();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true).setNegativeButton(R.string.dialog_cancel, null);
        return builder.create();
    }

    private void initView(View view) {
        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Toast.makeText(requireContext(),"من فضلك قم بتفعيل الصلاحيات لإستخدام البلوتوث",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
            intent.setData(uri) ;
            startActivity(intent);
            return;
        }

        lvPairedDevices = (ListView) view.findViewById(R.id.lv_dialog_choose_bluetooth_device_paired_devices);
        lvFoundDevices = (ListView) view.findViewById(R.id.lv_dialog_choose_bluetooth_device_found_devices);
        tvPairedDeviceEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_paired_devices_empty);
        tvFoundDeviceEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_found_devices_empty);
        tvSearchDevice = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_search_device);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_dialog_choose_bluetooth_device_progress_bar);
        btn_hide = view.findViewById(R.id.btn_hide);
        switch_enable_bluetooth = view.findViewById(R.id.switch_enable_bluetooth);

        switch_enable_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBluetooth(switch_enable_bluetooth.isChecked());
            }
        });
    }

    public void setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            switch_enable_bluetooth.setChecked(true);
            bluetoothAdapter.enable();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                initData();
            }catch (Exception e){ }
        }
        else if(!enable && isEnabled) {
            switch_enable_bluetooth.setChecked(false);
            pairedDeviceList = new ArrayList<>();
            pairedDeviceAdapter = new BluetoothDeviceAdapter(mContext, pairedDeviceList);
            lvPairedDevices.setAdapter(pairedDeviceAdapter);
            tvPairedDeviceEmpty.setVisibility(View.VISIBLE);
            bluetoothAdapter.disable();
        }

    }

    private void setListener() {
        tvSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSearchDevice.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                tvFoundDeviceEmpty.setVisibility(View.GONE);
                if (mSearchInited) {
                    foundDeviceList.clear();
                    foundDeviceAdapter.notifyDataSetChanged();
                } else {
                    foundDeviceList = new ArrayList<>();
                    foundDeviceAdapter = new BluetoothDeviceAdapter(mContext, foundDeviceList);
                    lvFoundDevices.setAdapter(foundDeviceAdapter);
                    mBluetoothReceiver = new BluetoothDeviceReceiver();
                    mBluetoothIntentFilter = new IntentFilter();
                    mBluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    mBluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    mSearchInited = true;
                }
                mContext.registerReceiver(mBluetoothReceiver, mBluetoothIntentFilter);
                mRegistered = true;
                mBluetoothAdapter.startDiscovery();
            }
        });
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                if (mRegistered) {
                    mContext.unregisterReceiver(mBluetoothReceiver);
                    mRegistered = false;
                }
                mListener.onDeviceItemClick((BluetoothDevice) parent.getAdapter().getItem(position));
                getDialog().dismiss();
            }
        });
        lvFoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                if (mRegistered) {
                    mContext.unregisterReceiver(mBluetoothReceiver);
                    mRegistered = false;
                }
                mListener.onDeviceItemClick((BluetoothDevice) parent.getAdapter().getItem(position));
                getDialog().dismiss();
            }
        });
        btn_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHidePairedDevlist){//当前是隐藏
                    isHidePairedDevlist = false;
                    lvPairedDevices.setVisibility(View.VISIBLE);
                    btn_hide.setText(getString(R.string.hide));
                }else{//当前是可见
                    isHidePairedDevlist = true;
                    lvPairedDevices.setVisibility(View.GONE);
                    btn_hide.setText(R.string.show);
                }
            }
        });
    }

    private void initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            switch_enable_bluetooth.setChecked(false);
            Toast.makeText(requireContext() , "من فضلك قم بفتح البلوتوث" , Toast.LENGTH_LONG).show();
        }else {
            switch_enable_bluetooth.setChecked(true);
            pairedDeviceList = new ArrayList<>();
            List<BluetoothDevice> BondedDevicesList = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
            for (int i =0; i <= BondedDevicesList.size()-1; i++) {
                if (BondedDevicesList.get(i).getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    pairedDeviceList.add(BondedDevicesList.get(i));
                }
            }
            if (pairedDeviceList.size() == 0) {
                tvPairedDeviceEmpty.setVisibility(View.VISIBLE);
            }else {
                tvPairedDeviceEmpty.setVisibility(View.GONE);
            }
            pairedDeviceAdapter = new BluetoothDeviceAdapter(mContext, pairedDeviceList);
            lvPairedDevices.setAdapter(pairedDeviceAdapter);
        }

    }



    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mBluetoothAdapter.cancelDiscovery();
        if (mRegistered) {
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
    }

    public void setOnDeviceItemClickListener(onDeviceItemClickListener listener) {
        mListener = listener;
    }

    public interface onDeviceItemClickListener {
        void onDeviceItemClick(BluetoothDevice device);
    }

    private class BluetoothDeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int devType = device.getBluetoothClass().getMajorDeviceClass();
                if(devType != BluetoothClass.Device.Major.IMAGING){
                    return;
                }

                if (!foundDeviceList.contains(device)) {
                    foundDeviceList.add(device);
                    foundDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mBluetoothAdapter.cancelDiscovery();
                mContext.unregisterReceiver(mBluetoothReceiver);
                mRegistered = false;
                tvSearchDevice.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (foundDeviceList.size() == 0) {
                    tvFoundDeviceEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }



}



