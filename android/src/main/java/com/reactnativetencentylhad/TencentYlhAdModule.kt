package com.reactnativetencentylhad

import android.util.Log
import com.facebook.react.bridge.*
import com.qq.e.ads.rewardvideo.RewardVideoAD
import com.qq.e.comm.managers.GDTAdSdk
import com.qq.e.comm.util.AdError
import com.reactnativetencentylhad.view.Hybrid
import com.reactnativetencentylhad.view.Interstitial


class TencentYlhAdModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var appId: String = "";

  override fun getName(): String {
    return "TencentYlhAd"
  }

  /**
   * 初始化
   */
  @ReactMethod
  fun registerAppId(appId: String, promise: Promise) {
    this.appId = appId;
    GDTAdSdk.initWithoutStart(reactApplicationContext, appId);
    GDTAdSdk.start(OnStartListener() {
       fun onStartSuccess() {
        // 推荐开发者在onStartSuccess回调后开始拉广告
        promise.resolve(null)
      }

      fun onStartFailed(e: Exception) {
        Log.e("gdt onStartFailed:", e.toString());
        promise.reject("-1", e.toString())
      }
    })

//    val result = gdt.getInstance().initWith(reactApplicationContext, appId)
//    if (result) {
//      promise.resolve(null)
//    } else{
//      promise.reject("-1", "")
//    }
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
  fun showRewardVideoAD(posId: String, promise: Promise) {
    val listener = RewardVideoADListener()
    rewardVideoAD = RewardVideoAD(reactApplicationContext, posId, listener)
    listener.setConfig(rewardVideoAD, promise)
    rewardVideoAD.loadAD()
  }

  class RewardVideoADListener: com.qq.e.ads.rewardvideo.RewardVideoADListener {

    private lateinit var rewardVideoAD: RewardVideoAD

    private lateinit var promise: Promise

    private var result: MutableMap<String, Any>? = null

    fun setConfig (ad: RewardVideoAD, p: Promise) {
      rewardVideoAD = ad
      promise = p
    }

    override fun onADLoad() {
      Log.d("ylh-ad", "广告加载")
      if(rewardVideoAD.isValid) {
        rewardVideoAD.showAD()
      } else {
        Log.d("ylh-ad","onADLoad - 无效的广告")
      }
    }

    override fun onVideoCached() {
      Log.d("ylh-ad", "广告缓存")
      if(rewardVideoAD.isValid) {
        rewardVideoAD.showAD()
      } else {
        Log.d("ylh-ad","onVideoCached - 无效的广告")
      }
    }

    override fun onADShow() {
      Log.d("ylh-ad", "广告显示")
    }

    override fun onADExpose() {
      Log.d("ylh-ad", "广告曝光")
    }

    override fun onReward(p0: MutableMap<String, Any>?) {
      Log.d("ylh-ad", "获得奖励")
      if (p0 != null) {
        result = p0
      }
    }

    override fun onADClick() {
      Log.d("ylh-ad", "广告点击")
    }

    override fun onVideoComplete() {
      Log.d("ylh-ad", "播放完成")
    }

    override fun onADClose() {
      Log.d("ylh-ad", "关闭广告")
      if(result != null) {
        promise.resolve(result.toString())
      } else {
        promise.reject("-2", "关闭广告")
      }

    }

    override fun onError(p0: AdError?) {
      promise.reject("-1", p0.toString())
    }
  }
}

class OnStartListener(function: () -> Unit) : GDTAdSdk.OnStartListener {
  override fun onStartSuccess() {

  }

  override fun onStartFailed(p0: java.lang.Exception?) {

  }

}
