package com.SmartLightSensor.ServiceModule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.AsynchStorage.AdvertiseBeacon;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.MyBase64;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

public class ScannerService extends Service implements BeaconConsumer, RangeNotifier {

    /** indicates how to behave if the service is killed */
    int mStartMode= START_STICKY;;

    String TAG="ScannerService";
    /** interface for clients that bind */
    IBinder mBinder;

    int scanPeriod=200;
//    int scanPeriod=5*1000;
    int request=0x4f;
    AnimatedProgress animatedProgress;
    ReceiverResultInterface receiverResultInterface;
    /** indicates whether onRebind should be used */
    boolean mAllowRebind;
    private BeaconManager mBeaconManager;
    /** Called when the service is being created. */
    @Override
    public void onCreate() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("x,m:0-1=da03,i:0-1,d:2-3,d:4-5,d:6-7,d:8-9,d:9-10"));
      // Detect the main identifier (UID) frame:
//        mBeaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
//// Detect the telemetry (TLM) frame:
//        mBeaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
//      // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
//        mBeaconManager.setForegroundScanPeriod(200);
//        mBeaconManager.startRangingBeaconsInRegion(region);
        mBeaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setForegroundBetweenScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundScanPeriod(scanPeriod);
        mBeaconManager.setForegroundScanPeriod(scanPeriod);
        mBeaconManager.setBackgroundMode(false);



        mBeaconManager.bind(this);
    }
     public ScannerService()
    {}
    public ScannerService(ReceiverResultInterface receiverResultInterface, int requestCode)
    {
        this.request=requestCode;
        this.receiverResultInterface=receiverResultInterface;
    }
public void setReceiverResultInterface(ReceiverResultInterface receiverResultInterface)
{
    this.receiverResultInterface=receiverResultInterface;
}

    public void setRequest(int request) {
        this.request = request;
    }

    public int getRequest() {
        return request;
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy()
    {
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect()
    {
        Log.w(TAG,"onBeaconServiceConnect");
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            Log.w(TAG,"onBeaconServiceConnect try");

            mBeaconManager.setBackgroundBetweenScanPeriod(scanPeriod);
            mBeaconManager.setForegroundBetweenScanPeriod(scanPeriod);
            mBeaconManager.setBackgroundScanPeriod(scanPeriod);
            mBeaconManager.setForegroundScanPeriod(scanPeriod);
            mBeaconManager.setBackgroundMode(false);
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e)
        {
            Log.w(TAG,"onBeaconServiceConnect catch"+e.getMessage());
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }


    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.e("All=====>", "didRangeBeaconsInRegion12asf" + beacons.size());
        for (Beacon beacon : beacons)
        {
            Log.w(TAG, beacon.getId1() + "" + beacon.toString() + beacon.getDataFields().toString());
            Log.w(TAG, "didRangeBeaconsInRegion12" + beacon.getServiceUuid());


//            Log.w("nbytes",(byteQueue.pop())+","+(byteQueue.pop4B())+","+byteQueue.pop());
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10)
            {
                // This is a Eddystone-URL frame
//                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
//                String[] splitUrl=url.split("#");
                byte[] bytes = beacon.getId1().toByteArray();
                String receivedString = null;
                try {
                    receivedString = new String(bytes, 0, bytes.length, "ASCII");
                } catch (UnsupportedEncodingException e) {
                    Log.w(TAG, "Cannot decode ASCII");
                }

                Log.w(TAG, "I just received: " + receivedString);
                String[] splitUrl = receivedString.split("#");

//                String url_string = "http://inf.rx#" + splitUrl[1];
//                transection(url_string);

//                Log.w("URL",splitUrl[1]);
//                if(splitUrl[0].equals("http://inf.tx") || splitUrl[0].equals("http://inf.rx"))
//                {
//                    Log.w(TAG,"Url is valid");
//                }


                ByteQueue byteQueue = new ByteQueue();
                if (splitUrl.length > 1) {
                    byte[] encodeId1 = MyBase64.decode(splitUrl[1]);
                    byteQueue.push(encodeId1);

                    //****************  ALL RESPONSE METHOD OF EVERY TRANSACTION ***************/

                    byte byte1=byteQueue.pop();
                    switch (byte1)
                    {
                        case 0x4f:   ///Info
                            Log.w("Byte 0x4f", byte1 + "");
                            int deriverType = byteQueue.pop();
                            long nodeID = byteQueue.popU4B();
                            int groupId = byteQueue.pop();
                            break;
                        case 0x50:  ///State
                            Log.w("Byte 0x50", byte1 + "");
                            long node_id50 = byteQueue.popU4B();
                            int status50 = byteQueue.pop();
                            break;
                        case 0x51:  ////State Command
                            Log.w("Byte 0x51", byte1 + "");
                            long node_id51 = byteQueue.popU4B();
                            int status51 = byteQueue.pop();

                            break;
                        case 0x52:  ///State Group
                            Log.w("Byte 0x52", byte1 + "");
                            int group_id52 = byteQueue.pop();
                            int status52 = byteQueue.pop();
                            break;
                        case 0x53:////State Group Command
                            Log.w("Byte 0x53", byte1 + "");
                            int group_id53 = byteQueue.pop();
                            int status53 = byteQueue.pop();
                            break;
                        case 0x54:    /////Group
                            Log.w("Byte 0x54", byte1 + "");
                            long node_id54 = byteQueue.popU4B();
                            int group_id54 = byteQueue.pop();
                            break;
                        case 0x55:   ///Add Group
                            Log.w("Byte 0x55", byte1 + "");
                            int status55 = byteQueue.pop(); //// add in group
                            break;
                        case 0x56:    //Remove Group
                            Log.w("Byte 0x56", byte1 + "");
                            int status56 = byteQueue.pop(); //// remove from group
                            break;
                        case 0x57:   ////Update Group
                            Log.w("Byte 0x57", byte1 + "");
                            int status57 = byteQueue.pop(); //// update group
                            break;
                        case 0x58:   ///Light Level
                            Log.w("Byte 0x58", byte1 + "");////Light Level
                            long node_id58 = byteQueue.popU4B();
                            int status58 = byteQueue.pop();
                            break;
                        case 0x59:    ////Light Level Command
                            Log.w("Byte 0x59", byte1 + "");
                            int status59 = byteQueue.pop(); //// Light Level Command
                            break;
                        case 0x5a://///Light Level Group
                            Log.w("Byte 0x5a", byte1 + ""); ////Light Level Group
                            int group_id5a = byteQueue.pop();
                            int status5a = byteQueue.pop();
                            break;
                        case 0x5b:   ////Light Level Group Command
                            int group_id5b = byteQueue.pop();
                            int status5b = byteQueue.pop();
                            Log.w("Byte 0x5b", byte1 + "");
                            break;

                    }
//                    if (byteQueue.pop() == 0x01)
//                        Log.w("Byte first", "First");
//                    Log.w("Byte 2-5", byteQueue.popU4B() + "");
//                    Log.w("Byte 6", byteQueue.pop() + "");
//            String encodeId1_3=Base64Encode.encode(beacon.getId1().toUuid()+"");
//                    Log.w(TAG, "Encoding" + encodeId1);
//                    encoded123: =010000000C00
                    //encoded: =tx#MDEwMDAwMDAwQzAw
                }
//                Log.w(TAG, "I see a beacon transmitting a url: " + url +
//                        " approximately " + beacon.getDistance() + " meters away.");
            }


        }
    }

    public void transection(String url_string)
        {

            AdvertiseBeacon advertiseBeacon=new AdvertiseBeacon();
            advertiseBeacon.execute(url_string);
        }

}
