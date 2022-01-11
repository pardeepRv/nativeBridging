import React, {useState} from 'react';
import {
  Button,
  NativeModules,
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';

const {Torch} = NativeModules;

const AppNew = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const [isOn, setisOn] = useState(false);

  console.log(Torch, 'TorchTorch');
  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const turnOn = () => {
    Torch.turnOn();
    setisOn(true);
  };

  const turnOff = () => {
    Torch.turnOff();
    setisOn(false);
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />

      <View
        style={{
          alignItems: 'center',
        }}>
        <Text style={{fontSize: 24}}>Torch is {isOn ? 'ON' : 'OFF'}</Text>
        {!isOn ? (
          <Button onPress={turnOn} title="Switch ON " color="green" />
        ) : (
          <Button onPress={turnOff} title="Switch OFF " color="red" />
        )}
      </View>
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
});

export default AppNew;
