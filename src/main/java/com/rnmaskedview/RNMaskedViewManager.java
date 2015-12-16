package com.rnmaskedview;

import javax.annotation.Nullable;

import android.webkit.WebChromeClient;
import android.webkit.CookieManager;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ReactProp;
import com.facebook.react.common.annotations.VisibleForTesting;

import java.util.Map;

public class RNMaskedViewManager extends ViewGroupManager<RNWebView> {

    @VisibleForTesting
    public static final String REACT_CLASS = "RNMaskedViewAndroid";

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
    public void setUrl(RNWebView view, @Nullable String image) {
        view.loadMask(image);
    }

}
