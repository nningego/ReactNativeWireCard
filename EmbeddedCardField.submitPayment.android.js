import { NativeModules } from 'react-native';

const { WirecardFormFieldModule } = NativeModules;

const submitPayment = () => WirecardFormFieldModule.submitPayment();

export default submitPayment;
