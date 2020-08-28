package com.SmartLightSensor.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.SmartLightSensor.activity.HelperActivity;
import com.SmartLightSensor.adapter.DashboardGroupAdapter;
import com.SmartLightSensor.adapter.GroupAdapter;
import com.SmartLightSensor.adapter.IndividualBuildingAdapter;
import com.SmartLightSensor.adapter.IndividualLevelAdapter;
import com.SmartLightSensor.adapter.IndividualLightAdapter;
import com.SmartLightSensor.adapter.IndividualRoomAdapter;
import com.SmartLightSensor.adapter.IndividualSiteAdapter;
import com.SmartLightSensor.constant.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.SmartLightSensor.activity.AppHelper.sqlHelper;

public class AvailableDeviceFragment extends Fragment {
    @BindView(R.id.group_list_layout)
    LinearLayout groupListLayout;
    @BindView(R.id.site_light_list_layout)
    LinearLayout site_light_list_layout;
    @BindView(R.id.building_light_list_layout)
    LinearLayout building_light_list_layout;
    @BindView(R.id.level_light_list_layout)
    LinearLayout level_light_list_layout;
    @BindView(R.id.room_light_list_layout)
    LinearLayout room_light_list_layout;
    @BindView(R.id.individual_light_list_layout)
    LinearLayout individualLightListLayout;
    @BindView(R.id.dashboard_spinner)
    Spinner dashboardSpinner;
    @BindView(R.id.individual_light_list)
    ListView individualLightList;
    @BindView(R.id.group_light_list)
    ListView groupLightList;
    @BindView(R.id.site_light_list)
    ListView site_light_list;
    @BindView(R.id.building_light_list)
    ListView building_light_list;
    @BindView(R.id.level_light_list)
    ListView level_light_list;
    @BindView(R.id.room_light_list)
    ListView room_light_list;

    Activity activity;
    Unbinder unbinder;
    @BindView(R.id.create_new_group)
    Button createNewGroup;
    ArrayList<DeviceClass> deviceList;
    ArrayAdapter<GroupDetailsClass> adapter;
    ArrayList<GroupDetailsClass> list;
    ArrayList<GroupDetailsClass> spinnerList;
    ArrayList<GroupedLight> groupedLightArrayList;
    IndividualLightAdapter individualLightAdapter;
    DashboardGroupAdapter dashboardGroupAdapter;
    IndividualSiteAdapter individualSiteAdapter;
    IndividualBuildingAdapter individualBuildingAdapter;
    IndividualLevelAdapter individualLevelAdapter ;
    IndividualRoomAdapter individualRoomAdapter ;
    GroupAdapter groupAdapter ;

    public AvailableDeviceFragment() {
        // Required empty public constructor
        list=new ArrayList<>();
        groupedLightArrayList=new ArrayList<>();
        deviceList=new ArrayList<>();
        spinnerList=new ArrayList<>();

    }

    public void setSpinner() {
        spinnerList.clear();
        GroupDetailsClass lightData = new GroupDetailsClass();
        lightData.setGroupId(1);
        lightData.setGroupName("All Light");
        GroupDetailsClass siteData = new GroupDetailsClass();
        siteData.setGroupId(2);
        siteData.setGroupName("All Site");
        GroupDetailsClass buildingData = new GroupDetailsClass();
        buildingData.setGroupId(3);
        buildingData.setGroupName("All Building");
        GroupDetailsClass levelData = new GroupDetailsClass();
        levelData.setGroupId(4);
        levelData.setGroupName("All Level");
        GroupDetailsClass roomData = new GroupDetailsClass();
        roomData.setGroupId(5);
        roomData.setGroupName("All Room");
        GroupDetailsClass groupData = new GroupDetailsClass();
        groupData.setGroupId(6);
        groupData.setGroupName("All Group");
        spinnerList.add(lightData);
        spinnerList.add(siteData);
        spinnerList.add(buildingData);
        spinnerList.add(levelData);
        spinnerList.add(roomData);
        spinnerList.add(groupData);
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
        groupAdapter.setList(list);
    }
    public void getDevice() {
    deviceList=new ArrayList<>();
    Cursor cursor=sqlHelper.getAllDevice(DatabaseConstant.ADD_DEVICE_TABLE);
    if (cursor.moveToFirst())
    {
        do{
            DeviceClass deviceClass=new DeviceClass();
            deviceClass.setDeviceName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_NAME)));
            deviceClass.setDeviceSite(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_SITE_NAME)));
            deviceClass.setDeviceBuilding(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_BUILDING_NAME)));
            deviceClass.setDeviceLevel(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_LEVEL_NAME)));
            deviceClass.setDeviceRoom(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_ROOM_NAME)));
            deviceClass.setDeviceGroup(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_DEVICE_GROUP_NAME)));
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
    individualSiteAdapter.setList(deviceList);
    individualBuildingAdapter.setList(deviceList);
    individualLevelAdapter.setList(deviceList);
    individualRoomAdapter.setList(deviceList);
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
        View view = inflater.inflate(R.layout.available_devices, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity=getActivity();
        if (activity==null)
            return view;
        individualLightAdapter =new IndividualLightAdapter(activity);
        individualSiteAdapter =new IndividualSiteAdapter(activity);
        individualBuildingAdapter =new IndividualBuildingAdapter(activity);
        individualLevelAdapter =new IndividualLevelAdapter(activity);
        individualRoomAdapter =new IndividualRoomAdapter(activity);
        groupAdapter =new GroupAdapter(activity);

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


        groupLightList.setAdapter(groupAdapter);
        individualLightList.setAdapter(individualLightAdapter);
        site_light_list.setAdapter(individualSiteAdapter);
        building_light_list.setAdapter(individualBuildingAdapter);
        level_light_list.setAdapter(individualLevelAdapter);
        room_light_list.setAdapter(individualRoomAdapter);
        dashboardSpinner.setAdapter(adapter);
        dashboardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                getAllGroups();
                getDevice();

                if(position==0) {
                    individualLightListLayout.setVisibility(View.VISIBLE);
                    groupListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.GONE);
                    building_light_list_layout.setVisibility(View.GONE);
                    level_light_list_layout.setVisibility(View.GONE);
                    room_light_list_layout.setVisibility(View.GONE);
                }
                else if (position==1){
                    individualLightListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.VISIBLE);
                    building_light_list_layout.setVisibility(View.GONE);
                    level_light_list_layout.setVisibility(View.GONE);
                    room_light_list_layout.setVisibility(View.GONE);
                    groupListLayout.setVisibility(View.GONE);
                }
                else if (position==2){
                    individualLightListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.GONE);
                    building_light_list_layout.setVisibility(View.VISIBLE);
                    level_light_list_layout.setVisibility(View.GONE);
                    room_light_list_layout.setVisibility(View.GONE);
                    groupListLayout.setVisibility(View.GONE);
                }

                else if (position==3){
                    individualLightListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.GONE);
                    building_light_list_layout.setVisibility(View.GONE);
                    level_light_list_layout.setVisibility(View.VISIBLE);
                    room_light_list_layout.setVisibility(View.GONE);
                    groupListLayout.setVisibility(View.GONE);
                }
                else if (position==4){
                    individualLightListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.GONE);
                    building_light_list_layout.setVisibility(View.GONE);
                    level_light_list_layout.setVisibility(View.GONE);
                    room_light_list_layout.setVisibility(View.VISIBLE);
                    groupListLayout.setVisibility(View.GONE);
                }
                else if (position==5){
                    individualLightListLayout.setVisibility(View.GONE);
                    site_light_list_layout.setVisibility(View.GONE);
                    building_light_list_layout.setVisibility(View.GONE);
                    level_light_list_layout.setVisibility(View.GONE);
                    room_light_list_layout.setVisibility(View.GONE);
                    groupListLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllGroups();
        getDevice();
    }

    @OnClick(R.id.create_new_group)
    public void onViewClicked() {
        Intent intent = new Intent(activity, HelperActivity.class);
        intent.putExtra(Constants.MAIN_KEY, Constants.CREATE_GROUP);
        activity.startActivity(intent);
    }
}

