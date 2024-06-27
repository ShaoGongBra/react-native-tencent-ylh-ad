package com.reactnativetencentylhad.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.gson.Gson;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;
import com.reactnativetencentylhad.NativeExpressViewManager;
import com.reactnativetencentylhad.R;

import java.util.List;

/**
 * 流广告
 */
public class NativeExpress extends FrameLayout implements NativeExpressMediaListener, NativeExpressAD.NativeExpressADListener {
  private static final String TAG = "NativeExpress";
  private NativeExpressAD mNativeExpressAD;
  private RCTEventEmitter mEventEmitter;
  private Runnable mLayoutRunnable;
  private NativeExpressADView mNativeExpressADView;
  private ThemedReactContext mThemedReactContext;
  private FrameLayout mContainer;

  public NativeExpress(Context context, String posID, RCTEventEmitter mEventEmitter, FrameLayout mContainer) {
    super(context);
    mThemedReactContext = (ThemedReactContext) context;
    this.mEventEmitter = mEventEmitter;
    this.mContainer = mContainer;
    // 把布局加载到这个View里面
    inflate(context, R.layout.layout_native_express,this);
    initView(posID);
  }

  /**
   * 初始化View
   */
  private void initView(String posID) {
    closeNativeExpress();
    mNativeExpressAD = new NativeExpressAD(this.getContext(), new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT), posID, this);
    // 如果您在平台上新建平台模板2.0广告位时，选择了支持视频，那么可以进行个性化设置（可选）
//    VideoOption2.Builder builder = new VideoOption2.Builder();

    /**
     * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前设置setAutoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
     * 如果广告位仅支持图文广告，则无需调用
     */

    mNativeExpressAD.setVideoOption(new VideoOption.Builder().setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS) // WIFI 环境下可以自动播放视频
      .setAutoPlayMuted(false) // 自动播放时为静音
      .setMaxVideoDuration(0) // 设置返回视频广告的最大视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制，合法输入为：5<=maxVideoDuration<=61. 此设置会影响广告填充，请谨慎设置
      .setMinVideoDuration(0) // 设置返回视频广告的最小视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制， 此设置会影响广告填充，请谨慎设置
      .build()
    );
    mNativeExpressAD.loadAD(1);
  }

  public void closeNativeExpress() {
    removeAllViews();
    if (mNativeExpressAD != null) {
      mNativeExpressAD = null;
      Log.e(TAG,"关闭广告");
    }
    if (mLayoutRunnable != null){
      removeCallbacks(mLayoutRunnable);
    }
  }

  public NativeExpressAD getNativeExpressAD2() {
    return mNativeExpressAD;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  public void requestLayout() {
    super.requestLayout();
    if (mLayoutRunnable != null){
      removeCallbacks(mLayoutRunnable);
    }
    mLayoutRunnable = new Runnable() {
      @Override
      public void run() {
        measure(
          MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        layout(getLeft(), getTop(), getRight(), getBottom());
      }
    };
    post(mLayoutRunnable);
  }

  // 广告回调
  @Override
  public void onNoAD(AdError adError) {
    Log.e(TAG,"onNoAD: eCode=" + adError.getErrorCode() + ",eMsg=" + adError.getErrorMsg());
    WritableMap event = Arguments.createMap();
    event.putString("error", new Gson().toJson(adError));
    mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_FAIL_TO_RECEIVED.toString(), event);
  }

  @Override
  public void onADLoaded(List<NativeExpressADView> list) {
    Log.e(TAG,"onADReceive");
    mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_RECEIVED.toString(), null);
    if (mNativeExpressADView != null) {
      return;
    }
    if (!list.isEmpty()) {
      NativeExpressADView mNativeExpressADView = list.get(0);
      this.mNativeExpressADView = mNativeExpressADView;
      if (mNativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
        mNativeExpressADView.setMediaListener(this);
      }
      mNativeExpressADView.render();
    }
  }

  @Override
  public void onRenderFail(NativeExpressADView nativeExpressADView) {
    Log.e(TAG,"onRenderFail");
  }

  @Override
  public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
    Log.e(TAG,"onRenderSuccess");
    if (mNativeExpressADView != null) {
      removeAllViews();
      addView(mNativeExpressADView);
      mNativeExpressADView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
          WritableMap arguments = Arguments.createMap();
          arguments.putInt("height", view.getHeight());
          arguments.putInt("width", view.getWidth());
          mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_ON_RENDER.toString(), arguments);
        }
      });
    }
  }

  @Override
  public void onADExposure(NativeExpressADView nativeExpressADView) {
    mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_WILL_EXPOSURE.toString(), null);
  }

  @Override
  public void onADClicked(NativeExpressADView nativeExpressADView) {
    mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_ON_CLICK.toString(), null);
  }

  @Override
  public void onADClosed(NativeExpressADView nativeExpressADView) {
    Log.e(TAG,"onAdClosed");
    mEventEmitter.receiveEvent(mContainer.getId(), NativeExpressViewManager.Events.EVENT_WILL_CLOSE.toString(), null);
  }

  @Override
  public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
    Log.e(TAG,"onADLeftApplication");
  }

  // 视频回调
  @Override
  public void onVideoInit(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoLoading(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoCached(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoReady(NativeExpressADView nativeExpressADView, long l) {

  }

  @Override
  public void onVideoStart(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoPause(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoComplete(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoError(NativeExpressADView nativeExpressADView, AdError adError) {

  }

  @Override
  public void onVideoPageOpen(NativeExpressADView nativeExpressADView) {

  }

  @Override
  public void onVideoPageClose(NativeExpressADView nativeExpressADView) {

  }
}
