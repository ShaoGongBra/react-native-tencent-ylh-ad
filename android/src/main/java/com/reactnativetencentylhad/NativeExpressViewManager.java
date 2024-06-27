package com.reactnativetencentylhad;

import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactnativetencentylhad.view.NativeExpress;

import java.util.List;
import java.util.Map;

public class NativeExpressViewManager extends SimpleViewManager {
  private static final String TAG = "NativeExpressView";

  @NonNull
  @Override
  public String getName() {
    return TAG;
  }

  public enum Events {
    EVENT_FAIL_TO_RECEIVED("onFailToReceived"),
    EVENT_RECEIVED("onReceived"),
    EVENT_WILL_LEAVE_APP("onViewWillLeaveApplication"),
    EVENT_WILL_CLOSE("onViewWillClose"),
    EVENT_WILL_EXPOSURE("onViewWillExposure"),
    EVENT_ON_RENDER("onRender"),
    EVENT_ON_CLICK("onClicked"),
    EVENT_WILL_OPEN_FULL_SCREEN("onViewWillPresentFullScreenModal"),
    EVENT_DID_OPEN_FULL_SCREEN("onViewDidPresentFullScreenModal"),
    EVENT_WILL_CLOSE_FULL_SCREEN("onViewWillDismissFullScreenModal"),
    EVENT_DID_CLOSE_FULL_SCREEN("onViewDidDismissFullScreenModal");

    private final String mName;

    Events(final String name) {
      mName = name;
    }

    @Override
    public String toString() {
      return mName;
    }
  }

  private FrameLayout mContainer;
  private RCTEventEmitter mEventEmitter;
  private ThemedReactContext mThemedReactContext;
  private NativeExpress mNativeExpress;

  @Override
  protected View createViewInstance(ThemedReactContext reactContext) {
    mThemedReactContext = reactContext;
    mEventEmitter = reactContext.getJSModule(RCTEventEmitter.class);
    FrameLayout viewGroup = new FrameLayout(reactContext);
    mContainer = viewGroup;
    return viewGroup;
  }

  @Override
  public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
    for (Events event : Events.values()) {
      builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
    }
    return builder.build();
  }

  @ReactProp(name = "posId")
  public void setPosId(FrameLayout view, final String posId) {
    NativeExpress nativeExpress = new NativeExpress(mThemedReactContext, posId, mEventEmitter, mContainer);
    mNativeExpress = nativeExpress;
    view.addView(mNativeExpress);
  }
}
