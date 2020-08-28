package com.SmartLightSensor.Fragments;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.SmartLightSensor.DatabaseModule.DatabaseConstant;
import com.SmartLightSensor.PogoClasses.DeviceClass;
import com.SmartLightSensor.PogoClasses.GroupDetailsClass;
import com.SmartLightSensor.PogoClasses.GroupedLight;
import com.SmartLightSensor.R;
import com.SmartLightSensor.adapter.DashboardItemAdapter;
import com.SmartLightSensor.adapter.GroupedLightAdapter;
import com.SmartLightSensor.adapter.IndividualLightAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.SmartLightSensor.activity.AppHelper.sqlHelper;


public class DashboardFragment extends Fragment {
    @BindView(R.id.dashboard_home_setting_layout)
    LinearLayout dashboardHomeSettingLayout;
    @BindView(R.id.group_list_layout)
    LinearLayout groupListLayout;
    @BindView(R.id.individual_light_list_layout)
    LinearLayout individualLightListLayout;
    @BindView(R.id.dashboard_spinner)
    Spinner dashboardSpinner;
    @BindView(R.id.dash_group_list)
    ListView dashboargItemList;

//    @BindView(R.id.grouped_light_list)
//    NonScrollListView groupedLightList;



    @BindView(R.id.individual_light_list)
    ListView individualLightList;

    Unbinder unbinder;
    DashboardItemAdapter dashboardItemAdapter;

    IndividualLightAdapter individualLightAdapter;

    GroupedLightAdapter groupedLightAdapter;

    Activity activity;
    ArrayList<DeviceClass> deviceList;
    ArrayAdapter<GroupDetailsClass> adapter;
    ArrayList<GroupDetailsClass> list;
    ArrayList<GroupDetailsClass> spinnerList;
    ArrayList<GroupedLight> groupedLightArrayList;

    public DashboardFragment() {
        // Required empty public constructor
        list=new ArrayList<>();
        groupedLightArrayList=new ArrayList<>();
        deviceList=new ArrayList<>();
        spinnerList=new ArrayList<>();

    }
    public void setSpinner()
    {
        spinnerList.clear();
        GroupDetailsClass groupData = new GroupDetailsClass();
        groupData.setGroupId(1);
        groupData.setGroupName("All Group");
        GroupDetailsClass lightData = new GroupDetailsClass();
        lightData.setGroupId(2);
        lightData.setGroupName("All Light");
        spinnerList.add(groupData);
        spinnerList.add(lightData);
        adapter.notifyDataSetChanged();
    }
    public void getAllGroups() {
        setSpinner();
        list.clear();
        Cursor cursor = sqlHelper.getAllGroup();
        if (cursor.moveToFirst())
        {
            do {
                GroupDetailsClass groupData = new GroupDetailsClass();
                groupData.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                groupData.setGroupDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_PROGRESS)));
                groupData.setGroupName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_NAME)));
                groupData.setGroupStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_STATUS)) == 1);
                list.add(groupData);
                // do what ever you want here
            }
            while (cursor.moveToNext());
        }

//
        cursor.close();
//        adapter.notifyDataSetChanged();
        dashboardItemAdapter.setList(list);
    }

    public void getAllGroupedLight()
    {
        groupedLightArrayList.clear();

        Cursor cursor = sqlHelper.getAllGroupLight();

        if (cursor.moveToFirst())
        {
            do {
                GroupedLight groupData = new GroupedLight();
                groupData.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                groupData.setGroupName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_NAME)));
                groupData.setMasterStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS)));
                groupData.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                groupData.setDeviceUid(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                groupedLightArrayList.add(groupData);
                // do what ever you want here
            }
            while (cursor.moveToNext());
        }

//
        cursor.close();
        groupedLightAdapter.setList(groupedLightArrayList);
    }


    public void getDevice()
    {
        deviceList=new ArrayList<>();
        Cursor cursor=sqlHelper.getAllDevice(DatabaseConstant.ADD_DEVICE_TABLE);
        if (cursor.moveToFirst())
        {
            do{
                DeviceClass deviceClass=new DeviceClass();
                deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
                deviceClass.setDeviceUID(cursor.getLong(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_UID)));
                deviceClass.setDeriveType(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DERIVE_TYPE)));
                deviceClass.setDeviceDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_PROGRESS)));
                deviceClass.setGroupId(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_ID)));
                deviceClass.setMasterStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_MASTER_STATUS)));
                deviceClass.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_STATUS))==1);
//            Log.w("DeviceType",deviceClass.getDeriveType());
                deviceList.add(deviceClass);
                // do what ever you want here
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        individualLightAdapter.setList(deviceList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getDevice();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);

        activity=getActivity();
        if (activity==null)
            return view;
        dashboardItemAdapter =new DashboardItemAdapter(activity);
        individualLightAdapter =new IndividualLightAdapter(activity);
        groupedLightAdapter =new GroupedLightAdapter(activity);


//        adapter=new ArrayAdapter<GroupDetailsClass>(this, R.layout.spinerlayout, list);
        adapter=new ArrayAdapter<GroupDetailsClass>(activity,R.layout.spinerlayout,spinnerList){
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the spinner collapsed item (non-popup item) as a text view
                TextView tv = (TextView) super.getView(position, convertView, parent);

                // Set the text color of spinner item
                tv.setTextColor(Color.GRAY);
                tv.setText(spinnerList.get(position).getGroupName());
                // Return the view
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                // Cast the drop down items (popup items) as text view
                TextView tv = (TextView) super.getDropDownView(position,convertView,parent);

                // Set the text color of drop down items
                tv.setTextColor(Color.BLACK);
                tv.setText(spinnerList.get(position).getGroupName());

                /*// If this item is selected item
                if(position == mSelectedIndex){
                    // Set spinner selected popup item's text color
                    tv.setTextColor(Color.BLUE);
                }*/

                // Return the modified view
                return tv;
            }
        };


       /* adapter = ArrayAdapter.createFromResource(activity,
                R.array.dashboard_spinner, R.layout.spinerlayout);
       */
//       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dashboargItemList.setAdapter(dashboardItemAdapter);
//        groupedLightList.setAdapter(groupedLightAdapter);
        individualLightList.setAdapter(individualLightAdapter);
        dashboardSpinner.setAdapter(adapter);
        dashboardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                getAllGroups();
//        getAllGroupedLight();
                getDevice();

                if(position==0)
                {
                    groupListLayout.setVisibility(View.VISIBLE);
                    individualLightListLayout.setVisibility(View.GONE);
                }
                else {
                    groupListLayout.setVisibility(View.GONE);
                    individualLightListLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        dashboardSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if(position==0)
//                {
//                    groupListLayout.setVisibility(View.VISIBLE);
//                    individualLightListLayout.setVisibility(View.GONE);
//                }
//                else {
//                    groupListLayout.setVisibility(View.GONE);
//                    individualLightListLayout.setVisibility(View.VISIBLE);
//                }
//            }
//        });
        return view;
    }

    @Override
    public void onResume() {
        getAllGroups();
//        getAllGroupedLight();
        getDevice();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

//    @OnClick(R.id.dashboard_home_setting_layout)
//    public void onViewClicked() {
//    }
}
