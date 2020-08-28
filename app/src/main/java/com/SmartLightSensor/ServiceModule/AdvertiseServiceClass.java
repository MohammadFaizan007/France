package com.SmartLightSensor.ServiceModule;

import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;

import java.nio.charset.StandardCharsets;

import static com.SmartLightSensor.ServiceModule.ScanningBeacon.EDDYSTONE_URL_LAYOUT;


public class AdvertiseServiceClass extends Service {

    /** indicates how to behave if the service is killed */
    int mStartMode= START_STICKY;;

    String TAG="ScannerService";
    /** interface for clients that bind */
    IBinder mBinder;
    AdvertiseResultInterface advertiseResultInterface;
    ByteQueue byteQueue;
    String url="rx";
    BeaconTransmitter beaconTransmitter;

    public AdvertiseServiceClass(AdvertiseResultInterface advertiseResultInterface)
    {
        this.advertiseResultInterface=advertiseResultInterface;
        byteQueue=new ByteQueue();
    }

    public void stopAdvertising()
    {
        if (beaconTransmitter!=null)
            beaconTransmitter.stopAdvertising();
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

    @Override
    public void onCreate() {
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(EDDYSTONE_URL_LAYOUT);
         beaconTransmitter = new BeaconTransmitter(this, beaconParser);
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }



    /** Called when all clients have unbound with unbindService() */
//    @Override
//    public boolean onUnbind(Intent intent) {
//        return mAllowRebind;
//    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy()
    {

        stopAdvertising();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startBeacon()
    {
        byte[] allByte=byteQueue.popAll();
        String dataString= MyBase64.encode(allByte);
        String stringToTransmit = (url+"#"+dataString);
        Log.w("encoded","="+stringToTransmit);

        byte[] stringToTransmitAsAsciiBytes = stringToTransmit.getBytes(StandardCharsets.US_ASCII);
        Beacon beacon = new Beacon.Builder()
                .setId1(Identifier.fromBytes(stringToTransmitAsAsciiBytes, 0, stringToTransmitAsAsciiBytes.length, false).toString())
                .setTxPower(-59).build();

        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {

                Log.e("StartFailed", "Advertisement start failed with code: "+errorCode);
                if (advertiseResultInterface!=null)
                    advertiseResultInterface.onFailed("Advertisement start failed with code: "+errorCode);

            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.w("StartSuccess", "Advertisement start succeeded."+settingsInEffect.toString());
                if (advertiseResultInterface!=null)
                    advertiseResultInterface.onSuccess("Advertisement start succeeded."+settingsInEffect.toString());

            }
        });
    }
}
