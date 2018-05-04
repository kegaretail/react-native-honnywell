
package nl.kega;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.*;

import android.graphics.Point;
import android.util.Log;

public class BarcodeScanner extends ReactContextBaseJavaModule implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener, LifecycleEventListener  {

    private final ReactApplicationContext context;

    private BarcodeReader barcodeReader;
    private AidcManager manager;

    public BarcodeScanner(ReactApplicationContext context) {
        super(context);

        this.context = context;
        this.context.addLifecycleEventListener(this);

        Log.e("Scanner", "BarcodeScanner");

        AidcManager.create(context, new CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                Log.e("Scanner", "AidcManager onCreated");
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                initScanner();
            }
        });

    }

    private void initScanner() {
        if (barcodeReader != null) {

            Log.e("Scanner", "initScanner");

            // register bar code event listener
            barcodeReader.addBarcodeListener(this);

            // set the trigger mode to client control
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
            } catch (UnsupportedPropertyException e) {
  
            }
            // register trigger state change listener
            barcodeReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);

            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Disable bad read response, handle in onFailureEvent
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);

            // Apply the settings
            barcodeReader.setProperties(properties);
        }
    }

    @Override
    public String getName() {
        return "BarcodeScanner";
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        Log.e("Scanner", "onBarcodeEvent ");
        Log.e("Scanner", "Barcode data: " + event.getBarcodeData());
        Log.e("Scanner", "Character Set: " + event.getCharset());
        Log.e("Scanner", "Code ID: " + event.getCodeId());
        Log.e("Scanner", "AIM ID: " + event.getAimId());
        Log.e("Scanner", "Timestamp: " + event.getTimestamp());

        WritableMap barcode_event = Arguments.createMap();
        barcode_event.putString("name", "BarcodeEvent");
        barcode_event.putString("data", event.getBarcodeData());
        barcode_event.putString("character", event.getCharset().toString());
        barcode_event.putString("code_id", event.getCodeId());
        barcode_event.putString("aim_id", event.getAimId());
        barcode_event.putString("timestamp", event.getTimestamp());

        WritableArray bounds = Arguments.createArray();
 
        List<Point> points = event.getBarcodeBounds();

        if(points != null) {
            for (Point point : points) {
                WritableMap newPoint = Arguments.createMap();
                newPoint.putString("x", String.valueOf(point.x));
                newPoint.putString("y", String.valueOf(point.y));
                bounds.pushMap(newPoint);
            }
        }

        barcode_event.putArray("bounds", bounds);

        this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("BarcodeEvent", barcode_event);
    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        Log.e("Scanner", "onTriggerEvent");

        WritableMap trigger_event = Arguments.createMap();
        trigger_event.putString("name", "TriggerEvent");

        this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("TriggerEvent", trigger_event);

        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(event.getState());
            barcodeReader.light(event.getState());
            barcodeReader.decode(event.getState());

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        Log.e("Scanner", "onFailureEvent");
    }

    @Override
    public void onHostResume() {
        if (barcodeReader != null) {
            try {
                Log.e("Scanner", "claim");
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onHostPause() {
        if (barcodeReader != null) {
            Log.e("Scanner", "release");
            barcodeReader.release();
        }
    }

    @Override
    public void onHostDestroy() {
        if (barcodeReader != null) {
            Log.e("Scanner", "onHostDestroy");
            barcodeReader.removeBarcodeListener(this);
            barcodeReader.removeTriggerListener(this);
            barcodeReader.close();
        }

        if (manager != null) {
            manager.close();
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (barcodeReader != null) {
            Log.e("Scanner", "onCatalystInstanceDestroy");
            barcodeReader.removeBarcodeListener(this);
            barcodeReader.removeTriggerListener(this);
            barcodeReader.close();
        }

        if (manager != null) {
            manager.close();
        }

    }
    
    @ReactMethod
    public void setBarcodes(ReadableMap barcodes) {

        try {
            if (barcodeReader != null) {
                ReadableMapKeySetIterator iterator = barcodes.keySetIterator();
                while (iterator.hasNextKey()) {
                    String key = iterator.nextKey();

                    try {
                        barcodeReader.setProperty(key, barcodes.getBoolean(key));
                    } catch (Exception e) {
                        Log.e(key, ""+barcodes.getBoolean(key));
                        //e.printStackTrace();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @ReactMethod
    public void setProperties() {

    }

    @ReactMethod
    public void getDefaultProperties(Callback callback) {
        try {
            if (barcodeReader != null) {
                Log.e("Scanner", "getDefaultProperties");
                //Log.e("Scanner", barcodeReader.getAllDefaultProperties());
                callback.invoke("test");
            }
        } catch (Exception e) {
  
        }
    }

    @ReactMethod
    public void getProperties(Callback callback) {
        try {
            if (barcodeReader != null) {
                callback.invoke(barcodeReader.getAllProperties());
            }
        } catch (Exception e) {
  
        }
    }

    @ReactMethod
    public void softwareTrigger(boolean state) {
        try {
            barcodeReader.softwareTrigger(state);
        } catch (Exception e) {
    
        }
    }

    

}