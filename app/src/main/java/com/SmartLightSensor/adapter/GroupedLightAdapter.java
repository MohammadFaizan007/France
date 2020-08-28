package com.SmartLightSensor.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
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

import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupedLight;
import com.SmartLightSensor.R;
import com.SmartLightSensor.activity.HelperActivity;
import com.SmartLightSensor.constant.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.SmartLightSensor.activity.AppHelper.sqlHelper;


public class GroupedLightAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<GroupedLight> arrayList;
    ProgressDialog progressDialog;

    public GroupedLightAdapter(@NonNull Activity context) {
        activity = context;
        arrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
    }
    public void setList(ArrayList<GroupedLight> arrayList1) {
        arrayList.clear();
        arrayList.addAll(arrayList1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public GroupedLight getItem(int position) {
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
                    inflate(R.layout.group_light_adapter, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(convertView);
        GroupedLight groupLight=arrayList.get(position);

        viewHolder.LightName.setText(groupLight.getDeviceName());
        viewHolder.groupName.setText(groupLight.getGroupName());
        viewHolder.lightDetails.setBackground(activity.getResources().getDrawable(groupLight.getMasterStatus()==0?R.drawable.white_circle_border:R.drawable.yellow_circle));
        viewHolder.lightDetails.setOnClickListener(v ->
        {
            DeviceClass deviceClass=new DeviceClass();
            Cursor cursor = sqlHelper.getLightDetails(groupLight.getDeviceUid());
            if (cursor.moveToFirst())
            {
                do{

                    deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                    deviceClass.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                    deviceClass.setDeriveType(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DERIVE_TYPE)));
                    deviceClass.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_PROGRESS)));
                    deviceClass.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                    deviceClass.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_STATUS))==1);
                    deviceClass.setMasterStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS)));

                    // do what ever you want here
                }
                while(cursor.moveToNext());
            }
            cursor.close();

            if(deviceClass.getDeviceUID()<=0)
            {
                Toast.makeText(activity, "Some error,Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(activity, HelperActivity.class);
            intent.putExtra(Constants.MAIN_KEY, Constants.EDIT_LIGHT);
            intent.putExtra(Constants.LIGHT_DETAIL_KEY,deviceClass);
            activity.startActivity(intent);

        });

//        viewHolder.dashboardCustomize.setOnClickListener(view -> showDialog(position));
//        Picasso.with(activity).load(IMAGE_URL + friendsDetails.getUserImage()).placeholder(R.drawable.ic_user_male_icon_2).error(R.drawable.ic_user_male_icon_2).into(viewHolder.friendsProfile);


        return convertView;
    }

    void showAlert(int position, String message, String title) {
        if (title.length() < 1)
            title = "Alert";
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton("Block", (dialog1, id) -> {
            // User clicked OK button
//            acceptRequest(2,position);
            dialog1.dismiss();

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    static class ViewHolder {
        @BindView(R.id.grouped_light_name)
        TextView LightName;

        @BindView(R.id.group_name)
        TextView groupName;

        @BindView(R.id.customize_device)
        Button dashboardCustomize;


        @BindView(R.id.review_1)
        ImageView lightDetails;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }



}
