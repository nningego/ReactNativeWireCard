package com.reactnativewirecard.nativeComponents.cardField;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.reactnativewirecard.R;

import java.math.BigDecimal;
import java.util.Map;

import de.wirecard.paymentsdk.BuildConfig;
import de.wirecard.paymentsdk.WirecardCardFormFragment;
import de.wirecard.paymentsdk.WirecardClient;
import de.wirecard.paymentsdk.WirecardClientBuilder;
import de.wirecard.paymentsdk.WirecardEnvironment;
import de.wirecard.paymentsdk.WirecardException;
import de.wirecard.paymentsdk.WirecardInputFormsStateChangedListener;
import de.wirecard.paymentsdk.WirecardInputFormsStateManager;
import de.wirecard.paymentsdk.models.WirecardExtendedCardPayment;


public class WirecardFormFieldManager
        extends SimpleViewManager<LinearLayout> {
    private static final String REACT_CLASS = "WirecardFormField";
    private static final int COMMAND_CREATE = 1;
    private ThemedReactContext mContext;
    private WirecardInputFormsStateManager wirecardInputFormsStateManager;
    private TextView stateLabel;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected LinearLayout createViewInstance(
            ThemedReactContext reactContext) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(reactContext)
                .inflate(R.layout.activity_main, null);
        mContext = reactContext;
        return root;
    }

    @Override
    public void onDropViewInstance(LinearLayout view) {
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

    @Override
    public void receiveCommand(LinearLayout root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_CREATE:
                createFragment();
                break;
        }
    }

    private void createFragment() {
        String environment = WirecardEnvironment.TEST.getValue();
        try {
            WirecardClientBuilder.newInstance(mContext.getCurrentActivity(), environment)
                    .setRequestTimeout(60)
                    .build();
        } catch (WirecardException exception) {
            Log.d(BuildConfig.APPLICATION_ID, "Device is rooted!");
        }

        WirecardExtendedCardPayment wirecardExtendedCardPayment =
                new WirecardExtendedCardPayment(null, null, null, null,
                        null, new BigDecimal(0), null);
        wirecardExtendedCardPayment.setCardToken(null);
        WirecardCardFormFragment wirecardCardFormFragment = new WirecardCardFormFragment.Builder(wirecardExtendedCardPayment)
                .setLocale("en")
                .build();

        ((FragmentActivity) mContext.getCurrentActivity())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, wirecardCardFormFragment)
                .commitNow();

        //TODO
        stateLabel = (TextView) mContext.getCurrentActivity().findViewById(R.id.state);
        WirecardInputFormsStateChangedListener wirecardInputFormsStateChangedListener = new WirecardInputFormsStateChangedListener() {
            @Override
            public void onStateChanged(int code) {
                updateStateLabel(code);
            }
        };

        wirecardInputFormsStateManager =
                new WirecardInputFormsStateManager(mContext.getCurrentActivity(), wirecardInputFormsStateChangedListener);
        wirecardInputFormsStateManager.startReceivingEvents();

    }

    private void updateStateLabel(int code) {
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
        stateLabel.setText(state);
    }

}
