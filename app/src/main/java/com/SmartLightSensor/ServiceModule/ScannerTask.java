package com.SmartLightSensor.ServiceModule;

import android.app.Activity;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;


import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.activity.AppHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ScannerTask implements RangeNotifier
{ /** indicates whether onRebind should be used */
boolean mAllowRebind;
    public static final int SCAN_SUCCESS_CODE =200;
    public static final int SCAN_FAIL_CODE =201;
    //    public static  int SCANNING_TIMEOUT=5*1000;
    public static  int SCANNING_TIMEOUT=3*1000;
    public static final String EDDYSTONE_URL_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v";
    private BeaconManager mBeaconManager;
    Activity activity;
    String TAG="ScannerTask";
    //    int scanPeriod=1*1000;     /// 200 m
    int scanPeriod=200;
    int request=0x4f;
    int resultCode=SCAN_FAIL_CODE;
    String url="rx";
    Handler handler ;
    ByteQueue byteQueue;
    AnimatedProgress animatedProgress;
    ReceiverResultInterface receiverResultInterface;
    Region region;
    private Runnable runnable= this::stop;

    public ScannerTask(Activity activity, ReceiverResultInterface receiverResultInterface)
    {
        if(AppHelper.IS_TESTING) {
            SCANNING_TIMEOUT=2*1000;
        }
        BeaconManager.setDebug(true);
        BeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager = BeaconManager.getInstanceForApplication(activity);

        this.activity=activity;
        this.receiverResultInterface=receiverResultInterface;
        animatedProgress=new AnimatedProgress(activity);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
// Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // set the duration of the scan to be 1.1 seconds
//        mBeaconManager.setBackgroundScanPeriod(1100l);
// set the time between each scan to be 1 hour (3600 seconds)
//        mBeaconManager.setBackgroundBetweenScanPeriod(3600000l);
//
        mBeaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setForegroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundScanPeriod(scanPeriod);
        mBeaconManager.setForegroundScanPeriod(scanPeriod);
//        mBeaconManager.setBackgroundBetweenScanPeriod(0);
//        mBeaconManager.setBackgroundScanPeriod(1100);
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mBeaconManager.setBackgroundMode(false);
//        mBeaconManager.syncSettingsToService();

//        mBeaconManager.bind(this);

    }
    public void start()
    {
//    animatedProgress.showProgress();
        region = new Region("all-beacons-region", null, null, null);
        try {
            Log.w(TAG,"onBeaconServiceConnect try");
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e)
        {
            Log.w(TAG,"onBeaconServiceConnect catch"+e.getMessage());
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
        handler();
    }

    public void stop()
    {
        Log.w(TAG,"stopping"+resultCode);
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

        handler.removeCallbacks(runnable);
//    animatedProgress.hideProgress();

        switch (resultCode)
        {
            case SCAN_SUCCESS_CODE:
                if(byteQueue.size()==0)
                {
                    Log.w(TAG,"Bytequeue is null.");
                    receiverResultInterface.onScanFailed(SCAN_FAIL_CODE);
                }
                else
                    receiverResultInterface.onScanSuccess(request,byteQueue);
                break;
            case SCAN_FAIL_CODE:
                receiverResultInterface.onScanFailed(SCAN_FAIL_CODE);
        }


    }
    public void setRequestCode(int request)
    {
        this.request = request;
    }

    public int getRequest()
    {
        return request;
    }



    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.w(TAG, "didRangeBeaconsInRegion" + beacons.size());
        for (Beacon beacon : beacons)
        {
            Log.w(TAG, beacon.getId1() + "" + beacon.toString() + beacon.getDataFields().toString());
            Log.i(TAG, "didRangeBeaconsInRegion12" + beacon.getServiceUuid());


//            Log.w("nbytes",(byteQueue.pop())+","+(byteQueue.pop4B())+","+byteQueue.pop());
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10)
            {
                byte[] bytes = beacon.getId1().toByteArray();
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

                        int methodType=byteQueue1.pop();
                        Log.w("MethodType",methodType+","+request);
                        if(methodType==request)
                        {
//                                long uid=byteQueue2.popU4B();
                            this.resultCode=SCAN_SUCCESS_CODE;
                            this.byteQueue=new ByteQueue(encodeId1);
                            // stop();
                            break;
                        }
                        else
                        {
                            Log.w("ScanningTask","OtherType");
                        }


                    }

                }}}}

    private void handler()
    {
        handler = new Handler();

        handler.postDelayed(runnable,SCANNING_TIMEOUT);
    }
}
