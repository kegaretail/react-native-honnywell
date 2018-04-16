import {NativeModules, DeviceEventEmitter} from 'react-native';

import { Barcodes } from './Barcodes';

const Scanner = NativeModules.BarcodeScanner;

let instance = null;

class BarcodeScannerClass {

    constructor() {
        if(!instance){

            this.onBarcode = null;

            instance = this;

            DeviceEventEmitter.addListener('BarcodeEvent', this.onBarcodeEvent);
            DeviceEventEmitter.addListener('FailureEvent', this.onFailureEvent);
            DeviceEventEmitter.addListener('TriggerEvent', this.onTriggerEvent);

        }

        return instance;
    }

    setBarcodes = (enabled_barcodes) => {
    
        let codes = {};
        Object.keys(Barcodes).forEach((key) => {
            codes[Barcodes[key]] = enabled_barcodes.includes(Barcodes[key])
        });

        Scanner.setBarcodes(codes);

    }

    resetBarcodes = () => {
    
        let codes = {};
        Object.keys(Barcodes).forEach((key) => {
            codes[Barcodes[key]] = true;
        });

        Scanner.setBarcodes(codes);

    }

    setProperties = () => {

    }

    onBarcodeEvent = (data) => {
        console.log('[BarcodeScanner]', data);
        if (this.onBarcode) {
            this.onBarcode(data);
        }
    }

    onFailureEvent = () => {

    }

    onTriggerEvent = () => {

    }

    claim = (callback) => {
        this.onBarcode = callback;
    }

    release() {
        this.onBarcode = null;
    }

    getDefaultProperties = () => {
        Scanner.getDefaultProperties((properties) => {
            console.log('==> ',properties);
        });
    }
}

export let BarcodeScanner = new BarcodeScannerClass();
export default BarcodeScanner;