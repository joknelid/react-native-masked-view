package com.rnmaskedview;

import javax.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.common.annotations.VisibleForTesting;

public class RNMaskedViewManager extends ViewGroupManager<RNMaskedView> {

    @VisibleForTesting
    public static final String REACT_CLASS = "RNMaskedView";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RNMaskedView createViewInstance(ThemedReactContext context) {
        RNMaskedView view = new RNMaskedView(context);
        return view;
    }

    @ReactProp(name = "maskImage")
    public void setMaskImage(RNMaskedView view, @Nullable ReadableMap source) {
        view.loadMask(source);
    }

}
