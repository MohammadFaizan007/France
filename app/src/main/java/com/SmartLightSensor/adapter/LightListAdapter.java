package com.SmartLightSensor.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;
import com.SmartLightSensor.EncodeDecodeModule.RxMethodType;
import com.SmartLightSensor.Fragments.EditGroupFragment;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_ID;
import static com.SmartLightSensor.EncodeDecodeModule.TxMethodType.ADD_GROUP_RESPONSE;


public class LightListAdapter extends BaseAdapter implements AdvertiseResultInterface, ReceiverResultInterface {
    Activity activity;
    ArrayList<DeviceClass> arrayList;
    String TAG = this.getClass().getSimpleName();
    int requestCode;
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int seekBarProgress = 0;
    int selectedPosition = 0;
    boolean advertise = true;
    GroupDetailsClass groupDetailsClass;
    EditGroupFragment editGroupFragment;

    public LightListAdapter(@NonNull Activity context, GroupDetailsClass groupDetailsClass, EditGroupFragment editDeviceFragment) {
        activity = context;
        arrayList = new ArrayList<>();
        scannerTask = new ScannerTask(activity, this);
        animatedProgress = new AnimatedProgress(activity);
        animatedProgress.setCancelable(false);
        this.groupDetailsClass=groupDetailsClass;
        this.editGroupFragment=editDeviceFragment;


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
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).
                    inflate(R.layout.light_list_item, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(convertView);
        DeviceClass deviceClass = arrayList.get(position);
        viewHolder.lightDeviceName.setText(deviceClass.getDeviceName());
        viewHolder.lightDetails.setBackground(activity.getResources().getDrawable(deviceClass.getMasterStatus()==0?R.drawable.white_circle_border:R.drawable.yellow_circle));
//        viewHolder.lightDetails.setOnClickListener(v -> {
//            Intent intent = new Intent(activity, HelperActivity.class);
//            intent.putExtra(Constants.MAIN_KEY, Constants.EDIT_LIGHT);
//            intent.putExtra(Constants.LIGHT_DETAIL_KEY, deviceClass);
//            activity.startActivity(intent);
//        });

        viewHolder.lightAdd.setOnClickListener(v -> {
           showAlert(position);
        });

        return convertView;
    }


    @Override
    public void onSuccess(String message) {
//        advertise=false;
//        animatedProgress.showProgress();
        Log.w(TAG, "Advertising start");
    }

    @Override
    public void onFailed(String errorMessage) {
//        advertise=true;
//        arrayList.get(selectedPosition).setStatus();
        if (animatedProgress == null)
            return;
        ///
        if(AppHelper.IS_TESTING)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_GROUP_ID, groupDetailsClass.getGroupId());
            Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + "");
            arrayList.remove(selectedPosition);
            notifyDataSetChanged();
            editGroupFragment.onResume();
        }
     ////
        Toast.makeText(activity, "Advertising failed.", Toast.LENGTH_SHORT).show();
        animatedProgress.hideProgress();
        Log.w(TAG, "onScanFailed " + errorMessage);
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
//        Log.w(TAG, "Advertising stop");
        if (animatedProgress != null)
            animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();

        switch (resultCode) {
            case ADD_GROUP_RESPONSE:

                contentValues.put(COLUMN_GROUP_ID, groupDetailsClass.getGroupId());
                Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + "");
                arrayList.remove(selectedPosition);
                notifyDataSetChanged();
                editGroupFragment.onResume();

                break;
        }
    }

    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue) {
        advertise = true;
        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();
        ContentValues contentValues = new ContentValues();

        Log.w("MethodType", (int) byteQueue.pop() + "");

        int status = byteQueue.pop();

        Log.w("Scann",  "," + status);

        switch (successCode) {
            case ADD_GROUP_RESPONSE:
                if (status == 0)
                {
                    contentValues.put(COLUMN_GROUP_ID, groupDetailsClass.getGroupId());
                    Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues)+"");
                    arrayList.remove(selectedPosition);
                    notifyDataSetChanged();
                    editGroupFragment.onResume();
                } else {
                    Toast.makeText(activity, "Cannot add in group.", Toast.LENGTH_SHORT).show();
                }
                break;

        }

    }
    void showAlert(int position) {
        selectedPosition=position;

//            animatedProgress.showProgress();
        DeviceClass deviceClass=arrayList.get(position);




        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure to add "+deviceClass.getDeviceName()+" to group "+groupDetailsClass.getGroupName())
                .setTitle("Add to group");
        builder.setPositiveButton("Add", (dialog1, id) -> {
            // User clicked OK button
//            acceptRequest(2,position);
            dialog1.dismiss();
            ByteQueue byteQueue=new ByteQueue();
            byteQueue.push(RxMethodType.ADD_GROUP);
            byteQueue.pushU4B(deviceClass.getDeviceUID());
            byteQueue.push(groupDetailsClass.getGroupId());
            AdvertiseTask advertiseTask;
            advertiseTask=new AdvertiseTask(this,activity);
            advertiseTask.setByteQueue(byteQueue);
            advertiseTask.setSearchRequestCode(ADD_GROUP_RESPONSE);
            advertiseTask.startAdvertising();

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onScanFailed(int errorCode) {
        if (animatedProgress != null)
        {
            animatedProgress.hideProgress();
        }
        advertise = true;

        if(AppHelper.IS_TESTING) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_GROUP_ID, groupDetailsClass.getGroupId());
            Log.w("DashGroup", AppHelper.sqlHelper.updateDevice(arrayList.get(selectedPosition).getDeviceUID(), contentValues) + "");
            arrayList.remove(selectedPosition);
            notifyDataSetChanged();
            editGroupFragment.onResume();


            notifyDataSetChanged();
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
//        Toast.makeText(activity, "Cannot get response from beacon ,make sure your beacon is in range ", Toast.LENGTH_SHORT).show();
        Log.w("StartFailed", errorCode + "");
    }


    static
    class ViewHolder {
        @BindView(R.id.light_details)
        ImageView lightDetails;
        @BindView(R.id.light_deviceName)
        TextView lightDeviceName;
        @BindView(R.id.light_add)
        Button lightAdd;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
