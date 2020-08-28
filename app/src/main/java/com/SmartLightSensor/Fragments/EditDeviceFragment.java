package com.SmartLightSensor.Fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.EncodeDecodeModule.ArrayUtilities;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.RxMethodType;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupDetailsClass;
import com.SmartLightSensor.R;
import com.SmartLightSensor.ServiceModule.AdvertiseTask;
import com.SmartLightSensor.ServiceModule.ScannerTask;
import com.SmartLightSensor.activity.AppHelper;
import com.SmartLightSensor.activity.HelperActivity;
import com.SmartLightSensor.adapter.IndividualLightAdapter;
import com.SmartLightSensor.constant.Constants;
import com.niftymodaldialogeffects.Effectstype;
import com.niftymodaldialogeffects.NiftyDialogBuilder;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.math.BigInteger;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.view.Gravity.CENTER;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_STATUS;
import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.LIGHT_LEVEL_COMMAND;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.GROUP_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_STATE_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_STATE_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.SELECT_MASTER_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.UPDATE_GROUP_RESPONSE;
import static com.SmartLightSensor.ServiceModule.ScanningBeacon.bytesToHex;
import static com.SmartLightSensor.activity.AppHelper.sqlHelper;

public class EditDeviceFragment extends Fragment implements AdvertiseResultInterface, ReceiverResultInterface {


    @BindView(R.id.group_name_text)
    TextView groupNameText;

    @BindView(R.id.edit_light_status)
    TextView editLightStatus;

    @BindView(R.id.edit_light_deriveType)
    TextView lightDeriveType;

    @BindView(R.id.edit_light_name)
    EditText editLightName;

//    @BindView(R.id.group_list_spinner)
//    Spinner groupListSpinner;

    @BindView(R.id.light_save)
    ImageView lightSave;
    @BindView(R.id.light_delete)
    ImageView lightDelete;

    @BindView(R.id.status_switch)
    JellyToggleButton lightStatus;

//    @BindView(R.id.group_save)
//    Button lightSave;
//    @BindView(R.id.details)
//    Button details;
//        @BindView(R.id.edit_light_save)
//        LinearLayout editLightSave;
    Unbinder unbinder;
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    String TAG = this.getClass().getSimpleName();
    Activity activity;
    DeviceClass deviceClass;
    int spinnerSelectedPosition = 0;
    ArrayAdapter<GroupDetailsClass> adapter;
    ArrayList<GroupDetailsClass> list;
    int levelProgress = 0;
    int requestCode;
    @BindView(R.id.light_edit)
    ImageView lightEdit;
//    @BindView(R.id.light_save)
//    ImageView editLightSave;

//    @BindView(R.id.light_delete)
//    ImageView editLightDelete;
String Type;

    public EditDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_device, container, false);
        activity = getActivity();
        unbinder = ButterKnife.bind(this, view);

        Type= getActivity().getIntent().getStringExtra("Type");
        Log.e("TYPE====>",Type.trim());

        if (deviceClass == null) {
            deviceClass = new DeviceClass();
        }

        if (Type.equalsIgnoreCase("light")){
            editLightName.setText(deviceClass.getDeviceName());
            groupNameText.setText(deviceClass.getDeviceName());
        }else if(Type.equalsIgnoreCase("site")){
            editLightName.setText(deviceClass.getDeviceSite());
            groupNameText.setText(deviceClass.getDeviceSite());

        }else if (Type.equalsIgnoreCase("building")){
            editLightName.setText(deviceClass.getDeviceBuilding());
            groupNameText.setText(deviceClass.getDeviceBuilding());

        }else if (Type.equalsIgnoreCase("level")){
            editLightName.setText(deviceClass.getDeviceLevel());
            groupNameText.setText(deviceClass.getDeviceLevel());

        }else if (Type.equalsIgnoreCase("room")){
            editLightName.setText(deviceClass.getDeviceRoom());
            groupNameText.setText(deviceClass.getDeviceRoom());
        }



        scannerTask = new ScannerTask(activity, this);
        animatedProgress = new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);
        //animatedProgress.showProgress();
        list = new ArrayList<>();


        lightDeriveType.setText(String.format("Device Type:%s", deviceClass.getDeriveType()));

        Log.e("DeriverType=========>",deviceClass.getDeriveType());

//        ByteQueue byteQueue1 = new ByteQueue();
//        byteQueue1.push(RxMethodType.LIGHT_LEVEL);
//        byteQueue1.pushU4B(deviceClass.getDeviceUID());
//        byteQueue1.push(0x00);
//        AdvertiseTask advertiseTask1;
//        advertiseTask1 = new AdvertiseTask(this, activity);
//        advertiseTask1.setByteQueue(byteQueue1);
//        advertiseTask1.setSearchRequestCode(LIGHT_LEVEL_RESPONSE);
//        advertiseTask1.startAdvertising();


        if (deviceClass.getStatus())
            editLightStatus.setText("Light Status:On");
        else
            editLightStatus.setText("Light Status:Off");


//        details.setOnClickListener(v -> {
//            Intent intent = new Intent(activity, HelperActivity.class);
//            intent.putExtra(Constants.MAIN_KEY, Constants.READ_DETAILS);
//            intent.putExtra(Constants.LIGHT_DETAIL_KEY,deviceClass);
//            activity.startActivity(intent);
//        });
//if(deviceClass.getGroupId()==0)
//{
//    if(deviceClass.getStatus())
//        editLightStatus.setText("Light Status:On");
//    else
//        editLightStatus.setText("Light Status:Off");
//
//
//    ByteQueue byteQueue = new ByteQueue();
//    byteQueue.push(RxMethodType.LIGHT_STATE);
//    byteQueue.pushU4B(deviceClass.getDeviceUID());
//    byteQueue.push(0x00);
//    AdvertiseTask advertiseTask;
//    advertiseTask = new AdvertiseTask(this, activity);
//    advertiseTask.setByteQueue(byteQueue);
//    advertiseTask.setSearchRequestCode(LIGHT_STATE_RESPONSE);
//    advertiseTask.startAdvertising();

        setLightStatus();
        lightStatus.setChecked(deviceClass.getStatus());
//}
//else {
//    editLightStatus.setText("");
//    Toast.makeText(activity, "group command", Toast.LENGTH_SHORT).show();
//    ByteQueue byteQueue = new ByteQueue();
//    byteQueue.push(RxMethodType.GROUP);
//    byteQueue.pushU4B(deviceClass.getDeviceUID());
//    byteQueue.push(0x00);
//    AdvertiseTask advertiseTask;
//    advertiseTask = new AdvertiseTask(this, activity);
//    advertiseTask.setByteQueue(byteQueue);
//    advertiseTask.setSearchRequestCode(GROUP_RESPONSE);
//    advertiseTask.startAdvertising();
//}
        adapter = new ArrayAdapter<GroupDetailsClass>(activity, R.layout.spinerlayout, list) {
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the spinner collapsed item (non-popup item) as a text view
                TextView tv = (TextView) super.getView(position, convertView, parent);

                // Set the text color of spinner item
                tv.setTextColor(Color.GRAY);
                tv.setText(list.get(position).getGroupName());
                // Return the view
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // Cast the drop down items (popup items) as text view
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);

                // Set the text color of drop down items
                tv.setTextColor(Color.BLACK);
                tv.setText(list.get(position).getGroupName());

                /*// If this item is selected item
                if(position == mSelectedIndex){
                    // Set spinner selected popup item's text color
                    tv.setTextColor(Color.BLUE);
                }*/

                // Return the modified view
                return tv;
            }
        };
//        groupListSpinner.setAdapter(adapter);
//        getAllGroups();
        return view;
    }

//    public void getAllGroups() {
//        list.clear();
//        GroupDetailsClass noGroup = new GroupDetailsClass();
//        noGroup.setGroupName("No Group");
//        list.add(noGroup);
//
//        Cursor cursor = sqlHelper.getAllGroup();
//        int i = 1;
//        if (cursor.moveToFirst()) {
//            do {
//                GroupDetailsClass groupData = new GroupDetailsClass();
//                groupData.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
//                groupData.setGroupDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_PROGRESS)));
//                groupData.setGroupName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_NAME)));
//                groupData.setGroupStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_STATUS)) == 1);
//                list.add(groupData);
//                if (groupData.getGroupId() == deviceClass.getGroupId()) {
//                    spinnerSelectedPosition = i;
////                    Toast.makeText(activity, "i=" + i, Toast.LENGTH_SHORT).show();
//                }
//                i++;
//                // do what ever you want here
//            }
//            while (cursor.moveToNext());
//        }
//
////
//        cursor.close();
//        adapter.notifyDataSetChanged();
//        groupListSpinner.setSelection(spinnerSelectedPosition);
//    }

    public GroupDetailsClass getGroup(int id) {

        for (GroupDetailsClass groupDetailsClass : list) {
            if (groupDetailsClass.getGroupId() == id)
                return groupDetailsClass;
        }

        return new GroupDetailsClass();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setLightStatus() {
        lightStatus.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb) {

                boolean switchStatus = state != State.LEFT;
                if (deviceClass.getStatus() == switchStatus) {
//                    Log.w("Advertise","state is same");
                    return;
                }
                Log.w("DeviceID", deviceClass.getDeviceUID() + "");

                AdvertiseTask advertiseTask;
                ByteQueue byteQueue = new ByteQueue();
                byteQueue.push(RxMethodType.LIGHT_STATE_COMMAND);       ////State Command
                byteQueue.pushU4B(deviceClass.getDeviceUID());      ////  12 is static vale for Node id
//                byteQueue.push(0x00);                                    ///0x00 – OFF    0x01 – ON
//                scannerTask.setRequestCode(TxMethodType.LIGHT_STATE_COMMAND_RESPONSE);
                Log.w(TAG, state + "");
                switch (state) {
                    case LEFT:
                        //// remove group method type
//                        byteQueue.pushS4B(12);
                        Log.w("SwitchStatus", "Left");
                        byteQueue.push(0x00);   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(false);

                        break;
                    case RIGHT:
                        Log.w("SwitchStatus", "Right");
                        byteQueue.push(0x01);   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(true);
                        break;
                    case LEFT_TO_RIGHT:

                        return;

                    case RIGHT_TO_LEFT:
                        return;

                }
                advertiseTask = new AdvertiseTask(EditDeviceFragment.this, activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
            }
        });
    }

    void showDialog() {
        levelProgress = deviceClass.getDeviceDimming();

        final Dialog dialog = new Dialog(activity);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.90);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setGravity(CENTER);

        dialog.setContentView(R.layout.customize_group);

        TextView deviceName = dialog.findViewById(R.id.customize_group_name);
        SeekBar seekBar = dialog.findViewById(R.id.customizeGroupSeekBar);
        Button button = dialog.findViewById(R.id.customiseGroupSave);

        TextView levelPercentage = dialog.findViewById(R.id.level_percentage);
        levelPercentage.setText(levelProgress + " %");

        seekBar.setProgress(deviceClass.getDeviceDimming());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                levelProgress = i;
                levelPercentage.setText(levelProgress + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                String hex = Integer.toHexString(levelProgress);
                Log.w("IndividualLight", hex + " " + String.format("%02X", levelProgress));
                AdvertiseTask advertiseTask;
                ByteQueue byteQueue = new ByteQueue();
                byteQueue.push(LIGHT_LEVEL_COMMAND);   //// Light Level Command method type
                byteQueue.pushU4B(deviceClass.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
                byteQueue.push(levelProgress);    ////0x00-0x64
//            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
                advertiseTask = new AdvertiseTask(EditDeviceFragment.this, activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();

            }
        });
//        button.setOnClickListener(view -> {
//
//
//            String hex = Integer.toHexString(levelProgress);
//            Log.w("IndividualLight", hex + " " + String.format("%02X", levelProgress));
//            AdvertiseTask advertiseTask;
//            ByteQueue byteQueue = new ByteQueue();
//            byteQueue.push(LIGHT_LEVEL_COMMAND);   //// Light Level Command method type
//            byteQueue.pushU4B(deviceClass.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
//            byteQueue.push(levelProgress);    ////0x00-0x64
////            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
//            advertiseTask = new AdvertiseTask(this, activity);
//            advertiseTask.setByteQueue(byteQueue);
//            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
//            advertiseTask.startAdvertising();
////            Log.w("IndividualLight",AppHelper.sqlHelper.updateDevice(deviceDetail.getDeviceUID(),contentValues)+"");
//            dialog.dismiss();
//        });

        deviceName.setText(deviceClass.getDeviceName());

        dialog.show();

    }

    public void checkMaster() {
        DeviceClass deviceClass1 = null;
        Cursor cursor = sqlHelper.getLightDetails(deviceClass.getDeviceUID());
        if (cursor.moveToFirst()) {
            do {
                deviceClass1 = new DeviceClass();
                deviceClass1.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                deviceClass1.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                deviceClass1.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_PROGRESS)));
                deviceClass1.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                deviceClass1.setMasterStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_MASTER_STATUS)));
                deviceClass1.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_STATUS)) == 1);

                // do what ever you want here
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (deviceClass1 != null) {
            if (deviceClass1.getMasterStatus() == 1) {
                showAlert(String.format("'%s' is master.", deviceClass.getDeviceName()));
            } else {
                showAlert(String.format("'%s' is not master.", deviceClass.getDeviceName()));
            }
        }
    }

    @OnClick({R.id.light_delete,R.id.light_save, R.id.light_edit,   /*R.id.light_check_group,*/ R.id.set_level/*, R.id.light_check_status, R.id.light_check_master*/})
    public void onViewClicked(View view) {
        final AdvertiseTask[] advertiseTask = new AdvertiseTask[1];
        final ByteQueue[] byteQueue = new ByteQueue[1];
        switch (view.getId()) {
            case R.id.light_delete:
//                deleteDialog();
//                if (sqlHelper.deleteLight(deviceClass.getDeviceUID()) > 0) {
//                    Toast.makeText(activity, "Group deleted.", Toast.LENGTH_SHORT).show();
//                    activity.onBackPressed();
//                } else
//                    Toast.makeText(activity, "Some error to delete group", Toast.LENGTH_SHORT).show();
//                break;
            case R.id.light_edit:
                if (editLightName.isEnabled())
                {
                    editLightName.setEnabled(false);
                    lightEdit.setVisibility(View.VISIBLE);
                    lightSave.setVisibility(View.GONE);
//                    editLightDelete.setVisibility(View.GONE);
                }
                else
                {
                    editLightName.setEnabled(true);
                    lightEdit.setVisibility(View.GONE);
                    lightSave.setVisibility(View.VISIBLE);
//                    editLightDelete.setVisibility(View.VISIBLE);
                }
                break;
//            case R.id.light_check_group:
//
//                byteQueue[0] = new ByteQueue();
//                byteQueue[0].push(RxMethodType.GROUP);
//                byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                byteQueue[0].push(0x00);
//                advertiseTask[0] = new AdvertiseTask(this, activity);
//                advertiseTask[0].setByteQueue(byteQueue[0]);
//                advertiseTask[0].setSearchRequestCode(GROUP_RESPONSE);
//                advertiseTask[0].startAdvertising();
//
//                break;
//            case R.id.light_set_master:
//                NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
//                dialogBuilder
//                        .withTitle("Master Light")
//                        .withEffect(Effectstype.Shake)
//                        .withMessage("Set light '" + deviceClass.getDeviceName() + "' as master light")
//                        .withButton1Text("OK")
//                        .setButton1Click(v -> {
//                            byteQueue[0] = new ByteQueue();
//                            byteQueue[0].push(RxMethodType.SELECT_MASTER);
//                            byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                            byteQueue[0].push(0x00);
//                            advertiseTask[0] = new AdvertiseTask(EditDeviceFragment.this, activity);
//                            advertiseTask[0].setByteQueue(byteQueue[0]);
//                            advertiseTask[0].setSearchRequestCode(SELECT_MASTER_RESPONSE);
//                            advertiseTask[0].startAdvertising();
//                            dialogBuilder.dismiss();
//                        }).withButton2Text("Cancel")
//                        .setButton2Click(v -> {
//                            dialogBuilder.dismiss();
//                        })
//                        .show();
//
//                break;
//            case R.id.light_check_status:
//                byteQueue[0] = new ByteQueue();
//                byteQueue[0].push(RxMethodType.LIGHT_STATE);
//                byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                byteQueue[0].push(0x00);
//                advertiseTask[0] = new AdvertiseTask(this, activity);
//                advertiseTask[0].setByteQueue(byteQueue[0]);
//                advertiseTask[0].setSearchRequestCode(LIGHT_STATE_RESPONSE);
//                advertiseTask[0].startAdvertising();
//
//                break;
//            case R.id.light_check_group:
//                if(deviceClass.getGroupId()==0)
//                    showAlert("No Group");
//                else
//                {
//                    showAlert(String.format("Group of %s is %s.",deviceClass.getDeviceName(),sqlHelper.getGroupDetails(deviceClass.getGroupId()).getGroupName()));
//                }

//                byteQueue[0] = new ByteQueue();
//                byteQueue[0].push(RxMethodType.GROUP);
//                byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                byteQueue[0].push(0x00);
//                advertiseTask[0] = new AdvertiseTask(this, activity);
//                advertiseTask[0].setByteQueue(byteQueue[0]);
//                advertiseTask[0].setSearchRequestCode(GROUP_RESPONSE);
//                advertiseTask[0].startAdvertising();

////                break;
//            case R.id.light_set_master:
//                NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
//                dialogBuilder
//                        .withTitle("Master Light")
//                        .withEffect(Effectstype.Shake)
//                        .withMessage("Set light '" + deviceClass.getDeviceName() + "' as master light")
//                        .withButton1Text("OK")
//                        .setButton1Click(v -> {
//                            byteQueue[0] = new ByteQueue();
//                            byteQueue[0].push(RxMethodType.SELECT_MASTER);
//                            byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                            byteQueue[0].push(0x00);
//                            advertiseTask[0] = new AdvertiseTask(EditDeviceFragment.this, activity,5*1000);
//                            advertiseTask[0].setByteQueue(byteQueue[0]);
//                            advertiseTask[0].setSearchRequestCode(SELECT_MASTER_RESPONSE);
//                            advertiseTask[0].startAdvertising();
//                            dialogBuilder.dismiss();
//                        }).withButton2Text("Cancel")
//                        .setButton2Click(v -> {
//                            dialogBuilder.dismiss();
//                        })
//                        .show();
//
//                break;
//            case R.id.light_check_status:
//                showAlert(deviceClass.getStatus()?"Light is on":"Light is off");
////                byteQueue[0] = new ByteQueue();
////                byteQueue[0].push(RxMethodType.LIGHT_STATE);
////                byteQueue[0].pushU4B(deviceClass.getDeviceUID());
////                byteQueue[0].push(0x00);
////                advertiseTask[0] = new AdvertiseTask(this, activity);
////                advertiseTask[0].setByteQueue(byteQueue[0]);
////                advertiseTask[0].setSearchRequestCode(LIGHT_STATE_RESPONSE);
////                advertiseTask[0].startAdvertising();
//
//                break;
//            case R.id.light_check_master:
////                Toast.makeText(activity, "Will be soon.", Toast.LENGTH_SHORT).show();
//                checkMaster();

//                break;
//            case R.id.check_level:

//                byteQueue[0] = new ByteQueue();
//                byteQueue[0].push(RxMethodType.LIGHT_LEVEL);
//                byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                byteQueue[0].push(0x00);
//                advertiseTask[0] = new AdvertiseTask(this, activity);
//                advertiseTask[0].setByteQueue(byteQueue[0]);
//                advertiseTask[0].setSearchRequestCode(LIGHT_LEVEL_RESPONSE);
//                advertiseTask[0].startAdvertising();
//                showAlert("Light dimming level is "+deviceClass.getDeviceDimming());
//                break;

            case R.id.set_level:
                showDialog();

                break;
            case R.id.light_save:
                if (editLightName.getText().toString().trim().length()<1){
                    editLightName.setError("Light name can't empty");
                    return;
                }
                ContentValues contentValues = new ContentValues();
                if (Type.equalsIgnoreCase("light")){
                    contentValues.put(DatabaseConstant.COLUMN_DEVICE_NAME, editLightName.getText().toString());
                    if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues)) {
                        Toast.makeText(activity, "Light Edited Successful.", Toast.LENGTH_SHORT).show();
                        editLightName.setEnabled(false);
                        lightEdit.setVisibility(View.VISIBLE);
//                   groupDelete.setVisibility(View.GONE);
                        lightSave.setVisibility(View.GONE);
                        activity.onBackPressed();
                    }else
                        Toast.makeText(activity, "Some error to edit Light", Toast.LENGTH_SHORT).show();

                }else if (Type.equalsIgnoreCase("site")){
                    contentValues.put(DatabaseConstant.COLUMN_DEVICE_SITE_NAME, editLightName.getText().toString());
                    if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues)) {
                        Toast.makeText(activity, "Site Edited Successful.", Toast.LENGTH_SHORT).show();
                        editLightName.setEnabled(false);
                        lightEdit.setVisibility(View.VISIBLE);
//                   groupDelete.setVisibility(View.GONE);
                        lightSave.setVisibility(View.GONE);
                        activity.onBackPressed();
                    }else
                        Toast.makeText(activity, "Some error to edit Site", Toast.LENGTH_SHORT).show();
                }else if (Type.equalsIgnoreCase("building")) {
                    contentValues.put(DatabaseConstant.COLUMN_DEVICE_BUILDING_NAME, editLightName.getText().toString());
                    if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues)) {
                        Toast.makeText(activity, "Building Edited Successful.", Toast.LENGTH_SHORT).show();
                        editLightName.setEnabled(false);
                        lightEdit.setVisibility(View.VISIBLE);
//                   groupDelete.setVisibility(View.GONE);
                        lightSave.setVisibility(View.GONE);
                        activity.onBackPressed();
                    } else
                        Toast.makeText(activity, "Some error to edit Building", Toast.LENGTH_SHORT).show();
                }else if (Type.equalsIgnoreCase("level")) {
                    contentValues.put(DatabaseConstant.COLUMN_DEVICE_LEVEL_NAME, editLightName.getText().toString());
                    if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues)) {
                        Toast.makeText(activity, "Level Edited Successful.", Toast.LENGTH_SHORT).show();
                        editLightName.setEnabled(false);
                        lightEdit.setVisibility(View.VISIBLE);
//                   groupDelete.setVisibility(View.GONE);
                        lightSave.setVisibility(View.GONE);
                        activity.onBackPressed();
                    } else
                        Toast.makeText(activity, "Some error to edit Level", Toast.LENGTH_SHORT).show();
                }else if (Type.equalsIgnoreCase("room")) {
                    contentValues.put(DatabaseConstant.COLUMN_DEVICE_ROOM_NAME, editLightName.getText().toString());
                    if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues)) {
                        Toast.makeText(activity, "Room Edited Successful.", Toast.LENGTH_SHORT).show();
                        editLightName.setEnabled(false);
                        lightEdit.setVisibility(View.VISIBLE);
//                   groupDelete.setVisibility(View.GONE);
                        lightSave.setVisibility(View.GONE);
                        activity.onBackPressed();
                    } else
                        Toast.makeText(activity, "Some error to edit Room", Toast.LENGTH_SHORT).show();
                }


                break;
//            case R.id.group_save:
//                if (groupListSpinner.getSelectedItemPosition() != spinnerSelectedPosition) {
////                    Toast.makeText(activity, "Selected Group " + list.get(groupListSpinner.getSelectedItemPosition()).getGroupName(), Toast.LENGTH_SHORT).show();
//                    byteQueue[0] = new ByteQueue();
//                    byteQueue[0].push(RxMethodType.UPDATE_GROUP);
//                    byteQueue[0].pushU4B(deviceClass.getDeviceUID());
//                    byteQueue[0].push(list.get(groupListSpinner.getSelectedItemPosition()).getGroupId());
//
//
//                    advertiseTask[0] = new AdvertiseTask(this, activity,5*1000);
//                    advertiseTask[0].setByteQueue(byteQueue[0]);
//                    advertiseTask[0].setSearchRequestCode(UPDATE_GROUP_RESPONSE);
//                    advertiseTask[0].startAdvertising();
//
//                } else
////                    saveData();
//                break;
        }
    }

//    public void saveData() {
//        if (editLightName.getText().toString().trim().length() < 1) {
//            editLightName.setError("Light name can't empty");
//            return;
//        }
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DatabaseConstant.COLUMN_DEVICE_NAME, editLightName.getText().toString());
//        if (groupListSpinner.getSelectedItemPosition() != spinnerSelectedPosition)
//            contentValues.put(DatabaseConstant.COLUMN_GROUP_ID, list.get(groupListSpinner.getSelectedItemPosition()).getGroupId());
//        if (sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues))
//        {
//            editLightName.setEnabled(false);
//            lightEdit.setVisibility(View.VISIBLE);
//            editLightSave.setVisibility(View.GONE);
////            editLightDelete.setVisibility(View.GONE);
//            activity.onBackPressed();
//        }
//        else
//            Toast.makeText(activity, "Some error to edit group", Toast.LENGTH_SHORT).show();
//    }

//    void deleteDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setMessage("Are you sure to delete Light  "+ deviceClass.getDeviceName())
//                .setTitle("Remove Group");
//        builder.setPositiveButton("delete", (dialog1, id) -> {
//            dialog1.dismiss();
//            ContentValues contentValues1 = new ContentValues();
//            contentValues1.put(DatabaseConstant.COLUMN_GROUP_ID, 0);
//            if (sqlHelper.deleteLight(deviceClass.getDeviceUID()>0)) {
//                    Toast.makeText(activity, "Group deleted.", Toast.LENGTH_SHORT).show();
//                    activity.onBackPressed();
//                } else
//                    Toast.makeText(activity, "Some error to delete group", Toast.LENGTH_SHORT).show();
////                break;
//                return;
//
//
//        });
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            dialog.dismiss();
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    public void setDeviceData(DeviceClass deviceData) {
        this.deviceClass = deviceData;
    }

    @Override
    public void onSuccess(String message) {
        animatedProgress.showProgress();
        Log.w(TAG, "Advertising start");
    }

    @Override
    public void onFailed(String errorMessage) {
        if (animatedProgress == null)
            return;
        Toast.makeText(activity, "Advertising failed.", Toast.LENGTH_SHORT).show();
        animatedProgress.hideProgress();
        Log.w(TAG,"onScanFailed "+errorMessage);

    }



    @Override
    public void onStop(String stopMessage, int resultCode) {
//        requestCode = resultCode;
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
//        Log.w(TAG, "Advertising stop" + resultCode);
        if (animatedProgress != null)
            animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();



        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        Log.w(TAG, "Advertising stop" + resultCode);
        switch (resultCode) {


            case LIGHT_STATE_COMMAND_RESPONSE:

                boolean changedStatus = !deviceClass.getStatus();
                contentValues.put(COLUMN_DEVICE_STATUS, changedStatus);
                contentValues.put(COLUMN_DEVICE_PROGRESS, changedStatus ? 100 : 0);
                deviceClass.setDeviceDimming(changedStatus ? 100 : 0);
                deviceClass.setStatus(changedStatus);
                editLightStatus.setText(String.format("Light Status:%s", deviceClass.getStatus() ? "On" : "Off"));


//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) + "");
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

                break;




            case SELECT_MASTER_RESPONSE:




//            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
                contentValues.put(COLUMN_DEVICE_MASTER_STATUS,  1 );
                deviceClass.setMasterStatus(1);
//                deviceClass.(lightStatus);
                showAlert(String.format("Light '%s' is %s as master.", deviceClass.getDeviceName(), "set "));
//                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) +"");
//
                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:

//                    bytes1=byteQueue.pop4B();
//                    ArrayUtilities.reverse(bytes1);
//                    nodeUid=bytesToHex(bytes1);
//                    Log.w("ScanningBeacon",nodeUid);
//                    //                                String s = "4d0d08ada45f9dde1e99cad9";
//                    deviceUid = new BigInteger(nodeUid, 16).longValue();
//
//                    if(deviceUid!=deviceClass.getDeviceUID())
//                        return;


                deviceClass.setDeviceDimming(levelProgress);
                contentValues.put(COLUMN_DEVICE_PROGRESS, levelProgress);
                contentValues.put(COLUMN_DEVICE_STATUS, 1);
                deviceClass.setStatus(true);

                if (!this.lightStatus.isChecked())
                    this.lightStatus.setChecked(true);
                editLightStatus.setText(String.format("Light Status:%s", deviceClass.getStatus() ? "On" : "Off"));

                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) + "");
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();


                break;

            case UPDATE_GROUP_RESPONSE:

//                saveData();
                showAlert("Group update successfully.");
//                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

//                contentValues.put(COLUMN_GROUP_PROGRESS,seekBarProgress);
//                Log.w("DashGroup",AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(),contentValues)+"");
                break;


        }
    }

    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();

        Log.w("BYTEQUESIZE", byteQueue.size() + ",");
        Log.w("MethodType", (int) byteQueue.pop() + "");

        byte[] bytes1;
        String nodeUid;
        long deviceUid;
        int lightStatus;
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        switch (successCode) {
            case LIGHT_STATE_RESPONSE:
                bytes1 = byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                nodeUid = bytesToHex(bytes1);
                Log.w("ScanningBeacon", nodeUid);
                //                                String s = "4d0d08ada45f9dde1e99cad9";
                deviceUid = new BigInteger(nodeUid, 16).longValue();

                if (deviceUid != deviceClass.getDeviceUID())
                    return;

                lightStatus = byteQueue.pop();
                Log.w("Scann", deviceUid + "," + lightStatus);
                contentValues.put(COLUMN_DEVICE_STATUS, lightStatus == 1);
                if (deviceClass.getStatus() != (lightStatus == 1)) {
                    contentValues.put(COLUMN_DEVICE_PROGRESS, lightStatus == 1 ? 100 : 0);
                    deviceClass.setDeviceDimming(lightStatus == 1 ? 100 : 0);
                }
                deviceClass.setStatus(lightStatus == 1);
                this.lightStatus.setChecked(lightStatus == 1);
                if (lightStatus == 0) {
                    editLightStatus.setText("Light Status:Off");
                } else
                    editLightStatus.setText("Light Status:On");

                dialogBuilder
                        .withTitle("Light Status")
                        .withEffect(Effectstype.Newspager)
                        .withMessage("Light is " + (lightStatus == 0 ? "Off" : "On"))
                        .withButton1Text("OK")
                        .setButton1Click(v -> {
                            dialogBuilder.dismiss();
                        })
                        .show();

//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) + "");
//
                break;

            case LIGHT_STATE_COMMAND_RESPONSE:
                bytes1 = byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                nodeUid = bytesToHex(bytes1);
                Log.w("ScanningBeacon", nodeUid);
                //                                String s = "4d0d08ada45f9dde1e99cad9";
                deviceUid = new BigInteger(nodeUid, 16).longValue();

                if (deviceUid != deviceClass.getDeviceUID())
                    return;

                lightStatus = byteQueue.pop();
                Log.w("Scann", deviceUid + "," + lightStatus);

                if (lightStatus == 0) {
                    boolean changedStatus = !deviceClass.getStatus();
                    contentValues.put(COLUMN_DEVICE_STATUS, changedStatus);
                    contentValues.put(COLUMN_DEVICE_PROGRESS, changedStatus ? 100 : 0);
                    deviceClass.setDeviceDimming(changedStatus ? 100 : 0);
                    deviceClass.setStatus(changedStatus);
                    editLightStatus.setText(String.format("Light Status:%s", deviceClass.getStatus() ? "On" : "Off"));


//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                    Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) + "");
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    this.lightStatus.setChecked(deviceClass.getStatus());
                    Toast.makeText(activity, "Cannot change light status", Toast.LENGTH_SHORT).show();

                }
//
                break;


            case LIGHT_LEVEL_RESPONSE:

                bytes1 = byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                nodeUid = bytesToHex(bytes1);
                Log.w("ScanningBeacon", nodeUid);
                //                                String s = "4d0d08ada45f9dde1e99cad9";
                deviceUid = new BigInteger(nodeUid, 16).longValue();
                if (deviceUid != deviceClass.getDeviceUID())
                    return;
                lightStatus = byteQueue.pop();
                Log.w("Scann", deviceUid + "," + lightStatus);

//            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
                contentValues.put(COLUMN_DEVICE_PROGRESS, lightStatus);
                deviceClass.setDeviceDimming(lightStatus);
                contentValues.put(COLUMN_DEVICE_STATUS, lightStatus>1?1:0);
                deviceClass.setStatus(lightStatus>1);

                if(deviceClass.getStatus())
                {
                    if(!this.lightStatus.isChecked())
                        this.lightStatus.setChecked(true);
                }
                else {
                    if(this.lightStatus.isChecked())
                        this.lightStatus.setChecked(false);
                }
                showAlert(String.format("Light level of %s is %s.", deviceClass.getDeviceName(), lightStatus));
//                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) +"");
//
                break;

            case SELECT_MASTER_RESPONSE:

                lightStatus = byteQueue.pop();


//            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
                contentValues.put(COLUMN_DEVICE_MASTER_STATUS, lightStatus == 0 ? 1 : 0);
                deviceClass.setMasterStatus(lightStatus == 0 ? 1 : 0);
//                deviceClass.(lightStatus);
                showAlert(String.format("Light '%s' is %s as master.", deviceClass.getDeviceName(), lightStatus == 0 ? "set " : "not set"));
//                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

//                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) +"");
//
                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:

//                    bytes1=byteQueue.pop4B();
//                    ArrayUtilities.reverse(bytes1);
//                    nodeUid=bytesToHex(bytes1);
//                    Log.w("ScanningBeacon",nodeUid);
//                    //                                String s = "4d0d08ada45f9dde1e99cad9";
//                    deviceUid = new BigInteger(nodeUid, 16).longValue();
//
//                    if(deviceUid!=deviceClass.getDeviceUID())
//                        return;

                lightStatus = byteQueue.pop();
                Log.w("Scann", "," + lightStatus);

//            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
                if (lightStatus == 0) {
                    deviceClass.setDeviceDimming(levelProgress);
                    contentValues.put(COLUMN_DEVICE_PROGRESS, levelProgress);
                    contentValues.put(COLUMN_DEVICE_STATUS, 1);
                    deviceClass.setStatus(true);

                    if (!this.lightStatus.isChecked())
                        this.lightStatus.setChecked(true);
                    editLightStatus.setText(String.format("Light Status:%s", deviceClass.getStatus() ? "On" : "Off"));

                    Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(deviceClass.getDeviceUID(), contentValues) + "");
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(activity, "Could not change the lighting level", Toast.LENGTH_SHORT).show();
                }
//
                break;

            case UPDATE_GROUP_RESPONSE:
                int requestStatus = byteQueue.pop();
                if (requestStatus == 0) {
//                    saveData();
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(activity, "Cannot Update group", Toast.LENGTH_SHORT).show();
//                contentValues.put(COLUMN_GROUP_PROGRESS,seekBarProgress);
//                Log.w("DashGroup",AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(),contentValues)+"");
                break;

            case GROUP_RESPONSE:

                bytes1 = byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                String nodeUid2 = bytesToHex(bytes1);
                Log.w("ScanningBeacon", nodeUid2);
                //                                String s = "4d0d08ada45f9dde1e99cad9";
                long deviceUid2 = new BigInteger(nodeUid2, 16).longValue();
                int lightStatus2 = byteQueue.pop();
                Log.w("Scann", deviceUid2 + "," + lightStatus2);
                deviceClass.setGroupId(lightStatus2);

                if (lightStatus2 == 0)
                    showAlert("Light has no group");
                else
                    showAlert(String.format("Group of %s is %s.", deviceClass.getDeviceName(), getGroup(lightStatus2).getGroupName()));


//                    contentValues.put(COLUMN_DEVICE_STATUS, lightStatus2==1);
//                contentValues.put(COLUMN_GROUP_PROGRESS,seekBarProgress);
//                Log.w("DashGroup",AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(),contentValues)+"");
                break;
        }

    }

    void showAlert(String message) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        dialogBuilder

                .withEffect(Effectstype.Fall)
                .withMessage(message)
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setMessage(message);
////                .setTitle("Remove Light");
//        builder.setPositiveButton("OK", (dialog1, id) -> {
//            // User clicked OK button
////            acceptRequest(2,position);
//            dialog1.dismiss();
//
//
////            Toast.makeText(activity, "Will be soon", Toast.LENGTH_SHORT).show();
//
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }

    @Override
    public void onScanFailed(int errorCode) {

        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();

        switch (requestCode) {

            case LIGHT_STATE_COMMAND_RESPONSE:

                this.lightStatus.setChecked(deviceClass.getStatus());

//
                break;


            case LIGHT_LEVEL_COMMAND_RESPONSE:


//                    deviceClass.setDeviceDimming(levelProgress);

//
                break;

            case UPDATE_GROUP_RESPONSE:

                Toast.makeText(activity, "Cannot Update group", Toast.LENGTH_SHORT).show();
//                contentValues.put(COLUMN_GROUP_PROGRESS,seekBarProgress);
//                Log.w("DashGroup",AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(),contentValues)+"");
                break;


        }

        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        dialogBuilder
                .withTitle("Timeout")
                .withEffect(Effectstype.Newspager)
                .withMessage("Timeout,Please check your beacon is in range")
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
//        saveData();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();
    }
    public void hideKeyboard()

    {

        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null)
        {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
