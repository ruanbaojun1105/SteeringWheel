// IBackService.aidl
package com.hwx.wheel.steeringwheel.tcp;

// Declare any non-default types here with import statements

interface IBackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean sendMessage(String message);
    //boolean sendMessage(byte[] data);
}
