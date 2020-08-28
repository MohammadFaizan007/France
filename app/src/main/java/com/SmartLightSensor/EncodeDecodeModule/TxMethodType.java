package com.SmartLightSensor.EncodeDecodeModule;

public class TxMethodType {
    public static final int info=0x4f;
    public static final int LIGHT_STATE_RESPONSE=0x50;
    public static final int LIGHT_STATE_COMMAND_RESPONSE=0x51;
    public static final int GROUP_STATE_RESPONSE=0x52;
    public static final int GROUP_STATE_COMMAND_RESPONSE=0x53;
    public static final int GROUP_RESPONSE=0x54;
    public static final int ADD_GROUP_RESPONSE=0x55;
    public static final int REMOVE_GROUP_RESPONSE=0x56;
    public static final int UPDATE_GROUP_RESPONSE=0x57;
    public static final int LIGHT_LEVEL_RESPONSE=0x58;
    public static final int LIGHT_LEVEL_COMMAND_RESPONSE=0x59;
    public static final int LIGHT_LEVEL_GROUP_RESPONSE=0x5a;
    public static final int LIGHT_LEVEL_GROUP_COMMAND_RESPONSE=0x5b;
    public static final int SELECT_MASTER_RESPONSE=0x5c;
}
