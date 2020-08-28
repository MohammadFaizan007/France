package com.SmartLightSensor.ServiceModule;

import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;

import java.nio.charset.StandardCharsets;

import static org.altbeacon.beacon.BeaconParser.EDDYSTONE_URL_LAYOUT;

public class ReceiverTask {


    String TAG="ReceiverTask";

    AdvertiseResultInterface advertiseResultInterface;
    ByteQueue byteQueue;
    String url="tx";
    BeaconTransmitter beaconTransmitter;
    Activity activity;

    public ReceiverTask(AdvertiseResultInterface advertiseResultInterface)
    {
        this.advertiseResultInterface=advertiseResultInterface;
        this.activity=(Activity)advertiseResultInterface;
        byteQueue=new ByteQueue();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(EDDYSTONE_URL_LAYOUT);
        beaconTransmitter = new BeaconTransmitter(activity, beaconParser);

    }

    public void stopAdvertising()
    {
        if (beaconTransmitter!=null)
        {
            beaconTransmitter.stopAdvertising();
            advertiseResultInterface.onStop("Stop advertising successfully.",1);
            return;
        }
        advertiseResultInterface.onStop("Failed to stop advertising for null beacon .",0);
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




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising()
    {
        byte[] allByte=byteQueue.popAll();
        String dataString= MyBase64.encode(allByte);
        String stringToTransmit = (url+"#"+dataString);
        Log.w(TAG,"url to advertise ="+stringToTransmit);

        byte[] stringToTransmitAsAsciiBytes = stringToTransmit.getBytes(StandardCharsets.US_ASCII);
        Beacon beacon = new Beacon.Builder()
                .setId1(Identifier.fromBytes(stringToTransmitAsAsciiBytes, 0, stringToTransmitAsAsciiBytes.length, false).toString())
                .setTxPower(-59).build();

        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {

                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
                if (advertiseResultInterface!=null)
                    advertiseResultInterface.onFailed("Advertisement start failed with code: "+errorCode);

            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.w(TAG, "Advertisement start succeeded."+settingsInEffect.toString());
                if (advertiseResultInterface!=null)
                    advertiseResultInterface.onSuccess("Advertisement start succeeded."+settingsInEffect.toString());

            }
        });
    }
}
