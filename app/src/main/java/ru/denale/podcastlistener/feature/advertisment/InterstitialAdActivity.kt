package ru.denale.podcastlistener.feature.advertisment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.R
import kotlinx.android.synthetic.main.activity_interstitial_ad.*

class InterstitialAdActivity : AppCompatActivity(R.layout.activity_interstitial_ad) {

    private val eventLogger = InterstitialAdEventLogger()
    private val singleAdLogger = InterstitialSingleAdEventListener()

    private var adUnitId = BuildConfig.FIRST_AD_UNIT_ID
    private var interstitialAd: InterstitialAdLoader? = null
    private var isBackPressedAllowed = false
    private var showingAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterstitial()
    }

    private fun loadInterstitial() {
        createInterstitial()
        progress.isVisible = true
        interstitialAd?.loadAd(AdRequestConfiguration.Builder(adUnitId).build())
    }

    private fun closeScreen() {
        showingAd?.setAdEventListener(null)
        interstitialAd?.setAdLoadListener(null)
        progress.isVisible = false
        setResult(RESULT_OK)
        finish()
    }

    private fun createInterstitial() {
        interstitialAd = InterstitialAdLoader(this).apply {
            setAdLoadListener(eventLogger)
        }
    }

    private fun destroyInterstitial() {
        interstitialAd?.cancelLoading()
        interstitialAd = null
    }

    override fun onDestroy() {
        destroyInterstitial()
        super.onDestroy()
    }

    private inner class InterstitialAdEventLogger : InterstitialAdLoadListener {

        override fun onAdLoaded(p0: InterstitialAd) {
            showingAd = p0
            showingAd?.setAdEventListener(singleAdLogger)
            showingAd?.show(this@InterstitialAdActivity)
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            progress.isVisible = false
            closeScreen()
        }
    }

    private inner class InterstitialSingleAdEventListener : InterstitialAdEventListener {

        override fun onAdShown() {
            progress.isVisible = false
        }

        override fun onAdFailedToShow(p0: AdError) {
            closeScreen()
        }

        override fun onAdDismissed() {
            closeScreen()
        }

        override fun onAdClicked() {

        }

        override fun onAdImpression(p0: ImpressionData?) {
        }

    }

    override fun onBackPressed() {
        if (isBackPressedAllowed) {
            super.onBackPressed()
        }
    }
}
