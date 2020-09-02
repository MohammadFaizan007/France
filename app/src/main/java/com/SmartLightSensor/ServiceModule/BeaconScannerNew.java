package com.SmartLightSensor.ServiceModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.InterfaceModule.MyBeaconScanner;
import com.SmartLightSensor.PogoClasses.BeconDeviceClass;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconScannerNew implements BeaconConsumer {
    MyBeaconScanner myBeaconScanner;
    boolean mAllowRebind;
    ArrayList<BeconDeviceClass> arrayList;
    public static final int SCAN_SUCCESS_CODE = 200;
    public static final int SCAN_FAIL_CODE = 201;
    public static final int SCANNING_TIMEOUT = 15 * 1000;
    public static final String EDDYSTONE_URL_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v";
    public static final String IBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static final String SWICTES_LAYOUT = "x,m:0-1=da03,i:0-1,d:2-3,d:4-5,d:6-7,d:8-9,d:9-10";
    private BeaconManager mBeaconManager;
    Activity activity;
    String TAG = "ScanningBeacon";
    int scanPeriod = 500;
    int request = 0x4f;
    int resultCode = SCAN_FAIL_CODE;
    String url = "rx";
    Handler handler;
    ByteQueue byteQueue;
    AnimatedProgress animatedProgress;
    Region region;
    private Runnable runnable = this::stop;

    public BeaconScannerNew(Activity activity) {
        BeaconManager.setDebug(true);
        BeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager = BeaconManager.getInstanceForApplication(activity);
        this.activity = activity;
        animatedProgress = new AnimatedProgress(activity);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(IBEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(SWICTES_LAYOUT));

        mBeaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setForegroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundScanPeriod(scanPeriod);
        mBeaconManager.setForegroundScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundMode(false);
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        arrayList = new ArrayList<>();

    }

    public void setMyBeaconScanner(MyBeaconScanner myBeaconScanner) {
        this.myBeaconScanner = myBeaconScanner;
    }

    public MyBeaconScanner getMyBeaconScanner() {
        return myBeaconScanner;
    }

    public void start() {

        region = new Region("all-beacons-region", null, null, null);
        try {
//                Log.w(TAG, "onBeaconServiceConnect try");
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.w(TAG, "onBeaconServiceConnect catch" + e.getMessage());
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.w(TAG, "BeaconsReceive" + beacons.size());

            }
        });
        handler();
    }

    public void startWithHandler() {
//    animatedProgress.showProgress();
        region = new Region("all-beacons-region", null, null, null);
        try {
//                Log.w(TAG, "onBeaconServiceConnect try");
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.w(TAG, "onBeaconServiceConnect catch" + e.getMessage());
            e.printStackTrace();
        }
//        mBeaconManager.setRangeNotifier(this);
        handler();
    }

    public void stop() {
//            arrayList.clear();
        if (region != null) {
            try {
                mBeaconManager.stopRangingBeaconsInRegion(region);
                mBeaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.w(TAG, "stopping error" + e.toString());
            }
        }
        mBeaconManager.removeAllRangeNotifiers();
        if (handler != null)
            handler.removeCallbacks(runnable);
//    animatedProgress.hideProgress();


    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void handler() {
        handler = new Handler();

        handler.postDelayed(runnable, SCANNING_TIMEOUT);
    }

    boolean hasBeacon(BeconDeviceClass beconDeviceClass) {
        int i = 0;
        for (BeconDeviceClass beconDeviceClass1 : arrayList) {
            if (beconDeviceClass1.getBeaconUID() == beconDeviceClass.getBeaconUID()) {
                Log.w("Has", "hash");
                return true;
            } else
                i++;

        }
        return i != arrayList.size();

    }

    @Override
    public void onBeaconServiceConnect() {

    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }
}
