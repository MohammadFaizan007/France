package com.SmartLightSensor.ReceiverModule;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BLEBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    // Bluetooth has been turned off;
                    Log.w("BLEReceiver","Bluetooth state off");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.w("BLEReceiver","Bluetooth off");
                    // Bluetooth is turning off;
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.w("BLEReceiver","Bluetooth state on");
                    // Bluetooth has been on
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.w("BLEReceiver","Bluetooth turning on");
                    // Bluetooth is turning on
                    break;
            }
        }
    }
}
