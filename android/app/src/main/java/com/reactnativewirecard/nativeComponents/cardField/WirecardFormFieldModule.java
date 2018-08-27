package com.reactnativewirecard.nativeComponents.cardField;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import de.wirecard.paymentsdk.BuildConfig;

public class WirecardFormFieldModule extends ReactContextBaseJavaModule {

    private WirecardFormFieldManager wirecardFormFieldManager;

    WirecardFormFieldModule(ReactApplicationContext reactContext, WirecardFormFieldManager wirecardFormFieldManager) {
        super(reactContext);
        this.wirecardFormFieldManager = wirecardFormFieldManager;
    }

    @Override
    public String getName() {
        return "WirecardFormFieldModule";
    }

    @ReactMethod
    public void submitPayment(final Promise promise) {
        wirecardFormFieldManager.makeTransaction(promise);
    }
}