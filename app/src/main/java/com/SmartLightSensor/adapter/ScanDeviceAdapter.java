package com.SmartLightSensor.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.RxMethodType;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.PogoClasses.BeconDeviceClass;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupDetailsClass;
import com.SmartLightSensor.R;
import com.SmartLightSensor.ServiceModule.AdvertiseTask;
import com.SmartLightSensor.ServiceModule.ScannerTask;
import com.SmartLightSensor.activity.AppHelper;
import com.niftymodaldialogeffects.Effectstype;
import com.niftymodaldialogeffects.NiftyDialogBuilder;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.Gravity.CENTER;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_STATE_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.SELECT_MASTER_RESPONSE;
import static com.SmartLightSensor.activity.AppHelper.sqlHelper;

public class ScanDeviceAdapter extends BaseAdapter implements AdvertiseResultInterface, ReceiverResultInterface {
    Activity activity;
    ArrayList<BeconDeviceClass> arrayList;
    ArrayAdapter<GroupDetailsClass> adapter;
    ArrayList<GroupDetailsClass> groupDetailsClasses;
    ArrayList<DeviceClass> deviceList;
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int requestCode;
    String TAG=this.getClass().getSimpleName();
    int selectedPosition=-1;

    public ScanDeviceAdapter(@NonNull Activity context) {
        activity = context;
        arrayList = new ArrayList<>();
        deviceList = new ArrayList<>();
        groupDetailsClasses=new ArrayList<>();
        scannerTask=new ScannerTask(activity,this);
        animatedProgress=new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);
        adapter=new ArrayAdapter<GroupDetailsClass>(activity,R.layout.spinerlayout,groupDetailsClasses){
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the spinner collapsed item (non-popup item) as a text view
                TextView tv = (TextView) super.getView(position, convertView, parent);

                // Set the text color of spinner item
                tv.setTextColor(Color.BLACK);
                tv.setText(groupDetailsClasses.get(position).getGroupName());
                // Return the view
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                // Cast the drop down items (popup items) as text view
                TextView tv = (TextView) super.getDropDownView(position,convertView,parent);

                // Set the text color of drop down items
                tv.setTextColor(Color.BLACK);
                tv.setText(groupDetailsClasses.get(position).getGroupName());

                /*// If this item is selected item
                if(position == mSelectedIndex){
                    // Set spinner selected popup item's text color
                    tv.setTextColor(Color.BLUE);
                }*/

                // Return the modified view
                return tv;
            }
        };
        getAllGroups();
        if(AppHelper.IS_TESTING) {
            setArrayList();
        }
    }

    public void clearList() {
        if(this.arrayList==null)
            this.arrayList=new ArrayList<>();
        this.arrayList.clear();
        notifyDataSetChanged();

    }
    public void setArrayList(ArrayList<BeconDeviceClass> arrayList) {
//        if(this.arrayList==null)
//            this.arrayList=new ArrayList<>();
//        this.arrayList.clear();
        this.arrayList = arrayList;
        notifyDataSetChanged();

    }
    public void setArrayList()
    {
        for (int i=0;i<=20;i++)
        {
            BeconDeviceClass beconDeviceClass=new BeconDeviceClass();
            beconDeviceClass.setBeaconUID(i+10);
            beconDeviceClass.setDeviceUid((i+10)+"");
            beconDeviceClass.setDeriveType(0x01);
            arrayList.add(beconDeviceClass);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public BeconDeviceClass getItem(int position) {
        if (arrayList.size() <= position)
            return null;
        return arrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).
                    inflate(R.layout.scan_device_adapter, parent, false);
        }
        BeconDeviceClass beconDeviceClass=arrayList.get(position);
        ViewHolder viewHolder=new ViewHolder(convertView);
        String uid = beconDeviceClass.getDeviceUid();
        viewHolder.addDevice.setText(beconDeviceClass.isAdded()?"Added":"Add");
//        Integer uid_int_dialog = Integer.parseInt(uid, 16);

//        viewHolder.addDeviceUid.setText(beconDeviceClass.getDeviceUid());

        if (beconDeviceClass.isAdded()) {
            viewHolder.addDevice.setVisibility(View.GONE);
            viewHolder.addDeviceUid.setText(beconDeviceClass.getDeviceName());
            viewHolder.statusSwitch.setVisibility(View.VISIBLE);
        } else {
            viewHolder.addDevice.setVisibility(View.VISIBLE);
            viewHolder.addDeviceUid.setText(beconDeviceClass.getDeviceUid());
            viewHolder.statusSwitch.setVisibility(View.GONE);
        }
        if (beconDeviceClass.getiBeaconUuid().equalsIgnoreCase("533")) {
            viewHolder.review1.setImageResource(R.drawable.mokoband);
            viewHolder.statusSwitch.setVisibility(View.GONE);
        }else if (beconDeviceClass.getiBeaconUuid().equalsIgnoreCase("55811")) {
            viewHolder.review1.setImageResource(R.mipmap.push_button);
            viewHolder.statusSwitch.setVisibility(View.GONE);
        }else {
            viewHolder.review1.setImageResource(R.drawable.ic_lightbulb_outline_black_24dp);
        }

        viewHolder.statusSwitch.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb) {
                boolean switchStatus = state != State.LEFT;
                if (beconDeviceClass.getStatus() == switchStatus) {
                    return;
                }
                requestCode = RxMethodType.LIGHT_STATE_COMMAND;
                AdvertiseTask advertiseTask;
                ByteQueue byteQueue = new ByteQueue();
                byteQueue.push(requestCode); ////State Command
                byteQueue.pushU4B(beconDeviceClass.getBeaconUID()); //// 12 is static vale for Node id
                Log.w(TAG, state + "");
                switch (state) {
                    case LEFT:
                        byteQueue.push(0x00); //0x00 – OFF 0x01 – ON
                        arrayList.get(position).setStatus(false);

                        break;
                    case RIGHT:
                        byteQueue.push(0x01); //0x00 – OFF 0x01 – ON
                        arrayList.get(position).setStatus(true);
                        break;
                    case LEFT_TO_RIGHT:

                        return;

                    case RIGHT_TO_LEFT:
                        return;

                }
                selectedPosition = position;
                advertiseTask = new AdvertiseTask(ScanDeviceAdapter.this, activity, 5 * 1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
            }

        });
        viewHolder.addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(position);

            }
        });

        return convertView;
    }

    public void showDialog(int position) {
        selectedPosition = position;
        final Dialog dialog = new Dialog(activity);
        BeconDeviceClass beconDeviceClass = arrayList.get(position);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        EditText deviceName = dialog.findViewById(R.id.deviceName);
        EditText et_site = dialog.findViewById(R.id.et_site);
        EditText et_building = dialog.findViewById(R.id.et_building);
        EditText et_level = dialog.findViewById(R.id.et_level);
        EditText et_room = dialog.findViewById(R.id.et_room);
//        EditText et_group = dialog.findViewById(R.id.et_group);
        Spinner spinner=dialog.findViewById(R.id.add_device_group_list);
        Button btn_submit = dialog.findViewById(R.id.btn_submit);
        spinner.setAdapter(adapter);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btn_submit.setOnClickListener(view -> {
         String name_st,site_st,building_st,level_st,room_st,group_st;
            name_st=deviceName.getText().toString().trim();
            site_st = et_site.getText().toString().trim();
            building_st =et_building.getText().toString().trim();
            level_st =et_level.getText().toString().trim();
            room_st = et_room.getText().toString().trim();
//            group_st = et_group.getText().toString().trim();

            if (name_st.length()==0) {
                deviceName.setError("Please Enter Name");
                requestFocus(deviceName);
                return;
            }

            ContentValues contentValues=new ContentValues();
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_UID,beconDeviceClass.getBeaconUID());
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_NAME,deviceName.getText().toString());
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_SITE_NAME,et_site.getText().toString());
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_BUILDING_NAME,et_building.getText().toString());
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_LEVEL_NAME,et_level.getText().toString());
            contentValues.put(DatabaseConstant.COLUMN_DEVICE_ROOM_NAME,et_room.getText().toString());
            contentValues.put(DatabaseConstant.COLUMN_GROUP_ID,((GroupDetailsClass) spinner.getSelectedItem()).getGroupId());

//            contentValues.put(DatabaseConstant.COLUMN_DEVICE_GROUP_NAME,et_group.getText().toString());


            if(sqlHelper.insertData(DatabaseConstant.ADD_DEVICE_TABLE,contentValues)<0) {
                arrayList.get(position).setAdded(true);
                Toast.makeText(activity, "Device Already added.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }else {
                arrayList.get(position).setAdded(true);
                arrayList.get(position).setDeviceName(deviceName.getText().toString());
                arrayList.get(position).setDeviceSite(et_site.getText().toString());
                arrayList.get(position).setDeviceBuilding(et_building.getText().toString());
                arrayList.get(position).setDeviceLevel(et_level.getText().toString());
                arrayList.get(position).setDeviceRoom(et_room.getText().toString());
//                arrayList.get(position).setDeviceGroup(et_group.getText().toString());
                Toast.makeText(activity, "Device  added successfully.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
            notifyDataSetChanged();
        });
    }

        private void requestFocus(View view) {
            if (view.requestFocus()) {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }

    @Override
    public void onSuccess(String message) {
        animatedProgress.showProgress();
        Log.w(TAG,"Advertising start");
    }

    @Override
    public void onFailed(String errorMessage) {
        if(animatedProgress==null)
            return;
        Toast.makeText(activity, "Advertising failed.", Toast.LENGTH_SHORT).show();
        animatedProgress.hideProgress();
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
        scannerTask=new ScannerTask(activity,this);
        scannerTask.setRequestCode(resultCode);
        scannerTask.start();
        Log.w(TAG,"Advertising stop"+resultCode);
    }


    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        if(animatedProgress==null)
            return;
        animatedProgress.hideProgress();
        ContentValues contentValues=new ContentValues();

        Log.w("BYTEQUESIZE",byteQueue.size()+",");
        Log.w("MethodType",(int)byteQueue.pop()+"");

        byte bytes1;
        String nodeUid;
        long deviceUid;
        int lightStatus;
        NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(activity);
        switch (successCode)
        {
            case SELECT_MASTER_RESPONSE:




                BeconDeviceClass beconDeviceClass=arrayList.get(selectedPosition);
                arrayList.get(selectedPosition).setMasterStatus(1);
                lightStatus=byteQueue.pop();

                contentValues.put(COLUMN_DEVICE_MASTER_STATUS, lightStatus==0?1:0);



                dialogBuilder
                        .withTitle("Master Status")
                        .withEffect(Effectstype.RotateBottom)
                        .withMessage("Light is set as master")
                        .withButton1Text("OK")
                        .setButton1Click(v -> {
                            dialogBuilder.dismiss();
                        })
                        .show();
                Log.w("DashGroup", sqlHelper.updateDevice(beconDeviceClass.getBeaconUID(), contentValues) +"");

                break;


        }

    }

    public void getAllGroups()
    {
        groupDetailsClasses.clear();
        GroupDetailsClass noGroupData = new GroupDetailsClass();
        noGroupData.setGroupId(0);
        noGroupData.setGroupName("No Group");
        noGroupData.setGroupStatus(true);
        groupDetailsClasses.add(noGroupData);
        Cursor cursor = sqlHelper.getAllGroup();
        if (cursor.moveToFirst())
        {
            do {
                GroupDetailsClass groupData = new GroupDetailsClass();
                groupData.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                groupData.setGroupDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_PROGRESS)));
                groupData.setGroupName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_NAME)));
                groupData.setGroupStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_STATUS)) == 1);
                groupDetailsClasses.add(groupData);
                // do what ever you want here
            }
            while (cursor.moveToNext());
        }

//
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onScanFailed(int errorCode) {
        if(animatedProgress==null)
            return;
        animatedProgress.hideProgress();
//        NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(activity);
//        dialogBuilder
//                .withTitle("Timeout")
//                .withEffect(Effectstype.Slit)
//                .withMessage("Timeout,Please check your beacon is in range")
//                .withButton1Text("OK")
//                .setButton1Click(v -> {
//                    dialogBuilder.dismiss();
//                })
//                .show();
    }

    static class ViewHolder {
        @BindView(R.id.review_1)
        ImageView review1;
        @BindView(R.id.add_device)
        Button addDevice;
        //        @BindView(R.id.add_device_layout)
//        RelativeLayout addDeviceLayout;
        @BindView(R.id.add_device_uid)
        TextView addDeviceUid;
        @BindView(R.id.status_switch)
        JellyToggleButton statusSwitch;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }}