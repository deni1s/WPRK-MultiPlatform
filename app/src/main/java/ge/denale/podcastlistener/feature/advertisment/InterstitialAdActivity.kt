package ge.denale.podcastlistener.feature.advertisment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import ge.denale.podcastlistener.BuildConfig
import ge.denale.podcastlistener.R
import kotlinx.android.synthetic.main.activity_interstitial_ad.*

class InterstitialAdActivity : AppCompatActivity(R.layout.activity_interstitial_ad) {

    private val eventLogger = InterstitialAdEventLogger()

    private var adUnitId = "R-M-2120824-2"
    private var interstitialAd: InterstitialAd? = null
    private var isBackPressedAllowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        close_button.setOnClickListener {
            closeScreen()
        }
        loadInterstitial()
    }

    private fun loadInterstitial() {
        destroyInterstitial()
        createInterstitial()
        interstitialAd?.loadAd(AdRequest.Builder().build())
    }

    private fun closeScreen() {
        setResult(RESULT_OK)
        finish()
    }

    private fun createInterstitial() {
        interstitialAd = InterstitialAd(this).apply {
            setAdUnitId(adUnitId)
            setInterstitialAdEventListener(eventLogger)
        }
    }

    private fun destroyInterstitial() {
        interstitialAd?.destroy()
        interstitialAd = null
    }

    override fun onDestroy() {
        destroyInterstitial()
        super.onDestroy()
    }

    private inner class InterstitialAdEventLogger : InterstitialAdEventListener {

        override fun onAdLoaded() {
            progress.isVisible = false
            interstitialAd?.show()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            progress.isVisible = false
            isBackPressedAllowed = true
            closeScreen()
        }

        override fun onAdShown() {
            progress.isVisible = false
        }

        override fun onAdDismissed() {
            progress.isVisible = false
            closeScreen()
        }

        override fun onAdClicked() {

        }

        override fun onLeftApplication() {
            progress.isVisible = false
        }

        override fun onReturnedToApplication() {
            progress.isVisible = false
        }

        override fun onImpression(data: ImpressionData?) {
            progress.isVisible = false
            close_button.isVisible = true
            isBackPressedAllowed = true
        }
    }

    override fun onBackPressed() {
        if (isBackPressedAllowed) {
            super.onBackPressed()
        }
    }
}
