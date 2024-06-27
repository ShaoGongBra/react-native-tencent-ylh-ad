package com.reactnativetencentylhad

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.qq.e.ads.rewardvideo.RewardVideoAD
import com.qq.e.comm.managers.GDTAdSdk
import com.qq.e.comm.util.AdError
import com.reactnativetencentylhad.view.Hybrid
import com.reactnativetencentylhad.view.Interstitial


class TencentYlhAdModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var appId: String = "";
  private var context: ReactApplicationContext = reactContext

  override fun getName(): String {
    return "TencentYlhAd"
  }

  private fun sendEvent(eventName: String, params: WritableMap?) {
    context
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  /**
   * 初始化
   */
  @ReactMethod
  fun registerAppId(appId: String, promise: Promise) {
    this.appId = appId;
    GDTAdSdk.initWithoutStart(reactApplicationContext, appId);


    class OnStartListener: GDTAdSdk.OnStartListener {
      override fun onStartSuccess() {
        promise.resolve(null)
      }

      override fun onStartFailed(p0: java.lang.Exception?) {
        Log.e("gdt onStartFailed:", p0.toString());
        promise.reject("-1", p0.toString())
      }

    }

    GDTAdSdk.start(OnStartListener())
  }

  /**
   * 半屏广告
   */
  @ReactMethod
  fun showInterstitialAD(posID: String, asPopup: Boolean) {
    Interstitial.getInstance(reactApplicationContext).showInterstitialAD(posID, false, asPopup);
  }

  /**
   * 全屏广告
   */
  @ReactMethod
  fun showFullScreenAD(posID: String) {
    Interstitial.getInstance(reactApplicationContext).showInterstitialAD(posID, true, false);
  }

  /***
   * h5
   * @param url
   * @param settings ["titleBarHeight", "titleBarColor", "title", "titleColor", "titleSize", "backButtonImage", "closeButtonImage", "separatorColor", "backSeparatorLength"]
   * titleColor='#ff0000ff',titleBarHeight=45 dp, titleSize=20 sp, backButtonImage='gdt_ic_back'
   */
  @ReactMethod
  fun openWeb(url: String, settings: ReadableMap) {
    Hybrid.getInstance(reactApplicationContext).openWeb(url, settings)
  }

  private lateinit var rewardVideoAD: RewardVideoAD
  /**
   * 激励广告
   */
  @ReactMethod
  fun showRewardVideoAD(posId: String) {
    val listener = RewardVideoADListener()
    rewardVideoAD = RewardVideoAD(reactApplicationContext, posId, listener)
    listener.setConfig(rewardVideoAD, ::sendEvent)
    rewardVideoAD.loadAD()
  }

  class RewardVideoADListener: com.qq.e.ads.rewardvideo.RewardVideoADListener {

    private lateinit var rewardVideoAD: RewardVideoAD

    private lateinit var sendEvent: (eventName: String, params: WritableMap?) -> Unit

    fun setConfig (ad: RewardVideoAD, send: (eventName: String, params: WritableMap?) -> Unit) {
      rewardVideoAD = ad
      sendEvent = send
    }

    override fun onADLoad() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADLoad")
      })
      if(rewardVideoAD.isValid) {
        rewardVideoAD.showAD()
      } else {
        Log.d("ylh-ad","onADLoad - 无效的广告")
      }
    }

    override fun onVideoCached() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onVideoCached")
      })
      if(rewardVideoAD.isValid) {
        rewardVideoAD.showAD()
      } else {
        Log.d("ylh-ad","onVideoCached - 无效的广告")
      }
    }

    override fun onADShow() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADShow")
      })
    }

    override fun onADExpose() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADExpose")
      })
    }

    override fun onReward(p0: MutableMap<String, Any>?) {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onReward")
        putString("transId", p0!!["transId"].toString())
      })
    }

    override fun onADClick() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADClick")
      })
    }

    override fun onVideoComplete() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADClose")
      })
    }

    override fun onADClose() {
      sendEvent("rewardVideo", Arguments.createMap().apply {
        putString("type", "onADClose")
      })
    }

    override fun onError(p0: AdError?) {
      val map = Arguments.createMap()
      map.putString("type", "onError")
      if (p0 != null) {
        map.putInt("errorCode", p0.errorCode)
        map.putString("errorMsg", p0.errorMsg)
      }
      sendEvent("rewardVideo", map)
    }
  }
}
