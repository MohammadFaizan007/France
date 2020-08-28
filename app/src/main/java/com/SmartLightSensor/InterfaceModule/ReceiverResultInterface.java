package com.SmartLightSensor.InterfaceModule;


import com.SmartLightSensor.EncodeDecodeModule.ByteQueue;

public interface ReceiverResultInterface {

    void onScanSuccess(int successCode, ByteQueue byteQueue);
    void onScanFailed(int errorCode);


}
