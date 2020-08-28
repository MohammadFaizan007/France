package com.SmartLightSensor.ServiceModule;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData.Builder;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.ParcelUuid;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.logging.LogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

@TargetApi(21)
public class MyBeaconTransmitter {
   ;
    /** @deprecated */
    @Deprecated
    public static final int NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS = 3;
    private static final String TAG = "MyBeaconTransmitter";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private int mAdvertiseMode = 0;
    private int mAdvertiseTxPowerLevel = 3;
    private Beacon mBeacon;
    private BeaconParser mBeaconParser;
    private AdvertiseCallback mAdvertisingClientCallback;
    private boolean mStarted;
    private AdvertiseCallback mAdvertiseCallback;
    private boolean mConnectable = false;
    private int mAdvertiseTimeout=2*1000;  //// 10 second time out

    public MyBeaconTransmitter(Context context, BeaconParser parser) {

        this.mBeaconParser = parser;
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                this.mBluetoothLeAdvertiser = this.mBluetoothAdapter.getBluetoothLeAdvertiser();
            }
            LogManager.d("MyBeaconTransmitter", "new MyBeaconTransmitter constructed.  mbluetoothLeAdvertiser is %s", new Object[]{this.mBluetoothLeAdvertiser});
        } else {
            LogManager.e("MyBeaconTransmitter", "Failed to get BluetoothManager", new Object[0]);
        }

    }

    public void setAdvertiseTimeout(int mAdvertiseTimeout) {
        this.mAdvertiseTimeout = mAdvertiseTimeout;
    }

    public int getAdvertiseTimeout() {
        return mAdvertiseTimeout;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public void setBeacon(Beacon beacon) {
        this.mBeacon = beacon;
    }

    public void setBeaconParser(BeaconParser beaconParser) {
        this.mBeaconParser = beaconParser;
    }

    public int getAdvertiseMode() {
        return this.mAdvertiseMode;
    }

    public void setAdvertiseMode(int mAdvertiseMode) {
        this.mAdvertiseMode = mAdvertiseMode;
    }

    public int getAdvertiseTxPowerLevel() {
        return this.mAdvertiseTxPowerLevel;
    }

    public void setAdvertiseTxPowerLevel(int mAdvertiseTxPowerLevel) {
        this.mAdvertiseTxPowerLevel = mAdvertiseTxPowerLevel;
    }

    public void setConnectable(boolean connectable) {
        this.mConnectable = connectable;
    }

    public boolean isConnectable() {
        return this.mConnectable;
    }

    public void startAdvertising(Beacon beacon) {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
//        {
//            return;
//        }
        this.startAdvertising(beacon, null);
    }

    public void startAdvertising(Beacon beacon, AdvertiseCallback callback) {

        this.mBeacon = beacon;
        this.mAdvertisingClientCallback = callback;
        this.startAdvertising();
    }

    public void startAdvertising() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            Log.w("MyBeaconTransmitter","start advertising Api level is below 21 ");
            return;
        }
        if (this.mBeacon == null) {
            throw new NullPointerException("Beacon cannot be null.  Set beacon before starting advertising");
        } else
        {
            int manufacturerCode = this.mBeacon.getManufacturer();
            int serviceUuid = -1;
            if (this.mBeaconParser.getServiceUuid() != null) {
                serviceUuid = this.mBeaconParser.getServiceUuid().intValue();
            }

            if (this.mBeaconParser == null) {
                throw new NullPointerException("You must supply a BeaconParser instance to MyBeaconTransmitter.");
            } else {
                byte[] advertisingBytes = this.mBeaconParser.getBeaconAdvertisementData(this.mBeacon);
                String byteString = "";

                for(int i = 0; i < advertisingBytes.length; ++i) {
                    byteString = byteString + String.format("%02X", advertisingBytes[i]);
                    byteString = byteString + " ";
                }

                LogManager.d("MyBeaconTransmitter", "Starting advertising with ID1: %s ID2: %s ID3: %s and data: %s of size %s", new Object[]{this.mBeacon.getId1(), this.mBeacon.getIdentifiers().size() > 1 ? this.mBeacon.getId2() : "", this.mBeacon.getIdentifiers().size() > 2 ? this.mBeacon.getId3() : "", byteString, advertisingBytes.length});

                try {
                    Builder dataBuilder = new Builder();
                    if (serviceUuid > 0) {
                        byte[] serviceUuidBytes = new byte[]{(byte)(serviceUuid & 255), (byte)(serviceUuid >> 8 & 255)};
                        ParcelUuid parcelUuid = parseUuidFrom(serviceUuidBytes);
                        dataBuilder.addServiceData(parcelUuid, advertisingBytes);
                        dataBuilder.addServiceUuid(parcelUuid);
                        dataBuilder.setIncludeTxPowerLevel(false);
                        dataBuilder.setIncludeDeviceName(false);
                    } else {
                        dataBuilder.addManufacturerData(manufacturerCode, advertisingBytes);
                    }

                    AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
                    settingsBuilder.setAdvertiseMode(this.mAdvertiseMode);
                    settingsBuilder.setTxPowerLevel(this.mAdvertiseTxPowerLevel);
                    settingsBuilder.setConnectable(this.mConnectable);
                    Log.w(TAG,this.mAdvertiseTimeout+" timeout");
                    settingsBuilder.setTimeout(this.mAdvertiseTimeout);

                    this.mBluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), this.getAdvertiseCallback());

                    LogManager.d("MyBeaconTransmitter", "Started advertisement with callback: %s", new Object[]{this.getAdvertiseCallback()});
                } catch (Exception var8) {
                    LogManager.e(var8, "MyBeaconTransmitter", "Cannot start advertising due to exception", new Object[0]);
                }

            }
        }
    }

    public void stopAdvertising() {
        Log.e(TAG, " stoping advertising - 1234");
        if (!this.mStarted) {
            LogManager.e("MyBeaconTransmitter", "Skipping stop advertising -- not started", new Object[0]);
        } else {
            LogManager.e("MyBeaconTransmitter", "Stopping advertising with object %s", new Object[]{this.mBluetoothLeAdvertiser});
            this.mAdvertisingClientCallback = null;

            try {
                this.mBluetoothLeAdvertiser.stopAdvertising(this.getAdvertiseCallback());
            } catch (IllegalStateException var2) {
                LogManager.e("MyBeaconTransmitter", "Bluetooth is turned off. Transmitter stop call failed.", new Object[0]);
            }

            this.mStarted = false;
        }
    }


    private AdvertiseCallback getAdvertiseCallback() {
        if (this.mAdvertiseCallback == null) {
            this.mAdvertiseCallback = new AdvertiseCallback() {
                public void onStartFailure(int errorCode) {
                    LogManager.e("MyBeaconTransmitter", "Advertisement start failed, code: %s", new Object[]{errorCode});
                    if (MyBeaconTransmitter.this.mAdvertisingClientCallback != null) {
                        MyBeaconTransmitter.this.mAdvertisingClientCallback.onStartFailure(errorCode);
                    }

                }

                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    LogManager.i("MyBeaconTransmitter", "Advertisement start succeeded.", new Object[0]);
                    MyBeaconTransmitter.this.mStarted = true;
                    if (MyBeaconTransmitter.this.mAdvertisingClientCallback != null) {
                        MyBeaconTransmitter.this.mAdvertisingClientCallback.onStartSuccess(settingsInEffect);
                    }

                }
            };
        }

        return this.mAdvertiseCallback;
    }

    private static ParcelUuid parseUuidFrom(byte[] uuidBytes) {

        ParcelUuid BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
        if (uuidBytes == null) {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        } else {
            int length = uuidBytes.length;
            if (length != 2 && length != 4 && length != 16) {
                throw new IllegalArgumentException("uuidBytes length invalid - " + length);
            } else if (length == 16) {
                ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
                long msb = buf.getLong(8);
                long lsb = buf.getLong(0);
                return new ParcelUuid(new UUID(msb, lsb));
            } else {
                long shortUuid;
                if (length == 2) {
                    shortUuid = (long)(uuidBytes[0] & 255);
                    shortUuid += (long)((uuidBytes[1] & 255) << 8);
                } else {
                    shortUuid = (long)(uuidBytes[0] & 255);
                    shortUuid += (long)((uuidBytes[1] & 255) << 8);
                    shortUuid += (long)((uuidBytes[2] & 255) << 16);
                    shortUuid += (long)((uuidBytes[3] & 255) << 24);
                }

                long msb = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
                long lsb = BASE_UUID.getUuid().getLeastSignificantBits();
                return new ParcelUuid(new UUID(msb, lsb));
            }
        }
    }
}


