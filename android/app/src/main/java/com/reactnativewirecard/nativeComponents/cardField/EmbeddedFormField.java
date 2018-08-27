package com.reactnativewirecard.nativeComponents.cardField;

import android.content.Context;
import android.view.Choreographer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.reactnativewirecard.R;

public class EmbeddedFormField extends LinearLayout {

    private final Context context;

    public EmbeddedFormField(Context context) {
        super(context);
        this.context = context;
        inflate(getContext(), R.layout.activity_main, this);
        handleDynamicRelayout();
    }

    // https://github.com/facebook/react-native/issues/17968
    private void handleDynamicRelayout() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
                    child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
                }
                getViewTreeObserver().dispatchOnGlobalLayout();
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

}
