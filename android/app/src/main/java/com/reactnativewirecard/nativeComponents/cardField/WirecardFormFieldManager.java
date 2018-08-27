package com.reactnativewirecard.nativeComponents.cardField;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.reactnativewirecard.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.wirecard.paymentsdk.BuildConfig;
import de.wirecard.paymentsdk.WirecardCardFormFragment;
import de.wirecard.paymentsdk.WirecardClient;
import de.wirecard.paymentsdk.WirecardClientBuilder;
import de.wirecard.paymentsdk.WirecardEnvironment;
import de.wirecard.paymentsdk.WirecardException;
import de.wirecard.paymentsdk.WirecardInputFormsStateChangedListener;
import de.wirecard.paymentsdk.WirecardInputFormsStateManager;
import de.wirecard.paymentsdk.WirecardPaymentResponse;
import de.wirecard.paymentsdk.WirecardResponseError;
import de.wirecard.paymentsdk.WirecardResponseListener;
import de.wirecard.paymentsdk.WirecardTransactionType;
import de.wirecard.paymentsdk.api.models.json.helpers.TransactionState;
import de.wirecard.paymentsdk.models.CustomerData;
import de.wirecard.paymentsdk.models.WirecardExtendedCardPayment;


public class WirecardFormFieldManager
        extends SimpleViewManager<EmbeddedFormField> {

    private static final String ENCRYPTION_ALGORITHM = "HS256";
    private static final String UTF_8 = "UTF-8";

    private static final String REACT_CLASS = "WirecardFormField";
    private static final int COMMAND_CREATE = 1;
    private ThemedReactContext mContext;
    private WirecardInputFormsStateManager wirecardInputFormsStateManager;
    private TextView stateLabel;
    private WirecardClient wirecardClient;
    private WirecardExtendedCardPayment wirecardExtendedCardPayment;
    private WirecardCardFormFragment wirecardCardFormFragment;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected EmbeddedFormField createViewInstance(
            ThemedReactContext reactContext) {
        mContext = reactContext;
        return new EmbeddedFormField(reactContext);
    }

    @Override
    public void onDropViewInstance(EmbeddedFormField view) {
        super.onDropViewInstance(view);
        wirecardInputFormsStateManager.stopReceivingEvents();
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "create", COMMAND_CREATE
        );

    }

    private RCTDeviceEventEmitter emitter() {
        return mContext.getJSModule(RCTDeviceEventEmitter.class);
    }

    private void pushPayload(String event, WritableMap payload) {
        emitter().emit(event, payload);
    }

    @Override
    public void receiveCommand(EmbeddedFormField root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_CREATE:
                createFragment();
                break;
        }
    }

    private void createFragment() {
        String environment = WirecardEnvironment.TEST.getValue();
        try {
            wirecardClient = WirecardClientBuilder.newInstance(mContext.getCurrentActivity(), environment)
                    .setRequestTimeout(60)
                    .build();
        } catch (WirecardException exception) {
            Log.d(BuildConfig.APPLICATION_ID, "Device is rooted!");
        }

        wirecardExtendedCardPayment =
                new WirecardExtendedCardPayment(null, null, null, null,
                        null, new BigDecimal(0), null);
        wirecardExtendedCardPayment.setCardToken(null);
        wirecardCardFormFragment = new WirecardCardFormFragment.Builder(wirecardExtendedCardPayment)
                .setLocale("en")
                .build();

        ((FragmentActivity) mContext.getCurrentActivity())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, wirecardCardFormFragment)
                .commitNow();

        stateLabel = mContext.getCurrentActivity().findViewById(R.id.state);
        WirecardInputFormsStateChangedListener wirecardInputFormsStateChangedListener = new WirecardInputFormsStateChangedListener() {
            @Override
            public void onStateChanged(int code) {
                mapCardFieldStates(code);
            }
        };

        wirecardInputFormsStateManager =
                new WirecardInputFormsStateManager(mContext.getCurrentActivity(), wirecardInputFormsStateChangedListener);
        wirecardInputFormsStateManager.startReceivingEvents();

    }

    private void mapCardFieldStates(int code) {
        String state = "";
        switch (code) {
            case WirecardInputFormsStateChangedListener.CARD_NUMBER_FORM_FOCUS_GAINED:
                state = "CARD_NUMBER_FORM_FOCUS_GAINED";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_MONTH_FORM_FOCUS_GAINED:
                state = "EXPIRATION_MONTH_FORM_FOCUS_GAINED";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_YEAR_FORM_FOCUS_GAINED:
                state = "EXPIRATION_YEAR_FORM_FOCUS_GAINED";
                break;
            case WirecardInputFormsStateChangedListener.SECURITY_CODE_FORM_FOCUS_GAINED:
                state = "SECURITY_CODE_FORM_FOCUS_GAINED";
                break;
            case WirecardInputFormsStateChangedListener.CARD_NUMBER_FORM_FOCUS_LOST:
                state = "CARD_NUMBER_FORM_FOCUS_LOST";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_MONTH_FORM_FOCUS_LOST:
                state = "EXPIRATION_MONTH_FORM_FOCUS_LOST";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_YEAR_FORM_FOCUS_LOST:
                state = "EXPIRATION_YEAR_FORM_FOCUS_LOST";
                break;
            case WirecardInputFormsStateChangedListener.SECURITY_CODE_FORM_FOCUS_LOST:
                state = "SECURITY_CODE_FORM_FOCUS_LOST";
                break;
            case WirecardInputFormsStateChangedListener.CARD_BRAND_UNSUPPORTED:
                state = "CARD_TYPE_UNSUPPORTED";
                break;
            case WirecardInputFormsStateChangedListener.CARD_NUMBER_INVALID:
                state = "CARD_NUMBER_INVALID";
                break;
            case WirecardInputFormsStateChangedListener.CARD_NUMBER_INCOMPLETE:
                state = "CARD_NUMBER_INCOMPLETE";
                break;
            case WirecardInputFormsStateChangedListener.CARD_NUMBER_VALID:
                state = "CARD_NUMBER_VALID";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_MONTH_INCOMPLETE:
                state = "EXPIRATION_MONTH_INCOMPLETE";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_MONTH_VALID:
                state = "EXPIRATION_MONTH_VALID";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_YEAR_INCOMPLETE:
                state = "EXPIRATION_YEAR_INCOMPLETE";
                break;
            case WirecardInputFormsStateChangedListener.EXPIRATION_YEAR_VALID:
                state = "EXPIRATION_YEAR_VALID";
                break;
            case WirecardInputFormsStateChangedListener.SECURITY_CODE_INCOMPLETE:
                state = "SECURITY_CODE_INCOMPLETE";
                break;
            case WirecardInputFormsStateChangedListener.SECURITY_CODE_VALID:
                state = "SECURITY_CODE_VALID";
                break;
            case WirecardInputFormsStateChangedListener.CARD_VALID:
                state = "CARD_VALID";
                break;

        }
        WritableMap data = Arguments.createMap();
        data.putString("cardFieldStatus", state);
        pushPayload("nativeCardStatus", data);
        stateLabel.setText(state);
    }

    public void makeTransaction(final Promise promise) {
        // for testing purposes only, do not store your merchant account ID and secret key inside app
        String timestamp = generateTimestamp();
        String merchantID = "33f6d473-3036-4ca5-acb5-8c64dac862d1";
        String secretKey = "9e0130f6-2e1e-4185-b0d5-dc69079c75cc";
        String requestID = UUID.randomUUID().toString();
        WirecardTransactionType transactionType = WirecardTransactionType.PURCHASE;
        BigDecimal amount = new BigDecimal("10");
        String currency = "GBP";

        String signature = generateSignatureV2(timestamp, merchantID, requestID,
                transactionType.getValue(), amount, currency, secretKey);

        wirecardExtendedCardPayment.setRequestTimeStamp(timestamp);
        wirecardExtendedCardPayment.setMerchantAccountID(merchantID);
        wirecardExtendedCardPayment.setRequestID(requestID);
        wirecardExtendedCardPayment.setTransactionType(transactionType);
        wirecardExtendedCardPayment.setAmount(amount);
        wirecardExtendedCardPayment.setCurrency(currency);
        wirecardExtendedCardPayment.setSignature(signature);
        CustomerData accountHolder = new CustomerData();
        accountHolder.setLastName("Doe");
        wirecardExtendedCardPayment.setAccountHolder(accountHolder);

        //get WirecardExtendedCardPayment with appended card data from input fields
        wirecardExtendedCardPayment = wirecardCardFormFragment.getWirecardExtendedCardPayment();

        wirecardClient.makePayment(wirecardExtendedCardPayment, null, new WirecardResponseListener() {
            @Override
            public void onResponse(WirecardPaymentResponse wirecardPaymentResponse) {
                Log.d(de.wirecard.paymentsdk.BuildConfig.APPLICATION_ID, "response received");


                TransactionState transactionState = wirecardPaymentResponse.getTransactionState();
                if (transactionState.equals(TransactionState.SUCCESS)) {
                    WritableNativeMap cardToken = new WritableNativeMap();
                    cardToken.putString("id", wirecardPaymentResponse.getCardToken().getTokenId());
                    WritableNativeMap cardDetailsResponse = new WritableNativeMap();
                    cardDetailsResponse.putMap("cardToken", cardToken);
                    promise.resolve(cardDetailsResponse);
                } else {
                    String errorMessage = "Unable to make payment. Response code received: " + transactionState.getValue();
                    Log.d(BuildConfig.APPLICATION_ID, errorMessage);
                    promise.reject("CARD_PAYMENT_ERROR", errorMessage);
                }
            }

            @Override
            public void onError(WirecardResponseError wirecardResponseError) {
                Log.d(de.wirecard.paymentsdk.BuildConfig.APPLICATION_ID, wirecardResponseError.getErrorMessage());
                promise.reject(
                        "CARD_PAYMENT_ERROR",
                        "Unable to make payment. Reason" + wirecardResponseError.getErrorMessage());

            }
        });
    }

    private static String generateSignatureV2(String timestamp, String merchantID, String requestID,
                                              String transactionType, BigDecimal amount, String currency,
                                              String secretKey) {

        String payload = ENCRYPTION_ALGORITHM.toUpperCase() + "\n" +
                "request_time_stamp=" + timestamp + "\n" +
                "merchant_account_id=" + merchantID + "\n" +
                "request_id=" + requestID + "\n" +
                "transaction_type=" + transactionType + "\n" +
                "requested_amount=" + amount + "\n" +
                "requested_amount_currency=" + currency.toUpperCase();

        try {
            byte[] encryptedPayload = encryptSignatureV2(payload, secretKey);
            return new String(Base64.encode(payload.getBytes(UTF_8), Base64.NO_WRAP), UTF_8)
                    + "." + new String(Base64.encode(encryptedPayload, Base64.NO_WRAP), UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] encryptSignatureV2(String payload, String secretKey) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
            return mac.doFinal(payload.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[1];
    }

    private String generateTimestamp() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        return new StringBuilder(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
                .format(calendar.getTime()))
                .insert(22, ":")
                .toString();
    }


}
