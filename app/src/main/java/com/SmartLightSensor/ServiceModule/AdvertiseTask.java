package com.SmartLightSensor.ServiceModule;

import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.os.Handler;
import android.util.Log;


import com.SmartLightSensor.EncodeDecodeModule.ArrayUtilities;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.activity.AppHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;

import static org.altbeacon.beacon.BeaconParser.EDDYSTONE_URL_LAYOUT;

public class AdvertiseTask {

    //    public static int ADVERTISE_TIMEOUT=2*1000;
    int advertiseTimeout=0;
    public static int ADVERTISE_TIMEOUT=3*1000;
    private static boolean isAlreadyAdvertising=false;
    String TAG="AdvertiseTask";
    AdvertiseResultInterface advertiseResultInterface;
    ReceiverResultInterface receiverResultInterface;
    ByteQueue byteQueue;
    int searchRequestCode =-1;
    String url="rx";
    Handler handler ;
    boolean isAdvertising=false;
    MyBeaconTransmitter beaconTransmitter;
    Activity activity;
    ScannerTask scannerTask;

    private Runnable runnable= this::stopAdvertising;

    public AdvertiseTask(AdvertiseResultInterface advertiseResultInterface, Activity activity1)
    {
        if(AppHelper.IS_TESTING) {
            ADVERTISE_TIMEOUT=2*1000;
        }
        this.advertiseTimeout=ADVERTISE_TIMEOUT;
        this.advertiseResultInterface=advertiseResultInterface;
        this.activity=activity1;

        byteQueue=new ByteQueue();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(EDDYSTONE_URL_LAYOUT);
        beaconTransmitter = new MyBeaconTransmitter(activity, beaconParser);
        beaconTransmitter.setAdvertiseTimeout(ADVERTISE_TIMEOUT);
        handler();
    }
    public AdvertiseTask(AdvertiseResultInterface advertiseResultInterface, Activity activity1, int advertiseTimeout)
    {
        if(AppHelper.IS_TESTING) {
            ADVERTISE_TIMEOUT=2*1000;
        }

        this.advertiseTimeout=advertiseTimeout;
        this.advertiseResultInterface=advertiseResultInterface;
        this.activity=activity1;

        byteQueue=new ByteQueue();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(EDDYSTONE_URL_LAYOUT);
        beaconTransmitter = new MyBeaconTransmitter(activity, beaconParser);
        beaconTransmitter.setAdvertiseTimeout(this.advertiseTimeout);
        handler();
    }

    public  void setAdvertiseTimeout(int advertiseTimeout) {
        beaconTransmitter.setAdvertiseTimeout(advertiseTimeout);
        if(handler!=null)
            handler.removeCallbacks(runnable);
        handler();
//        this.advertiseTimeout = advertiseTimeout;
    }

    public AdvertiseTask(ReceiverResultInterface receiverResultInterface, AdvertiseResultInterface advertiseResultInterface, Activity activity1)
    {
        if(AppHelper.IS_TESTING) {
            ADVERTISE_TIMEOUT=2*1000;
        }
        this.advertiseTimeout=ADVERTISE_TIMEOUT;
        this.advertiseResultInterface=advertiseResultInterface;
        this.receiverResultInterface=receiverResultInterface;
        this.activity=activity1;

        byteQueue=new ByteQueue();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(EDDYSTONE_URL_LAYOUT);
        beaconTransmitter = new MyBeaconTransmitter(activity, beaconParser);
        beaconTransmitter.setAdvertiseTimeout(ADVERTISE_TIMEOUT);

        handler();
    }

    public void setSearchRequestCode(int searchRequestCode) {
        this.searchRequestCode = searchRequestCode;
    }

    public boolean isAdvertising() {
        return isAlreadyAdvertising;
    }


    public AdvertiseTask(Activity activity)
    {
        this.activity=activity;
        byteQueue=new ByteQueue();
        this.advertiseTimeout=ADVERTISE_TIMEOUT;
        BeaconParser beaconParser = new BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT);
        beaconTransmitter = new MyBeaconTransmitter(activity, beaconParser);
        beaconTransmitter.setAdvertiseTimeout(ADVERTISE_TIMEOUT);
//        beaconTransmitter.setAdvertiseTimeout(this.advertiseTimeout);
        handler();
    }

    public void stopAdvertising()
    {
        isAlreadyAdvertising=false;
        handler.removeCallbacks(runnable);
        Log.w(TAG,"stopping beacon");
        if (beaconTransmitter!=null)
        {
//            beaconTransmitter.setAdvertiseMode();
            beaconTransmitter.stopAdvertising();
//            scannerTask=new ScannerTask(this.activity,receiverResultInterface);
//            if(receiverResultInterface!=null)
//            {
//                ScannerTask scannerTask=new ScannerTask(activity,receiverResultInterface);
//                scannerTask.setRequestCode(searchRequestCode);
//                scannerTask.start();
////                        return;
//            }
            if(advertiseResultInterface!=null)
                advertiseResultInterface.onStop("Stop advertising successfully.", searchRequestCode);
            else
                Log.w(TAG,"Stop advertising successfully.");
            return;
        }
        if(advertiseResultInterface!=null)
        {
            Log.w(TAG,"Failed to stop advertising for null beacon ");
            advertiseResultInterface.onStop("Failed to stop advertising for null beacon .", searchRequestCode);
        }
        else
        {

            Log.w(TAG,"Failed to stop advertising for null beacon.");
        }
    }
    public ByteQueue getByteQueue()
    {
        return  this.byteQueue;
    }

    public void setByteQueue(ByteQueue byteQueue) {
        this.byteQueue = byteQueue;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    public void onDestroy()
    {
        stopAdvertising();
    }

    public void startAdvertising()
    {
//        if(isAlreadyAdvertising)
//            return;

//        handler();
        byte[] allByte=byteQueue.popAll();

        String dataString= MyBase64.encode(allByte);
        String stringToTransmit = (url+""+dataString);
        Log.w(TAG,"url to advertise "+stringToTransmit+ " , "+ ArrayUtilities.toHexString(allByte));

        byte[] stringToTransmitAsAsciiBytes = stringToTransmit.getBytes();
        ByteQueue byteQueue=new ByteQueue();
        byteQueue.push(0x02);
        byteQueue.push(stringToTransmitAsAsciiBytes);
        byte[] mainByte=byteQueue.popAll();
        Beacon beacon = new Beacon.Builder()
                .setId1(Identifier.fromBytes(mainByte, 0, mainByte.length, false).toString())
                .setTxPower(-59).build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                @Override
                public void onStartFailure(int errorCode)
                {
                    isAlreadyAdvertising=false;
                    Log.e(TAG, "Advertisement start failed with code: "+errorCode);
                    if (advertiseResultInterface!=null)
                        advertiseResultInterface.onFailed("Advertisement start failed with code: "+errorCode);
                    else
                        Log.w(TAG,"Advertisement start failed with code: "+errorCode);
                }

                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect)
                {
                    isAlreadyAdvertising=true;
                    Log.w(TAG, "Advertisement start succeeded."+settingsInEffect.toString());
                    if (advertiseResultInterface!=null)
                        advertiseResultInterface.onSuccess("Advertisement start succeeded."+settingsInEffect.toString());
                    else
                        Log.w(TAG,"Advertisement start succeeded."+settingsInEffect.toString());

                }
            });
        }
        else
        {
            beaconTransmitter.startAdvertising(beacon);
            advertiseResultInterface.onSuccess("Advertisement start succeeded.");
        }
    }

    private void handler()
    {
        handler = new Handler();

        handler.postDelayed(runnable, this.advertiseTimeout);
    }
}
