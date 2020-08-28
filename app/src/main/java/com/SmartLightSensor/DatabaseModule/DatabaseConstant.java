package com.SmartLightSensor.DatabaseModule;

public class DatabaseConstant {
    //information of database
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "NimbusLit.db";             /// Database Name

            /******************** SINGLE DEVICE COLUMN KEYS  ****************/

    public static final String ADD_DEVICE_TABLE = "AddDeviceTable";         ////Device List Table
    public static final String GROUP_TABLE_NAME = "GroupTable";             ////Group Table
    public static final String COLUMN_ID = "KEY_ID";                        ///Table column
//    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";

    public static final String COLUMN_DEVICE_ID ="DEVICE_ID";             ////Device IDc
    public static final String COLUMN_DEVICE_UID ="DEVICE_UID";             ////Device UIDc
    public static final String COLUMN_DEVICE_STATUS ="DEVICE_STATUS";       ////Device Status (ON/OFF)
    public static final String COLUMN_DEVICE_MASTER_STATUS ="MASTER_STATUS";       ////Master Status (1/0)
    public static final String COLUMN_DEVICE_NAME ="DEVICE_NAME";           ////Device Name
    public static final String COLUMN_DEVICE_PROGRESS ="DEVICE_PROGRESS";   //// Device dim progress

    public static final String COLUMN_DEVICE_SITE_NAME ="DEVICE_SITE_NAME";
    public static final String COLUMN_DEVICE_BUILDING_NAME ="DEVICE_BUILDING_NAME";
    public static final String COLUMN_DEVICE_LEVEL_NAME ="DEVICE_LEVEL_NAME";
    public static final String COLUMN_DEVICE_ROOM_NAME ="DEVICE_ROOM_NAME";
    public static final String COLUMN_DEVICE_GROUP_NAME ="DEVICE_GROUP_NAME";


                /************************** GROUP COLUMN KEYS ***********************************/


    public static final String COLUMN_GROUP_ID ="GROUP_ID";                 //// Gorup Id
    public static final String COLUMN_GROUP_NAME ="GROUP_NAME";             ////Group Name
    public static final String COLUMN_DERIVE_TYPE ="DERIVE_TYPE";             ////Group Name
    public static final String COLUMN_GROUP_PROGRESS ="GROUP_PROGRESS";     ////Group Progress
    public static final String COLUMN_GROUP_STATUS ="GROUP_STATUS";         ////Group Status(ON/OFF)



    public static final String COLUMN_GROUP_DEVICE_LIST ="GroupDeviceList";

    /**** Creating Single Device Table ****/

    //+COLUMN_DEVICE_ID +"INTEGER PRIMARY KEY AUTOINCREMENT ,"
        public static final String CREATE_DEVICE_TABLE = "CREATE TABLE IF NOT EXISTS " + ADD_DEVICE_TABLE + "(" + COLUMN_DEVICE_UID +
                " INTEGER PRIMARY KEY ," + COLUMN_DEVICE_STATUS + " INTEGER DEFAULT 1 ,"+ COLUMN_DEVICE_MASTER_STATUS + " INTEGER DEFAULT 0 ,"  +COLUMN_DEVICE_PROGRESS + " INTEGER DEFAULT 100 ,"  +
            COLUMN_DEVICE_NAME + " TEXT ,"
            + COLUMN_DEVICE_SITE_NAME + " TEXT ,"
            + COLUMN_DEVICE_BUILDING_NAME + " TEXT ,"
            + COLUMN_DEVICE_LEVEL_NAME + " TEXT ,"
            + COLUMN_DEVICE_ROOM_NAME + " TEXT ,"
            + COLUMN_DEVICE_GROUP_NAME + " TEXT ,"
            + COLUMN_DERIVE_TYPE + " TEXT ,"
            + COLUMN_GROUP_ID + " INTEGER DEFAULT 0 "  +  ")";

    /**** Delete Table ****/
    public static final String DROP_TABLE=" DROP TABLE IF EXISTS ";

    /**** Creating Group Table ****/
    public static final String CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS " +
            GROUP_TABLE_NAME + "(" +
            COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"  +
            COLUMN_GROUP_NAME + " TEXT , " +
            COLUMN_DERIVE_TYPE + " TEXT , " +
            COLUMN_GROUP_PROGRESS + " INTEGER DEFAULT 0 ,"  +
            COLUMN_GROUP_STATUS + " INTEGER DEFAULT 0 "    +  ")";
}
