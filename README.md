
# react-native-honnywell

## Getting started

`$ npm install react-native-honnywell --save`

### Mostly automatic installation

`$ react-native link react-native-honnywell`

## Usage
```javascript
import { BarcodeScanner, Barcodes } from 'react-native-honnywell';

// Set specific barcode types to scan.
BarcodeScanner.setBarcodes([
	Barcodes.EAN_13,
	Barcodes.QR_CODE
]);

BarcodeScanner.claim((data) => {
	console.log(data);
});
```
  