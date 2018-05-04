
# react-native-honnywell

## Getting started

`$ npm install react-native-honnywell --save`

### Mostly automatic installation

`$ react-native link react-native-honnywell`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import nl.kega.RNHonnywellPackage;` to the imports at the top of the file
  - Add `new RNHonnywellPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-honnywell'
  	project(':react-native-honnywell').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-honnywell/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-honnywell')
  	```

## Usage
```javascript

import { BarcodeScanner, Barcodes } from 'react-native-honnywell';
BarcodeScanner.claim((data) => {
           
});

```
  
