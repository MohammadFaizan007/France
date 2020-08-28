package com.SmartLightSensor.activity;

import android.app.Application;
import android.util.Log;

import com.SmartLightSensor.DatabaseModule.SqlHelper;


public class AppHelper extends Application {
    public static SqlHelper sqlHelper;
    public static long SCANNING_INTERVAL=2*1000l;
    public static boolean IS_TESTING=false;
    @Override
    public void onCreate() {
        super.onCreate();
        sqlHelper=new SqlHelper(this);

    }




    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.w("AppHelper","terminate");
    }



}
