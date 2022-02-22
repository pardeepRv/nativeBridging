import React, { useState , useEffect } from 'react';
import {
  Button,
  NativeModules,
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  requireNativeComponent,
  Platform,
  TouchableOpacity
} from 'react-native';
import { Colors } from 'react-native/Libraries/NewAppScreen';

const { ActivityStarterModule } = NativeModules;

const { Torch } = NativeModules;
// const Android = requireNativeComponent('ToastModule');
const Switch = requireNativeComponent('Switch');


const AppNew = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const [isOn, setisOn] = useState(false);

  console.log(Torch, 'TorchTorch');
  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  // const _showToast = () => {
  //   ToastModule.showToast('This is a native toast!!');
  // }

  const turnOn = () => {
    Torch.turnOn();
    setisOn(true);
  };

  const turnOff = () => {
    Torch.turnOff();
    setisOn(false);
  };

  return (
    <SafeAreaView style={{flex: 1}}>
    <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      {Platform.OS === 'ios' ?(
      // (  <View
      //   style={{
      //     alignItems: 'center',
      //   }}>
      //   <Text style={{fontSize: 24}}>Torch is {isOn ? 'ON' : 'OFF'}</Text>
      //   {!isOn ? (
      //     <Button onPress={turnOn} title="Switch ON " color="green" />
      //   ) : (
      //     <Button onPress={turnOff} title="Switch OFF " color="red" />
      //   )}
      // </View>
      <Switch 
      style={{
          width:414,
          height:896,
          backgroundColor:'#0a8481'
      }}
      /> ) : (
            
        <View style={styles.container}>
        {/* <Android style={styles.javaBtn} isTurnedOn={true} />*/}
        {/* <Button
                onPress={() => NativeModules.ActivityStarter.navigateToExample()}
                title='Start example activity'
                style={{
                  width:414,
                  height:896
              }}
            /> */}
            <TouchableOpacity style={{
                  width:414,
                  height: 325,
                  backgroundColor:'red'
              }} 
              onPress={NativeModules.ActivityStarter.navigateToExample() }>
              {/* <Text>Invoke native Java code</Text> */}
         </TouchableOpacity>

     </View>
) }
    </SafeAreaView>

  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  container: {
    flex: 1,
    backgroundColor: 'pink',
    alignItems: 'center',
    justifyContent: 'center',
  },
  javaBtn: {
    height: 100,
    width: 300,
    backgroundColor: 'yellow',
  },
});

export default AppNew;
