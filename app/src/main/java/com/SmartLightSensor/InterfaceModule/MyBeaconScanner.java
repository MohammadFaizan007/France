package com.SmartLightSensor.InterfaceModule;


import com.SmartLightSensor.PogoClasses.BeconDeviceClass;

import java.util.ArrayList;

public interface MyBeaconScanner {
    void onBeaconFound(ArrayList<BeconDeviceClass> byteQueue);
    void noBeaconFound();
}
