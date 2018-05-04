
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

    private boolean claimed = false;
    private boolean destroyed = false;
    private boolean softtrigger = false;

    public BarcodeScanner(ReactApplicationContext context) {
        super(context);

        this.context = context;
        this.context.addLifecycleEventListener(this);

        Log.e("Scanner", "BarcodeScanner");

        initManager();

    }

    private void initManager() {
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
        Log.e("Scanner", "onTriggerEvent " + softtrigger);

        WritableMap trigger_event = Arguments.createMap();
        trigger_event.putString("name", "TriggerEvent");

        this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("TriggerEvent", trigger_event);
        
        Log.e("Scanner", "event.getState() " + event.getState());

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

        // When backbutton is pressed and app goes to back onHostDestroy gets called
        if (destroyed) {
            initManager();
            destroyed = false;
        }

        if (barcodeReader != null && claimed) {
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
            try {
                Log.e("Scanner", "release");
                barcodeReader.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onHostDestroy() {

        destroyed = true;

        if (barcodeReader != null) {
            Log.e("Scanner", "onHostDestroy");
            barcodeReader.removeBarcodeListener(this);
            barcodeReader.removeTriggerListener(this);
            barcodeReader.close();
            barcodeReader = null;
            claimed = false;
        }

        if (manager != null) {
            manager.close();
            manager = null;
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (barcodeReader != null) {
            
            try {
                Log.e("Scanner", "onCatalystInstanceDestroy");
                barcodeReader.removeBarcodeListener(this);
                barcodeReader.removeTriggerListener(this);
                barcodeReader.close();
            } catch (IllegalStateException e) {
        
            }
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
            softtrigger = state;
            barcodeReader.softwareTrigger(state);
        } catch (Exception e) {
    
        }
    }

    @ReactMethod
    public void scanContinuous(boolean state) {
        try {
            if (state) {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_SCAN_MODE, BarcodeReader.TRIGGER_SCAN_MODE_CONTINUOUS);
            } else {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_SCAN_MODE, BarcodeReader.TRIGGER_SCAN_MODE_ONESHOT);
            }
            barcodeReader.softwareTrigger(state);

        } catch (Exception e) {
    
        }
    }

    @ReactMethod
    public void claim() {
        if (barcodeReader != null) {
            try {
                Log.e("Scanner", "claim");
                barcodeReader.claim();
                claimed = true;
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
        
            }
        }
    }

    @ReactMethod
    public void release() {
        if (barcodeReader != null) {
            try {
                Log.e("Scanner", "release");
                barcodeReader.release();
                claimed = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    

}