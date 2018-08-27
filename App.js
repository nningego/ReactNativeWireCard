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
  DeviceEventEmitter,
  Button,
} from 'react-native';

const fragmentIFace = {
  name: 'Fragment',
  propTypes: {
    ...View.propTypes,
  },
};
import submitPayment from './EmbeddedCardField.submitPayment';

const CardFieldNative = requireNativeComponent('WirecardFormField', fragmentIFace);

const subscribeForNativeEvents = (eventID, callback) => {
  DeviceEventEmitter.addListener(eventID, callback);
};

export default class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      cardValid: false,
      success: false,
      error: false,
    }
  }

  create = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.fragment),
      UIManager.WirecardFormField.Commands.create,
      [], // No args
    );
  };

  componentDidMount() {
    this.create();

    subscribeForNativeEvents('nativeCardStatus', (event) => {
      if (event.cardFieldStatus === 'CARD_VALID') {
        this.setState({ cardValid: true });
      } else {
        this.setState({ cardValid: false });
      }
    });
  }

  submit = () => submitPayment()
    .then((response) => {
      console.log('response', response);
      this.setState({ success: true });
    })
    .catch((error) => {
      console.log('error', error);
      this.setState({ error: true });
    });

  doValidCardStuff = () => (
    <Button
      title="You can submit form now."
      onPress={this.submit}
      style={styles.submit}/>
  );

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
        {this.state.success && <Text style={styles.instructions}>Success</Text>}
        {this.state.error && <Text style={styles.instructions}>Error</Text>}
        {this.state.cardValid && this.doValidCardStuff()}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  submit: {
    fontSize: 20,
    textAlign: 'center',
    marginBottom: 80,
    color: 'red',
  },
  instructions: {
    textAlign: 'center',
    color: 'red',
    marginBottom: 5,
  },
});
