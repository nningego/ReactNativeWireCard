/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React from 'react';
import {
  requireNativeComponent,
  StyleSheet,
  Text,
  View,
  UIManager,
  findNodeHandle,
} from 'react-native';

const fragmentIFace = {
  name: 'Fragment',
  propTypes: {
    ...View.propTypes,
  },
};

const CardFieldNative = requireNativeComponent('WirecardFormField', fragmentIFace);

export default class App extends React.Component {
  create = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.fragment),
      UIManager.WirecardFormField.Commands.create,
      [], // No args
    );
  };

  componentDidMount() {
    this.create();
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>First Name</Text>
        <Text style={styles.welcome}>Last Name</Text>
          <CardFieldNative
            ref={(field) => {
              this.fragment = field;
            }}
            {...this.props}
            style={[{ flex: 1, width: '100%' }]}
          />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
