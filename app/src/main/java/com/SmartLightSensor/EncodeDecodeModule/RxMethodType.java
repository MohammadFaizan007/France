package com.SmartLightSensor.EncodeDecodeModule;

public class RxMethodType {

    public static final int LIGHT_INFO=0xEF;
    public static final int LIGHT_STATE=0x00;
    public static final int LIGHT_STATE_COMMAND=0x01;
    public static final int GROUP_STATE=0x02;
    public static final int GROUP_STATE_COMMAND=0x03;
    public static final int GROUP=0x04;
    public static final int ADD_GROUP=0x05;
    public static final int REMOVE_GROUP=0x06;
    public static final int UPDATE_GROUP=0x07;
    public static final int LIGHT_LEVEL=0x08;
    public static final int LIGHT_LEVEL_COMMAND=0x09;
    public static final int LIGHT_LEVEL_GROUP=0x0a;
    public static final int LIGHT_LEVEL_GROUP_COMMAND=0x0b;
    public static final int SELECT_MASTER=0x0c;
    public static final int ADD_ASSOCIATE=0x11;
    public static final int DETAILS_INFO=0x0c;
}
