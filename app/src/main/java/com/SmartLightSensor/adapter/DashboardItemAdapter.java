package com.SmartLightSensor.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupDetailsClass;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_STATUS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_STATUS;
import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.GROUP_STATE_COMMAND;
import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.LIGHT_LEVEL_GROUP_COMMAND;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.GROUP_STATE_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_GROUP_COMMAND_RESPONSE;
import static com.SmartLightSensor.activity.AppHelper.sqlHelper;


public class DashboardItemAdapter extends BaseAdapter implements AdvertiseResultInterface, ReceiverResultInterface {
    Activity activity;
    ArrayList<GroupDetailsClass> arrayList;
    int requestCode;
    int requestCode2=0;
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int seekBarProgress=0;
    int selectedPosition=0;
    String TAG=this.getClass().getSimpleName();

    public DashboardItemAdapter(@NonNull Activity context) {
        activity = context;
        arrayList = new ArrayList<>();
        scannerTask=new ScannerTask(context,this);
        animatedProgress=new AnimatedProgress(activity);

    }

    public void setList(List<GroupDetailsClass> arrayList1) {
        arrayList.clear();

        arrayList.addAll(arrayList1);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public GroupDetailsClass getItem(int position) {
        if (arrayList.size() <= position)
            return null;
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    void showDialog( int index){
        final Dialog dialog = new Dialog(activity);
        GroupDetailsClass deviceDetail=arrayList.get(index);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customize_group);
        TextView deviceName=dialog.findViewById(R.id.customize_group_name);
        TextView levelPercentage=dialog.findViewById(R.id.level_percentage);
        SeekBar seekBar=dialog.findViewById(R.id.customizeGroupSeekBar);
        Button button=dialog.findViewById(R.id.customiseGroupSave);
        seekBarProgress=deviceDetail.getGroupDimming();
        seekBar.setProgress(deviceDetail.getGroupDimming());
        levelPercentage.setText(seekBarProgress+" %");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarProgress =i;
                levelPercentage.setText(seekBarProgress+" %");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.w(TAG,seekBarProgress+"");
                selectedPosition=index;
//            arrayList.get(index).setGroupDimming(seekBarProgress);

                String hex = Integer.toHexString(seekBarProgress);
                Log.w(TAG,hex+" "+String.format("%02X", seekBarProgress));
                AdvertiseTask advertiseTask;
                requestCode=LIGHT_LEVEL_GROUP_COMMAND;
                ByteQueue byteQueue=new ByteQueue();
                byteQueue.push(requestCode);   //// Light Level Command method type
                byteQueue.push(arrayList.get(index).getGroupId());   ////deviceDetail.getGroupId()   node id;
                byteQueue.push(seekBarProgress);    ////0x00-0x64
                byteQueue.pushU3B(0x00);
                advertiseTask=new AdvertiseTask(DashboardItemAdapter.this,activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_LEVEL_GROUP_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();

            }
        });

//        button.setOnClickListener(view ->
//        {
//            Log.w(TAG,seekBarProgress+"");
//            selectedPosition=index;
////            arrayList.get(index).setGroupDimming(seekBarProgress);
//
//            String hex = Integer.toHexString(seekBarProgress);
//            Log.w(TAG,hex+" "+String.format("%02X", seekBarProgress));
//            AdvertiseTask advertiseTask;
//            requestCode=LIGHT_LEVEL_GROUP_COMMAND;
//            ByteQueue byteQueue=new ByteQueue();
//            byteQueue.push(requestCode);   //// Light Level Command method type
//            byteQueue.push(arrayList.get(index).getGroupId());   ////deviceDetail.getGroupId()   node id;
//            byteQueue.push(seekBarProgress);    ////0x00-0x64
//            byteQueue.pushU3B(0x00);
//            advertiseTask=new AdvertiseTask(this,activity);
//            advertiseTask.setByteQueue(byteQueue);
//            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_GROUP_COMMAND_RESPONSE);
//            advertiseTask.startAdvertising();
//
//            dialog.dismiss();
//        });

        deviceName.setText(deviceDetail.getGroupName());

        dialog.show();

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView = LayoutInflater.from(activity).
                inflate(R.layout.dashboard_item_adapter, parent, false);

        ViewHolder viewHolder = new ViewHolder(convertView);
        GroupDetailsClass deviceClass=arrayList.get(position);
        viewHolder.dashboardDeviceName.setText(deviceClass.getGroupName());
        viewHolder.dashboardCustomize.setOnClickListener(view -> showDialog(position));
        viewHolder.lightDetails.setOnClickListener(v -> {

            Log.w("GroupDimming",deviceClass.getGroupDimming()+"");
            Intent intent = new Intent(activity, HelperActivity.class);
            intent.putExtra(Constants.MAIN_KEY, Constants.EDIT_GROUP);
            intent.putExtra(Constants.GROUP_DETAIL_KEY,arrayList.get(position));
            activity.startActivity(intent);

        });

        seekBarProgress=deviceClass.getGroupDimming();
        viewHolder.statusSwitch.setChecked(deviceClass.getGroupStatus());
        viewHolder.statusSwitch.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb)
            {
                boolean switchStatus= state != State.LEFT;
                if(deviceClass.getGroupStatus()==switchStatus)
                {
//                    Log.w("Advertise","state is same");
                    return;
                }

                AdvertiseTask advertiseTask;
                selectedPosition=position;
                ByteQueue byteQueue=new ByteQueue();
                byteQueue.push(GROUP_STATE_COMMAND);       ////State Group Command method type
                byteQueue.push(deviceClass.getGroupId( ));
                Log.w("DashboardItemAdapter",state+"");
                switch (state)
                {
                    case LEFT:
                        //// remove group method type
//                        byteQueue.pushS4B(12);

                        byteQueue.push(0x00);   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setGroupStatus(false);
                        Log.w("DashboardItem","LEFT");

                        break;
                    case RIGHT:
                        Log.w("DashboardItem","RIGHT");
                        byteQueue.push(0x01 );   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setGroupStatus(true);
                        break;
                    case LEFT_TO_RIGHT:

//                        Log.w("DashboardItem","LEFT_TO_RIGHT");
                        return;

                    case RIGHT_TO_LEFT:
//                        Log.w("DashboardItem","RIGHT_TO_LEFT");
                        return;

                }
                byteQueue.pushU3B(0x00);
                advertiseTask=new AdvertiseTask(DashboardItemAdapter.this,activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(GROUP_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();

            }
        });
//        Picasso.with(activity).load(IMAGE_URL + friendsDetails.getUserImage()).placeholder(R.drawable.ic_user_male_icon_2).error(R.drawable.ic_user_male_icon_2).into(viewHolder.friendsProfile);

        return convertView;
    }


    public ArrayList<DeviceClass> getGroupLight(int groupId)
    {
        ArrayList<DeviceClass> deviceClasses=new ArrayList<>();
        Cursor cursor = sqlHelper.getLightInGroup(groupId);
        if (cursor.moveToFirst()) {
            do {
                DeviceClass deviceClass = new DeviceClass();
                deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                deviceClass.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                deviceClass.setDeriveType(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DERIVE_TYPE)));
                deviceClass.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_PROGRESS)));
                deviceClass.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                deviceClass.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_STATUS))==1);
                // do what ever you want here
            } while (cursor.moveToNext());
        }

        cursor.close();
//        setList(arrayList);
        return deviceClasses;
    }



    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        animatedProgress.hideProgress();
        ContentValues contentValues=new ContentValues();
        ContentValues deviceContentValue = new ContentValues();
        Log.w("MethodType",(int)byteQueue.pop()+"");


        switch (successCode)
        {
            case GROUP_STATE_COMMAND_RESPONSE:
                int groupId=byteQueue.pop();
                Log.w("ScanningBeacon",groupId+"");
                int status=byteQueue.pop();
//                                String s = "4d0d08ada45f9dde1e99cad9";
                Log.w("Scann",","+status);

                if(status==0) {
                    boolean groupStatus = !arrayList.get(selectedPosition).getGroupStatus();

                    contentValues.put(COLUMN_GROUP_STATUS, groupStatus);
                    contentValues.put(COLUMN_GROUP_PROGRESS, groupStatus ? 100 : 0);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS, groupStatus);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, groupStatus ? 100 : 0);

                    arrayList.get(selectedPosition).setGroupStatus(groupStatus);
                    arrayList.get(selectedPosition).setGroupDimming(groupStatus ? 100 : 0);
                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                }
                else {
//                    this.groupStatus.setChecked(groupDetailsClass.getGroupStatus());
                    notifyDataSetChanged();
                }
                break;

            case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:
                int lightStatus = byteQueue.pop();
                if(lightStatus==0) {
                    contentValues.put(COLUMN_GROUP_PROGRESS, seekBarProgress);
                    contentValues.put(COLUMN_GROUP_STATUS, 1);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS, 1);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, seekBarProgress);
                    arrayList.get(selectedPosition).setGroupDimming(seekBarProgress);
                    arrayList.get(selectedPosition).setGroupStatus(true);
                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                }
                notifyDataSetChanged();
                break;
        }
//        animatedProgress.hideProgress();
        Log.w(TAG,"onScanSuccess "+successCode);
    }

    @Override
    public void onScanFailed(int errorCode)
    {
        NiftyDialogBuilder dialogBuilder= NiftyDialogBuilder.getInstance(activity);
        dialogBuilder
                .withTitle("Timeout")
                .withEffect(Effectstype.Newspager)
                .withMessage("Timeout,Please check your beacon is in range")
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
        animatedProgress.hideProgress();
        notifyDataSetChanged();
        if(AppHelper.IS_TESTING)
        {
            ContentValues contentValues = new ContentValues();
            ContentValues deviceContentValue = new ContentValues();
            switch (requestCode2) {
                case GROUP_STATE_COMMAND_RESPONSE:
                    boolean groupStatus = !arrayList.get(selectedPosition).getGroupStatus();

                    contentValues.put(COLUMN_GROUP_STATUS, groupStatus);
                    contentValues.put(COLUMN_GROUP_PROGRESS, groupStatus ? 100 : 0);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS,groupStatus);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, groupStatus ? 100 : 0);

                    arrayList.get(selectedPosition).setGroupStatus(groupStatus);
                    arrayList.get(selectedPosition).setGroupDimming(groupStatus ? 100 : 0);

                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                    break;

                case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:

                    contentValues.put(COLUMN_GROUP_STATUS,true);
                    contentValues.put(COLUMN_GROUP_PROGRESS, seekBarProgress);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS,true);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, seekBarProgress);

                    arrayList.get(selectedPosition).setGroupDimming(seekBarProgress);
                    arrayList.get(selectedPosition).setGroupStatus(true);

                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                    notifyDataSetChanged();
                    break;
            }
        }
        Log.w(TAG,"onScanFailed "+errorCode);
    }
    @Override
    public void onSuccess(String message) {
        animatedProgress.showProgress();
        Log.w(TAG,"onSuccess "+message);
    }

    @Override
    public void onFailed(String errorMessage) {
        Log.w(TAG,"onFailed "+errorMessage);
        animatedProgress.hideProgress();
    }


    @Override
    public void onStop(String stopMessage, int resultCode) {
//        Log.w(TAG,"onStop "+stopMessage);
//        requestCode2=resultCode;
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
        //        Log.w(TAG,"onStop "+stopMessage);
//        requestCode2=resultCode;
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
        animatedProgress.hideProgress();
        ContentValues contentValues=new ContentValues();
        ContentValues deviceContentValue = new ContentValues();



        switch (resultCode) {
            case GROUP_STATE_COMMAND_RESPONSE:

                boolean groupStatus = !arrayList.get(selectedPosition).getGroupStatus();

                contentValues.put(COLUMN_GROUP_STATUS, groupStatus);
                contentValues.put(COLUMN_GROUP_PROGRESS, groupStatus ? 100 : 0);

                deviceContentValue.put(COLUMN_DEVICE_STATUS, groupStatus);
                deviceContentValue.put(COLUMN_DEVICE_PROGRESS, groupStatus ? 100 : 0);

                arrayList.get(selectedPosition).setGroupStatus(groupStatus);
                arrayList.get(selectedPosition).setGroupDimming(groupStatus ? 100 : 0);
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();

                break;

            case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:
                contentValues.put(COLUMN_GROUP_PROGRESS, seekBarProgress);
                contentValues.put(COLUMN_GROUP_STATUS, 1);

                deviceContentValue.put(COLUMN_DEVICE_STATUS, 1);
                deviceContentValue.put(COLUMN_DEVICE_PROGRESS, seekBarProgress);
                arrayList.get(selectedPosition).setGroupDimming(seekBarProgress);
                arrayList.get(selectedPosition).setGroupStatus(true);
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(arrayList.get(selectedPosition).getGroupId(), contentValues) + "");
                Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(arrayList.get(selectedPosition).getGroupId(), deviceContentValue) + "");
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                break;
        }


    }


    static class ViewHolder {
        @BindView(R.id.dashboard_deviceName)
        TextView dashboardDeviceName;

        @BindView(R.id.individual_customize)
        Button dashboardCustomize;

        @BindView(R.id.light_details)
        ImageView lightDetails;

        @BindView(R.id.status_switch)
        JellyToggleButton statusSwitch;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
