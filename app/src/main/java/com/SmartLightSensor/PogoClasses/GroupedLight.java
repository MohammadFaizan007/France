package com.SmartLightSensor.PogoClasses;

public class GroupedLight {
    long deviceUid=0;

    String groupName="";
    String DeviceName="";
    int groupId=0;
    int masterStatus=0;

    public long getDeviceUid() {
        return deviceUid;
    }

    public void setDeviceUid(long deviceUid) {
        this.deviceUid = deviceUid;
    }

    public void setMasterStatus(int masterStatus) {
        this.masterStatus = masterStatus;
    }

    public int getMasterStatus() {
        return masterStatus;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

}
