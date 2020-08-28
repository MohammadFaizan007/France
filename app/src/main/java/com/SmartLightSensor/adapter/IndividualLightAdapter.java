package com.SmartLightSensor.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.EncodeDecodeModule.ArrayUtilities;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.RxMethodType;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.R;
import com.SmartLightSensor.ServiceModule.AdvertiseTask;
import com.SmartLightSensor.ServiceModule.ScannerTask;
import com.SmartLightSensor.activity.AppHelper;
import com.SmartLightSensor.activity.HelperActivity;
import com.SmartLightSensor.constant.Constants;
import com.niftymodaldialogeffects.Effectstype;
import com.niftymodaldialogeffects.NiftyDialogBuilder;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_STATUS;
import static com.SmartLightSensor.EncodeDecodeModule.ArrayUtilities.bytesToHex;
import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.LIGHT_LEVEL_COMMAND;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_STATE_COMMAND_RESPONSE;

public class IndividualLightAdapter extends BaseAdapter implements AdvertiseResultInterface, ReceiverResultInterface {
    Activity activity;
    ArrayList<DeviceClass> arrayList;
    String TAG=this.getClass().getSimpleName();
    int requestCode;
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int seekBarProgress=0;
    int selectedPosition=0;
    boolean advertise=true;

    public IndividualLightAdapter(@NonNull Activity context) {
        activity = context;
        arrayList = new ArrayList<>();
        scannerTask=new ScannerTask(activity,this);
        animatedProgress=new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);

    }

    public void setList(List<DeviceClass> arrayList1) {
        arrayList.clear();

        arrayList.addAll(arrayList1);

        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public DeviceClass getItem(int position) {
        if (arrayList.size() <= position)
            return null;
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) { {
        convertView = LayoutInflater.from(activity).
                inflate(R.layout.indivisual_light_adapter, parent, false);
    }
        ViewHolder viewHolder = new ViewHolder(convertView);
        DeviceClass deviceClass=arrayList.get(position);
        viewHolder.dashboardDeviceName.setText(deviceClass.getDeviceName());
        viewHolder.lightDetails.setBackground(activity.getResources().getDrawable(deviceClass.getMasterStatus()==0?R.drawable.white_circle_border:R.drawable.yellow_circle));
        viewHolder.lightDetails.setOnClickListener(v -> {
            Intent intent = new Intent(activity, HelperActivity.class);
            intent.putExtra(Constants.MAIN_KEY, Constants.EDIT_LIGHT);
            intent.putExtra(Constants.LIGHT_DETAIL_KEY,deviceClass);
            intent.putExtra("Type","light");
            activity.startActivity(intent);
        });
//
//        viewHolder.details.setOnClickListener(v -> {
//            Intent intent = new Intent(activity, HelperActivity.class);
//            intent.putExtra(Constants.MAIN_KEY, Constants.READ_DETAILS);
//            intent.putExtra(Constants.LIGHT_DETAIL_KEY,deviceClass);
//            activity.startActivity(intent);
//        });

        viewHolder.customise.setOnClickListener(view -> showDialog(position));
        viewHolder.statusSwitch.setChecked(deviceClass.getStatus());
        viewHolder.statusSwitch.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb)
            {

                boolean switchStatus= state != State.LEFT;
                if(deviceClass.getStatus()==switchStatus)
                {
//                    Log.w("Advertise","state is same");
                    return;
                }
                Log.w("DeviceID",deviceClass.getDeviceUID()+"");
                requestCode= RxMethodType.LIGHT_STATE_COMMAND;
                AdvertiseTask advertiseTask;
                ByteQueue byteQueue=new ByteQueue();
                byteQueue.push(requestCode);       ////State Command
                byteQueue.pushU4B(deviceClass.getDeviceUID());      ////  12 is static vale for Node id
//                byteQueue.push(0x00);                                    ///0x00 – OFF    0x01 – ON
//                scannerTask.setRequestCode(TxMethodType.LIGHT_STATE_COMMAND_RESPONSE);
                Log.w(TAG,state+"");
                switch (state)
                {
                    case LEFT:
                        //// remove group method type
//                        byteQueue.pushS4B(12);
//                        Log.w("SwitchStatus","Left");
                        byteQueue.push(0x00);   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(false);

                        break;
                    case RIGHT:
//                        Log.w("SwitchStatus","Right");
                        byteQueue.push(0x01 );   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(true);
                        break;
                    case LEFT_TO_RIGHT:

                        return;

                    case RIGHT_TO_LEFT:
                        return;

                }
                selectedPosition=position;
                advertiseTask=new AdvertiseTask(IndividualLightAdapter.this,activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
            }
        });
//        Picasso.with(activity).load(IMAGE_URL + friendsDetails.getUserImage()).placeholder(R.drawable.ic_user_male_icon_2).error(R.drawable.ic_user_male_icon_2).into(viewHolder.friendsProfile);


        return convertView;
    }

    void showDialog( int index){

        final Dialog dialog = new Dialog(activity);
        DeviceClass deviceDetail=arrayList.get(index);
        final int[] seekBarProgress = {deviceDetail.getDeviceDimming()};
        this.seekBarProgress=seekBarProgress[0];
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customize_group);
        TextView levelPercentage=dialog.findViewById(R.id.level_percentage);
        TextView deviceName=dialog.findViewById(R.id.customize_group_name);
        SeekBar seekBar=dialog.findViewById(R.id.customizeGroupSeekBar);
        Button button=dialog.findViewById(R.id.customiseGroupSave);
        levelPercentage.setText(deviceDetail.getDeviceDimming()+" %");
        seekBar.setProgress(deviceDetail.getDeviceDimming());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarProgress[0] =i;
                levelPercentage.setText(i+" %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.w("IndividualLight",seekBarProgress[0]+"");
//            ContentValues contentValues=new ContentValues();
//            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
//            contentValues.put(COLUMN_DEVICE_PROGRESS,seekBarProgress[0]);
                String hex = Integer.toHexString(seekBarProgress[0]);
                Log.w("IndividualLight",hex+" "+String.format("%02X", seekBarProgress[0]));
                AdvertiseTask advertiseTask;
                selectedPosition=index;
                IndividualLightAdapter.this.seekBarProgress=seekBarProgress[0];
                requestCode=LIGHT_LEVEL_COMMAND;
                ByteQueue byteQueue=new ByteQueue();
                byteQueue.push(requestCode);   //// Light Level Command method type
                byteQueue.pushU4B(deviceDetail.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
                byteQueue.push(seekBarProgress[0]);    ////0x00-0x64
//            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
                advertiseTask=new AdvertiseTask(IndividualLightAdapter.this,activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();

            }
        });
//        button.setOnClickListener(view -> {
//            Log.w("IndividualLight",seekBarProgress[0]+"");
////            ContentValues contentValues=new ContentValues();
////            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
////            contentValues.put(COLUMN_DEVICE_PROGRESS,seekBarProgress[0]);
//            String hex = Integer.toHexString(seekBarProgress[0]);
//            Log.w("IndividualLight",hex+" "+String.format("%02X", seekBarProgress[0]));
//            AdvertiseTask advertiseTask;
//            selectedPosition=index;
//            this.seekBarProgress=seekBarProgress[0];
//            requestCode=LIGHT_LEVEL_COMMAND;
//            ByteQueue byteQueue=new ByteQueue();
//            byteQueue.push(requestCode);   //// Light Level Command method type
//            byteQueue.pushU4B(deviceDetail.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
//            byteQueue.push(seekBarProgress[0]);    ////0x00-0x64
////            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
//            advertiseTask=new AdvertiseTask(this,this,activity);
//            advertiseTask.setByteQueue(byteQueue);
//            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
//            advertiseTask.startAdvertising();
////            Log.w("IndividualLight",AppHelper.sqlHelper.updateDevice(deviceDetail.getDeviceUID(),contentValues)+"");
//            dialog.dismiss();
//        });
        deviceName.setText(deviceDetail.getDeviceName());

        dialog.show();

    }
    @Override
    public void onSuccess(String message) {
//        advertise=false;
        animatedProgress.showProgress();
        Log.w(TAG,"Advertising start");
    }

    @Override
    public void onFailed(String errorMessage) {
//        advertise=true;
//        arrayList.get(selectedPosition).setStatus();
        if(animatedProgress==null)
            return;
        Toast.makeText(activity, "Advertising failed.", Toast.LENGTH_SHORT).show();
        animatedProgress.hideProgress();
        Log.w(TAG,"onScanFailed "+errorMessage);
    }

    @Override
    public void onStop(String stopMessage, int resultCode)
    {
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
        if(animatedProgress!=null)
            animatedProgress.hideProgress();
        ContentValues contentValues=new ContentValues();
        switch (resultCode)
        {
            case LIGHT_STATE_COMMAND_RESPONSE:

                boolean status1=!arrayList.get(selectedPosition).getStatus();
                contentValues.put(COLUMN_DEVICE_STATUS, status1?1:0);
                contentValues.put(COLUMN_DEVICE_PROGRESS, status1?100:0);
//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                arrayList.get(selectedPosition).setStatus(status1);

                arrayList.get(selectedPosition).setDeviceDimming(status1?100:0);
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + ","+status1);
//                    notifyDataSetChanged();

                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:

                contentValues.put(COLUMN_DEVICE_PROGRESS,this.seekBarProgress);
                contentValues.put(COLUMN_DEVICE_STATUS,1);
                arrayList.get(selectedPosition).setDeviceDimming(this.seekBarProgress);
                arrayList.get(selectedPosition).setStatus(true);

                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + "");
                notifyDataSetChanged();
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

                break;
        }

        Log.w(TAG,"Advertising stop");
    }

    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        advertise=true;
        if(animatedProgress==null)
            return;
        animatedProgress.hideProgress();
        ContentValues contentValues=new ContentValues();



        switch (successCode)
        {
            case LIGHT_STATE_COMMAND_RESPONSE:
                Log.w("MethodType",(int)byteQueue.pop()+"");
                byte[] bytes1=byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                String nodeUid=bytesToHex(bytes1);
                int status=byteQueue.pop();
                Log.w("ScanningBeacon",nodeUid);
//                                String s = "4d0d08ada45f9dde1e99cad9";
                BigInteger bi = new BigInteger(nodeUid, 16);
                Log.w("Scann",bi+","+status);
                if(status==0)
                {
                    boolean status1=!arrayList.get(selectedPosition).getStatus();
                    contentValues.put(COLUMN_DEVICE_STATUS, status1?1:0);
                    contentValues.put(COLUMN_DEVICE_PROGRESS, status1?100:0);
//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                    arrayList.get(selectedPosition).setStatus(status1);
                    arrayList.get(selectedPosition).setDeviceDimming(status1?100:0);
                    Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + ","+status1);
//                    notifyDataSetChanged();
                }
                else
                {
//                    arrayList.get(selectedPosition).setStatus();
                    notifyDataSetChanged();
                    Toast.makeText(activity, "State command failed.", Toast.LENGTH_SHORT).show();
                }
                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:
                Log.w("MethodType",(int)byteQueue.pop()+"");
                int status2=byteQueue.pop();
                Log.w("Scann",","+status2);
                if(status2==0) {
                    contentValues.put(COLUMN_DEVICE_PROGRESS,this.seekBarProgress);
                    contentValues.put(COLUMN_DEVICE_STATUS,1);
                    arrayList.get(selectedPosition).setDeviceDimming(this.seekBarProgress);
                    arrayList.get(selectedPosition).setStatus(true);

                    Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + "");
                    notifyDataSetChanged();
                }
                else
                {
                    Toast.makeText(activity, "Cannot set dimming.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    @Override
    public void onScanFailed(int errorCode) {
        if(animatedProgress==null)
            return;
        advertise=true;
        animatedProgress.hideProgress();
        notifyDataSetChanged();
        NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(activity);
        dialogBuilder
                .withTitle("Timeout")
                .withEffect(Effectstype.Newspager)
                .withMessage("Timeout,Please check your beacon is in range")
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
//        Toast.makeText(activity, "Cannot get response from beacon ,make sure your beacon is in range ", Toast.LENGTH_SHORT).show();
        Log.w("StartFailed",errorCode+"");
    }

    static class ViewHolder {

        @BindView(R.id.dashboard_deviceName)
        TextView dashboardDeviceName;

        @BindView(R.id.individual_customize)
        Button customise;
        @BindView(R.id.details)
        Button details;

        @BindView(R.id.status_switch)
        JellyToggleButton statusSwitch;

        @BindView(R.id.light_details)
        ImageView lightDetails;

        ViewHolder(View view) {

            ButterKnife.bind(this, view);

        }

    }
}


