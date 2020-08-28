package com.SmartLightSensor.ServiceModule;

import android.app.Activity;
import android.database.Cursor;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.EncodeDecodeModule.ArrayUtilities;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.MyBeaconScanner;
import com.SmartLightSensor.PogoClasses.BeconDeviceClass;
import com.SmartLightSensor.activity.AppHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

public class ScanningBeacon implements RangeNotifier {
    MyBeaconScanner myBeaconScanner;
    boolean mAllowRebind;
    ArrayList<BeconDeviceClass> arrayList;
    public static final int SCAN_SUCCESS_CODE = 200;
    public static final int SCAN_FAIL_CODE = 201;
    public static final int SCANNING_TIMEOUT = 15 * 1000;
    public static final String EDDYSTONE_URL_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v";
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


    public ScanningBeacon(Activity activity) {

        BeaconManager.setDebug(true);
        BeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager = BeaconManager.getInstanceForApplication(activity);
        this.activity = activity;
        animatedProgress = new AnimatedProgress(activity);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
// Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:

        mBeaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setForegroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundScanPeriod(scanPeriod);
        mBeaconManager.setForegroundScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundMode(false);
//            mBeaconManager.setBackgroundBetweenScanPeriod(0);
//            mBeaconManager.setBackgroundScanPeriod(1100);
//            mBeaconManager.setForegroundBetweenScanPeriod(0l);
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//            mBeaconManager.se
        arrayList= new ArrayList<>();

    }

    public void setMyBeaconScanner(MyBeaconScanner myBeaconScanner) {
        this.myBeaconScanner = myBeaconScanner;
    }

    public MyBeaconScanner getMyBeaconScanner() {
        return myBeaconScanner;
    }

    public void start()
    {

//    animatedProgress.showProgress();
        region = new Region("all-beacons-region", null, null, null);
        try {
//                Log.w(TAG, "onBeaconServiceConnect try");
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.w(TAG, "onBeaconServiceConnect catch" + e.getMessage());
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
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
        mBeaconManager.setRangeNotifier(this);
        handler();
    }

    public void stop() {
//            arrayList.clear();
        if(region!=null) {
            try {
                mBeaconManager.stopRangingBeaconsInRegion(region);
                mBeaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.w(TAG,"stopping error"+e.toString());
            }
        }
        mBeaconManager.removeAllRangeNotifiers();
        if(handler!=null)
            handler.removeCallbacks(runnable);
//    animatedProgress.hideProgress();



    }

    public void setRequestCode(int request) {
        this.request = request;
    }

    public int getRequest() {
        return request;
    }


//        From the discussion here, and especially this answer, this is the function I currently use:

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.w(TAG, "didRangeBeaconsInRegion" + beacons.size());
        for (Beacon beacon : beacons)
        {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {

                byte[] bytes = beacon.getId1().toByteArray();
                byte ONE = bytes[0];
                Log.w("Byte", ONE + "");
                String receivedString = null;
                receivedString = new String(bytes, 0, bytes.length, StandardCharsets.US_ASCII);

                Log.w(TAG, "I just received: " + receivedString);

                if (receivedString.toLowerCase().contains("tx"))
                {
                    String[] splitUrl = receivedString.split("tx");


                    if (splitUrl.length > 1)
                    {
//                            byte[] encodeId1 = MyBase64.decode(splitUrl[1]);
                        byte[] encodeId1 = MyBase64.decode(splitUrl[1]);
                        ByteQueue byteQueue1 = new ByteQueue(encodeId1);
                        byteQueue1.push(encodeId1);
                        ByteQueue byteQueue2 = new ByteQueue(encodeId1);
                        byteQueue2.push(encodeId1);
                        int methodType=byteQueue1.pop();
                        Log.w("MethodType",methodType+"");
                        if(methodType==0x4f){
//                            if(methodType==0x2f||methodType==0x4f){
//                                long uid=byteQueue2.popU4B();
                            byte[] bytes1=byteQueue1.pop4B();
                            ArrayUtilities.reverse(bytes1);
                            String nodeUid=bytesToHex(bytes1);
//                                String s = "4d0d08ada45f9dde1e99cad9";
                            BigInteger bi = new BigInteger(nodeUid, 16);
                            Log.w("Scann",bi+"");
                            int deriveType=byteQueue1.pop();
                            Log.w("ScanningBeacon",nodeUid+","+deriveType);
//                                Log.w("ScanningBeacon",uid+",");
                            BeconDeviceClass beconDeviceClass=new BeconDeviceClass();
                            beconDeviceClass.setBeaconUID(bi.longValue());
                            beconDeviceClass.setDeviceUid(nodeUid);
                            beconDeviceClass.setDeriveType(deriveType);
                            if(!hasBeacon(beconDeviceClass))
                            {
//                                    Log.w("Has","add");
                                Cursor cursor= AppHelper.sqlHelper.getLightDetails(beconDeviceClass.getBeaconUID());
                                if(cursor!=null && cursor.getCount()>0)
                                {
                                    cursor.moveToFirst();
                                    String beconName=cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME));
                                    Log.w("BeaconName",beconName+",");
                                    beconDeviceClass.setDeviceName(beconName);
                                    beconDeviceClass.setAdded(true);

                                }
                                arrayList.add(beconDeviceClass);
                            }
//                            else if (methodType==50){
//                                byte[] bytes2 = byteQueue1.pop4();
//                                ArrayUtilities.reverse(bytes2);
//                                String nodeUid=bytesToHex(bytes1);
//
//                            }
                            else {
                                Log.w("Has","Not add");
                            }
                        }

//                            else
//                            {
//                                Log.w("ScanningBEacon","OtherType");
//                            }
//                            if (byteQueue.pop() == 0x51)
//                            {
//                                Log.w(TAG, "Status command");
//                                long b = byteQueue.popLSBU4B();
////                                Log.w("Byte1", "" + bytesToHex(b));
//
////                                ArrayUtilities.reverse(b);
////                                Log.w("Byte2", "" + bytesToHex(b));
//                                if (byteQueue.pop() == 0x01)
//                                    Log.w(TAG, "Success");
//                                else
//                                {
//                                    Log.w(TAG, "unSuccess");
//                                }
//                            }



                    }


                }
            }
        }
        if(myBeaconScanner==null)
            return;
        if(arrayList.size()<1)
        {
            myBeaconScanner.noBeaconFound();
            return;
        }
        myBeaconScanner.onBeaconFound(arrayList);

    }

    private void handler() {
        handler = new Handler();

        handler.postDelayed(runnable, SCANNING_TIMEOUT);
    }

    boolean hasBeacon(BeconDeviceClass beconDeviceClass)
    {
        int i=0;
        for(BeconDeviceClass beconDeviceClass1:arrayList)
        {
            if(beconDeviceClass1.getBeaconUID()==beconDeviceClass.getBeaconUID())
            {
                Log.w("Has","hash");
                return true;
            }
            else
                i++;

        }
        return i != arrayList.size();

    }
}



