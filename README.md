
# react-native-honnywell

## Getting started

`$ npm install react-native-honnywell --save`

### Mostly automatic installation

`$ react-native link react-native-honnywell`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-honnywell` and add `RNHonnywell.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNHonnywell.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

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

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNHonnywell.sln` in `node_modules/react-native-honnywell/windows/RNHonnywell.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Honnywell.RNHonnywell;` to the usings at the top of the file
  - Add `new RNHonnywellPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNHonnywell from 'react-native-honnywell';

// TODO: What to do with the module?
RNHonnywell;
```
  