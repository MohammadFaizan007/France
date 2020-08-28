package com.SmartLightSensor.PogoClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupDetailsClass implements Parcelable {
    String groupName="";
    int groupId=0;
    boolean groupStatus=false;
    int groupDimming=0;

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupDimming(int groupDimming) {
        this.groupDimming = groupDimming;
    }

    public int getGroupDimming() {
        return groupDimming;
    }

    public boolean getGroupStatus() {
        return groupStatus;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupStatus(boolean groupStatus) {
        this.groupStatus = groupStatus;
    }

    public GroupDetailsClass() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupName);
        dest.writeInt(this.groupId);
        dest.writeByte(this.groupStatus ? (byte) 1 : (byte) 0);
        dest.writeInt(this.groupDimming);
    }

    protected GroupDetailsClass(Parcel in) {
        this.groupName = in.readString();
        this.groupId = in.readInt();
        this.groupStatus = in.readByte() != 0;
        this.groupDimming = in.readInt();
    }

    public static final Creator<GroupDetailsClass> CREATOR = new Creator<GroupDetailsClass>() {
        @Override
        public GroupDetailsClass createFromParcel(Parcel source) {
            return new GroupDetailsClass(source);
        }

        @Override
        public GroupDetailsClass[] newArray(int size) {
            return new GroupDetailsClass[size];
        }
    };
}
