package com.SmartLightSensor.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.RxMethodType;
import com.SmartLightSensor.EncodeDecodeModule.TxMethodType;
import com.SmartLightSensor.InterfaceModule.AdvertiseResultInterface;
import com.SmartLightSensor.InterfaceModule.ReceiverResultInterface;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupDetailsClass;
import com.SmartLightSensor.R;
import com.SmartLightSensor.ServiceModule.AdvertiseTask;
import com.SmartLightSensor.ServiceModule.ScannerTask;
import com.SmartLightSensor.activity.AppHelper;
import com.SmartLightSensor.adapter.EditGroupAdapter;
import com.SmartLightSensor.adapter.LightListAdapter;
import com.niftymodaldialogeffects.Effectstype;
import com.niftymodaldialogeffects.NiftyDialogBuilder;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_STATUS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_PROGRESS;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_STATUS;
import static com.SmartLightSensor.EncodeDecodeModule.RxMethodType.LIGHT_LEVEL_GROUP_COMMAND;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.GROUP_STATE_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.GROUP_STATE_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_GROUP_COMMAND_RESPONSE;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_GROUP_RESPONSE;
import static com.SmartLightSensor.activity.AppHelper.sqlHelper;


public class EditGroupFragment extends Fragment implements ReceiverResultInterface, AdvertiseResultInterface {
    @BindView(R.id.edit_group_device_list)
    ListView editGroupDeviceList;

    @BindView(R.id.available_group_device_list)
    ListView availableGroupDevice;
    @BindView(R.id.edit_group_name)
    EditText groupName;
    @BindView(R.id.edit_group_status)
    TextView edit_group_status;
    @BindView(R.id.group_name_text)
    TextView groupNameText;
    @BindView(R.id.status_switch)
    JellyToggleButton groupStatus;
    EditGroupAdapter editGroupAdapter;
    LightListAdapter lightListAdapter;
    GroupDetailsClass groupDetailsClass;
    String TAG = this.getClass().getSimpleName();
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int levelProgress = 0;
    int requestCode = 0;
    @BindView(R.id.group_edit)
    ImageView groupEdit;
    @BindView(R.id.group_save)
    ImageView groupSave;
    @BindView(R.id.group_delete)
    ImageView groupDelete;
    Unbinder unbinder;
    Activity activity;

    public EditGroupFragment() {
        // Required empty public constructor
    }

    public void setGroupDetailsClass(GroupDetailsClass groupDetailsClass) {
        this.groupDetailsClass = groupDetailsClass;
        Log.w("GroupLavel", groupDetailsClass.getGroupDimming() + "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_group, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity = getActivity();

        editGroupAdapter = new EditGroupAdapter(activity, groupDetailsClass, this);
        lightListAdapter = new LightListAdapter(activity, groupDetailsClass, this);
        editGroupDeviceList.setAdapter(editGroupAdapter);
        availableGroupDevice.setAdapter(lightListAdapter);
        groupNameText.setText(groupDetailsClass.getGroupName());
        groupName.setText(groupDetailsClass.getGroupName());
        scannerTask = new ScannerTask(activity, this);
        animatedProgress = new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);


        if (groupDetailsClass.getGroupStatus())
            edit_group_status.setText(("Group Status:On"));
        else
            edit_group_status.setText(("Group Status:Off"));
        setLightStatus();
        hideKeyboard();
        return view;
    }

    public void getLightInGroup() {
        ArrayList<DeviceClass> arrayList = new ArrayList<>();
        Cursor cursor = sqlHelper.getLightInGroup(groupDetailsClass.getGroupId());
        if (cursor.moveToFirst()) {
            do {
                DeviceClass deviceClass = new DeviceClass();
                deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                deviceClass.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                deviceClass.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_PROGRESS)));
                deviceClass.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                deviceClass.setMasterStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS)));
                deviceClass.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_STATUS)) == 1);
                arrayList.add(deviceClass);
                // do what ever you want here
            } while (cursor.moveToNext());
        }
//       etList(deviceList);
        cursor.close();
        editGroupAdapter.setArrayList(arrayList);
    }

    public void getDevice() {
        ArrayList<DeviceClass> deviceList = new ArrayList<>();
        Cursor cursor = sqlHelper.getNonGroupDevice(DatabaseConstant.ADD_DEVICE_TABLE);
        if (cursor.moveToFirst()) {
            do {
                DeviceClass deviceClass = new DeviceClass();
                deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                deviceClass.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                deviceClass.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_PROGRESS)));
                deviceClass.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                deviceClass.setMasterStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS)));
                deviceClass.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_STATUS)) == 1);
                deviceList.add(deviceClass);
                // do what ever you want here
            } while (cursor.moveToNext());
        }
        cursor.close();
        lightListAdapter.setList(deviceList);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();
        getLightInGroup();
        getDevice();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.group_save,R.id.group_edit, R.id.group_delete, R.id.group_status, R.id.check_level, R.id.set_level})
    public void onViewClicked(View view) {
        ByteQueue byteQueue;
        AdvertiseTask advertiseTask;
        switch (view.getId()) {
            case R.id.group_status:

                byteQueue = new ByteQueue();
                byteQueue.push(RxMethodType.GROUP_STATE);
                byteQueue.push(groupDetailsClass.getGroupId());
                byteQueue.pushU4B(0x00);

                advertiseTask = new AdvertiseTask(this, activity);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(GROUP_STATE_RESPONSE);
                advertiseTask.startAdvertising();
                break;

            case R.id.group_edit:
                if (groupName.isEnabled()) {
                    groupName.setEnabled(false);
                    groupEdit.setVisibility(View.VISIBLE);
//                    groupDelete.setVisibility(View.GONE);
                    groupSave.setVisibility(View.GONE);
                } else {
                    groupName.setEnabled(true);
                    groupEdit.setVisibility(View.GONE);
//                    groupDelete.setVisibility(View.GONE);
                    groupSave.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.check_level:
                byteQueue = new ByteQueue();
                byteQueue.push(RxMethodType.LIGHT_LEVEL_GROUP);
                byteQueue.push(groupDetailsClass.getGroupId());
                byteQueue.pushU4B(0x00);

                advertiseTask = new AdvertiseTask(this, activity);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_LEVEL_GROUP_RESPONSE);
                advertiseTask.startAdvertising();
                break;
            case R.id.set_level:
                showDialog();
                break;
            case R.id.group_save:
                if (groupName.getText().toString().trim().length() < 1) {
                    groupName.setError("Group name can't empty");
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseConstant.COLUMN_GROUP_NAME, groupName.getText().toString());
                if (sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues)) {
                    Toast.makeText(activity, "Group Edited Successful.", Toast.LENGTH_SHORT).show();
                    groupName.setEnabled(false);
                    groupEdit.setVisibility(View.VISIBLE);
//                    groupDelete.setVisibility(View.GONE);
                    groupSave.setVisibility(View.GONE);
                } else
                    Toast.makeText(activity, "Some error to edit group", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_delete:
                deleteDialog();
//                ContentValues contentValues1 = new ContentValues();
//                contentValues1.put(DatabaseConstant.COLUMN_GROUP_ID, 0);
//                if (editGroupAdapter.getCount() < 1) {
//                    deleteGroup();
//                    return;
//                }
//                if (sqlHelper.removeLight(groupDetailsClass.getGroupId(), contentValues1)) {
////                    Log.w( "Delete Group",sqlHelper.deleteGroup(groupDetailsClass.getGroupId())+"");
//                    deleteGroup();
//
//                } else
//                    Toast.makeText(activity, "Some error to delete group.", Toast.LENGTH_SHORT).show();

                break;
        }
    }
    void deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure to delete group  "+ groupDetailsClass.getGroupName())
                .setTitle("Remove Group");
        builder.setPositiveButton("delete", (dialog1, id) -> {
            dialog1.dismiss();

            ContentValues contentValues1 = new ContentValues();
            contentValues1.put(DatabaseConstant.COLUMN_GROUP_ID, 0);
            if (editGroupAdapter.getCount() < 1) {
                deleteGroup();
                return;
            }
            if (sqlHelper.removeLight(groupDetailsClass.getGroupId(), contentValues1)) {
                Log.w( "Delete Group",sqlHelper.deleteGroup(groupDetailsClass.getGroupId())+"");
                deleteGroup();
            } else
                Toast.makeText(activity, "Some error to delete group.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void deleteGroup() {
        if (sqlHelper.deleteGroup(groupDetailsClass.getGroupId()) > 0) {
            Toast.makeText(activity, "Group deleted.", Toast.LENGTH_SHORT).show();
            activity.onBackPressed();
        } else
            Toast.makeText(activity, "Group deleted", Toast.LENGTH_SHORT).show();
            activity.onBackPressed();
    }
    @Override
    public void onSuccess(String message) {
//        animatedProgress.showProgress();
        Log.w(TAG, "Advertising start");
    }
    @Override
    public void onFailed(String errorMessage) {
        if (activity == null)
            return;
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        dialogBuilder
                .withTitle("Error")
                .withEffect(Effectstype.Newspager)
                .withMessage("Cannot advertise")
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
        animatedProgress.hideProgress();
        Log.w(TAG, "onScanFailed " + errorMessage);
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
//        requestCode = resultCode;
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
//        Log.w(TAG, "Advertising stop");
        //        requestCode = resultCode;
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
        if (animatedProgress != null)
            animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();
        ContentValues deviceContentValue = new ContentValues();
        switch (resultCode) {

            case GROUP_STATE_COMMAND_RESPONSE:

                boolean groupState = !(groupDetailsClass.getGroupStatus());
                contentValues.put(COLUMN_GROUP_STATUS, groupState);
                contentValues.put(COLUMN_GROUP_PROGRESS, groupState ? 100 : 0);

                this.groupStatus.setChecked(groupState);
                groupDetailsClass.setGroupStatus(groupState);
                groupDetailsClass.setGroupDimming(groupState ? 100 : 0);

                deviceContentValue.put(COLUMN_DEVICE_STATUS, groupState);
                deviceContentValue.put(COLUMN_DEVICE_PROGRESS, groupState ? 100 : 0);
                edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + "");
                Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(groupDetailsClass.getGroupId(), deviceContentValue) + "");
                getLightInGroup();
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

                break;

//            case LIGHT_LEVEL_GROUP_RESPONSE:
//
//
//                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + "");
//                showAlert(String.format("Light level of %s is %s", groupDetailsClass.getGroupName(), groupDetailsClass.getGroupDimming()));
//                break;

            case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:

                contentValues.put(COLUMN_GROUP_PROGRESS, levelProgress);
                contentValues.put(COLUMN_GROUP_STATUS, 1);

                deviceContentValue.put(COLUMN_DEVICE_STATUS, 1);
                deviceContentValue.put(COLUMN_DEVICE_PROGRESS, levelProgress);

                groupDetailsClass.setGroupDimming(levelProgress);
                groupDetailsClass.setGroupStatus(true);

                if (!(this.groupStatus.isChecked()))
                    this.groupStatus.setChecked(groupDetailsClass.getGroupStatus());
                edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + " , " + levelProgress);
                Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(groupDetailsClass.getGroupId(), deviceContentValue) + "");
                getLightInGroup();
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();

                break;

        }


    }

    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();
        ContentValues deviceContentValue = new ContentValues();
        Log.w("BYTEQUESIZE", byteQueue.size() + ",");
        Log.w("MethodType", (int) byteQueue.pop() + "");


        int groupId;
        int lightStatus;
        switch (successCode) {
            case GROUP_STATE_RESPONSE:
                groupId = byteQueue.pop();
                lightStatus = byteQueue.pop();
                Log.w("Scann", "," + lightStatus);
                contentValues.put(COLUMN_GROUP_STATUS, lightStatus == 1);

                if (groupDetailsClass.getGroupStatus() != (lightStatus == 1)) {

                    this.groupStatus.setChecked(lightStatus == 1);
                    groupDetailsClass.setGroupStatus(lightStatus == 1);
                    contentValues.put(COLUMN_GROUP_PROGRESS, lightStatus == 1 ? 100 : 0);
                    groupDetailsClass.setGroupDimming(lightStatus == 1 ? 100 : 0);

//                    deviceContentValue.put(COLUMN_DEVICE_STATUS,lightStatus == 1);
//                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, lightStatus == 1 ? 100 : 0);
                }

                edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                showAlert(String.format("%s is %s.", groupDetailsClass.getGroupName(), groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + "");
//                Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(groupDetailsClass.getGroupId(), deviceContentValue) + "");
//                getLightInGroup();
//
                break;
            case GROUP_STATE_COMMAND_RESPONSE:
                groupId = byteQueue.pop();
                lightStatus = byteQueue.pop();
                Log.w("Scann", "," + lightStatus);
                if (lightStatus == 0) {
                    boolean groupState = !(groupDetailsClass.getGroupStatus());
                    contentValues.put(COLUMN_GROUP_STATUS, groupState);
                    contentValues.put(COLUMN_GROUP_PROGRESS, groupState ? 100 : 0);

                    this.groupStatus.setChecked(groupState);
                    groupDetailsClass.setGroupStatus(groupState);
                    groupDetailsClass.setGroupDimming(groupState ? 100 : 0);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS, groupState);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, groupState ? 100 : 0);
                    edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + "");
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(groupDetailsClass.getGroupId(), deviceContentValue) + "");
                    getLightInGroup();
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    this.groupStatus.setChecked(groupDetailsClass.getGroupStatus());
                    showAlert("Cannot change state of " + groupDetailsClass.getGroupName());
                }
//
                break;

            case LIGHT_LEVEL_GROUP_RESPONSE:
                groupId = byteQueue.pop();
                lightStatus = byteQueue.pop();
                Log.w("Scann", "," + lightStatus);
                contentValues.put(COLUMN_GROUP_PROGRESS, lightStatus);
                contentValues.put(COLUMN_GROUP_STATUS, lightStatus>1?1:0);
                groupDetailsClass.setGroupDimming(lightStatus);
                groupDetailsClass.setGroupStatus(lightStatus>1);

                if(groupDetailsClass.getGroupStatus())
                {
                    if(!this.groupStatus.isChecked())
                        this.groupStatus.setChecked(true);
                }
                else {
                    if(this.groupStatus.isChecked())
                        this.groupStatus.setChecked(false);
                }
                edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + "");
                showAlert(String.format("Light level of %s is %s", groupDetailsClass.getGroupName(), groupDetailsClass.getGroupDimming()));
                break;

            case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:
                lightStatus = byteQueue.pop();

                Log.w("LEVEL_GROUP_COMMAND_R", "," + lightStatus + "," + levelProgress);
                if (lightStatus == 0)   //// success
                {
                    contentValues.put(COLUMN_GROUP_PROGRESS, levelProgress);
                    contentValues.put(COLUMN_GROUP_STATUS, 1);

                    deviceContentValue.put(COLUMN_DEVICE_STATUS, 1);
                    deviceContentValue.put(COLUMN_DEVICE_PROGRESS, levelProgress);

                    groupDetailsClass.setGroupDimming(levelProgress);
                    groupDetailsClass.setGroupStatus(true);

                    if (!(this.groupStatus.isChecked()))
                        this.groupStatus.setChecked(groupDetailsClass.getGroupStatus());
                    edit_group_status.setText(String.format("Group Status:%s", groupDetailsClass.getGroupStatus() ? "On" : "Off"));
                    Log.w("DashGroup", AppHelper.sqlHelper.updateGroup(groupDetailsClass.getGroupId(), contentValues) + " , " + levelProgress);
                    Log.w("DashGroup12", AppHelper.sqlHelper.updateGroupDevice(groupDetailsClass.getGroupId(), deviceContentValue) + "");
                    getLightInGroup();
                } else
                    showAlert("Cannot change light level of " + groupDetailsClass.getGroupName());
                break;

        }

    }

    @Override
    public void onScanFailed(int errorCode) {
        if (activity == null)
            return;

        switch (requestCode) {

            case GROUP_STATE_COMMAND_RESPONSE:

                this.groupStatus.setChecked(groupDetailsClass.getGroupStatus());


                break;


            case LIGHT_LEVEL_GROUP_COMMAND_RESPONSE:


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
        animatedProgress.hideProgress();
        Log.w(TAG, "onScanFailed " + errorCode);
    }

    public void setLightStatus() {
        groupStatus.setChecked(groupDetailsClass.getGroupStatus());
        groupStatus.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb) {

                boolean switchStatus = state != State.LEFT;
                if (groupDetailsClass.getGroupStatus() == switchStatus) {
//                    Log.w("Advertise","state is same");
                    return;
                }
                Log.w("GroupID", groupDetailsClass.getGroupId() + "");

                AdvertiseTask advertiseTask;
                ByteQueue byteQueue = new ByteQueue();
                byteQueue.push(RxMethodType.GROUP_STATE_COMMAND);       ////State Command
                byteQueue.push(groupDetailsClass.getGroupId());      ////  12 is static vale for Node id
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
                byteQueue.pushU3B(0x00);
                advertiseTask = new AdvertiseTask(EditGroupFragment.this, activity);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(TxMethodType.GROUP_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
            }
        });
    }

    void showDialog() {
        if (!groupDetailsClass.getGroupStatus())
            groupDetailsClass.setGroupDimming(0);

        levelProgress = groupDetailsClass.getGroupDimming();


        final Dialog dialog = new Dialog(activity);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customize_group);

        TextView deviceName = dialog.findViewById(R.id.customize_group_name);
        SeekBar seekBar = dialog.findViewById(R.id.customizeGroupSeekBar);
        Button button = dialog.findViewById(R.id.customiseGroupSave);
        TextView levelPercentage = dialog.findViewById(R.id.level_percentage);
        levelPercentage.setText(levelProgress + " %");
        seekBar.setProgress(levelProgress);
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
                byteQueue.push(LIGHT_LEVEL_GROUP_COMMAND);   //// Light Level Command method type
                byteQueue.push(groupDetailsClass.getGroupId());   ////deviceDetail.getGroupId()   node id;
                byteQueue.push(levelProgress);    ////0x00-0x64
                byteQueue.pushU3B(0x00);
//            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
                advertiseTask = new AdvertiseTask(EditGroupFragment.this, activity);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_LEVEL_GROUP_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
//            Log.w("IndividualLight",AppHelper.sqlHelper.updateDevice(deviceDetail.getDeviceUID(),contentValues)+"");

            }
        });
//        button.setOnClickListener(view ->
//        {
//            String hex = Integer.toHexString(levelProgress);
//            Log.w("IndividualLight", hex + " " + String.format("%02X", levelProgress));
//            AdvertiseTask advertiseTask;
//            ByteQueue byteQueue = new ByteQueue();
//            byteQueue.push(LIGHT_LEVEL_GROUP_COMMAND);   //// Light Level Command method type
//            byteQueue.push(groupDetailsClass.getGroupId());   ////deviceDetail.getGroupId()   node id;
//            byteQueue.push(levelProgress);    ////0x00-0x64
//            byteQueue.pushU3B(0x00);
////            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
//            advertiseTask = new AdvertiseTask(this, activity);
//            advertiseTask.setByteQueue(byteQueue);
//            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_GROUP_COMMAND_RESPONSE);
//            advertiseTask.startAdvertising();
////            Log.w("IndividualLight",AppHelper.sqlHelper.updateDevice(deviceDetail.getDeviceUID(),contentValues)+"");
//            dialog.dismiss();
//        });

        deviceName.setText(groupDetailsClass.getGroupName());

        dialog.show();

    }

    void showAlert(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
//                .setTitle("Remove Light");
        builder.setPositiveButton("OK", (dialog1, id) -> {
            // User clicked OK button
//            acceptRequest(2,position);
            dialog1.dismiss();


//            Toast.makeText(activity, "Will be soon", Toast.LENGTH_SHORT).show();

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public  void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
