package com.SmartLightSensor.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.MyBeaconScanner;
import com.SmartLightSensor.PogoClasses.BeconDeviceClass;
import com.SmartLightSensor.R;
import com.SmartLightSensor.ServiceModule.AdvertiseTask;
import com.SmartLightSensor.ServiceModule.ScanningBeacon;
import com.SmartLightSensor.activity.AppHelper;
import com.SmartLightSensor.adapter.ScanDeviceAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.LIGHT_INFO;

public class ScanDeviceFragment extends Fragment implements MyBeaconScanner, AdvertiseResultInterface {
    @BindView(R.id.device_list)
    ListView deviceList;
    @BindView(R.id.refresh)
    Button refresh;
    ScanDeviceAdapter scanDeviceAdapter;
    ArrayAdapter<CharSequence> adapter;
    int movement=150;
    ScanningBeacon scanningBeacon;
    boolean isAdvertisingFinished=false;
    AdvertiseTask advertiseTask;
    AnimatedProgress animatedProgress;
    String TAG=this.getClass().getSimpleName();
    Unbinder unbinder;
    Activity activity;

    Handler handler ;
    private Runnable runnable= () -> {
        if(animatedProgress!=null)
        {
            animatedProgress.hideProgress();
        }

    };
    public ScanDeviceFragment() {
        // Required empty public constructor
    }

    private void handlerProgressar() {
        animatedProgress.showProgress();
        handler = new Handler();
        handler.postDelayed(runnable,15*1000);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scan_device, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity = getActivity();

        scanDeviceAdapter=new ScanDeviceAdapter(activity);
        deviceList.setAdapter(scanDeviceAdapter);
        scanningBeacon=new ScanningBeacon(activity);
        scanningBeacon.setMyBeaconScanner(this);

        animatedProgress=new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);

//        Toast.makeText(getActivity(),  PreferencesManager.getInstance(activity).getUniqueKey(), Toast.LENGTH_SHORT).show();

        ByteQueue byteQueue=new ByteQueue();
        byteQueue.push(LIGHT_INFO);   //// Light Level Command method type
        byteQueue.pushU4B(0x00);
        byteQueue.push(0x00);////deviceDetail.getGroupId()   node id;
        advertiseTask=new AdvertiseTask(this,activity,5*1000);
        animatedProgress.setText("Advertising");
        advertiseTask.setByteQueue(byteQueue);
//        advertiseTask.setAdvertiseTimeout(10*1000);
        advertiseTask.setSearchRequestCode(LIGHT_INFO);
        advertiseTask.startAdvertising();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDeviceAdapter.clearList();
                ByteQueue byteQueue1 = new ByteQueue();
                byteQueue1.push(LIGHT_INFO);
                byteQueue1.pushU4B(0x00);
                byteQueue1.push(0x00);////deviceDetail.getGroupId()   node id;
                animatedProgress.setText("Refreshing");
                advertiseTask.setByteQueue(byteQueue1);
                advertiseTask.setAdvertiseTimeout(2*1000);
                advertiseTask.setSearchRequestCode(LIGHT_INFO);
                advertiseTask.startAdvertising();
//                scanningBeacon.start();


            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAdvertisingFinished)
        {
            animatedProgress.setText("Scanning Sensor");
            scanningBeacon.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scanningBeacon.stop();
        if(handler!=null)
            handler.removeCallbacks(runnable);
        unbinder.unbind();
    }

    @Override
    public void onSuccess(String message) {
        handlerProgressar();
        Log.e(TAG,"Advertising start");

    }

    @Override
    public void onFailed(String errorMessage) {
        isAdvertisingFinished=true;
        scanningBeacon.start();
        animatedProgress.setText("Scanning Sensor");

    }

    @Override
    public void onStop() {
        scanningBeacon.stop();
        super.onStop();
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
        isAdvertisingFinished=true;
        animatedProgress.setText("Scanning Sensor");
        scanningBeacon.start();

    }

    @Override
    public void onBeaconFound(ArrayList<BeconDeviceClass> beaconList) {
        if(scanDeviceAdapter==null)
            scanDeviceAdapter=new ScanDeviceAdapter(activity);
        scanDeviceAdapter.setArrayList(beaconList);

    }

    @Override
    public void noBeaconFound() {
        Log.w("AddDeviceFragment","No Beacon found");
        if(!AppHelper.IS_TESTING)
            scanDeviceAdapter.clearList();

    }

}
