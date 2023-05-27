package ge.denale.podcastlistener.data.repo

import android.content.SharedPreferences
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader
import ge.denale.podcastlistener.feature.home.IS_ADVERTISEMENT_ALLOWED
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject

class AdvertismentRepositoryImpl(
    private val nativeAdLoader: NativeBulkAdLoader,
    val sharedPreferences: SharedPreferences
) : AdvertisementRepository {

    private val advertisementSubject = SingleSubject.create<List<NativeAd>>()

    override fun getNativeAdvList(count: Int): Single<List<NativeAd>> {
        val parameters: HashMap<String, String> = hashMapOf(
            "preferable-height" to "120",
            "preferable-width" to "120",
        )
        loadAvertisement(parameters)
        return advertisementSubject
    }

    override fun getHorizontalNativeAdvList(count: Int): Single<List<NativeAd>> {
        val parameters: HashMap<String, String> = hashMapOf(
            "preferable-height" to "70",
            "preferable-width" to "match_parent"
        )
        loadAvertisement(parameters)
        return advertisementSubject
    }

    private fun loadAvertisement(parameters: HashMap<String, String>) {
        val isAdvertisementAllowed = sharedPreferences.getBoolean(IS_ADVERTISEMENT_ALLOWED, true)
        if (!isAdvertisementAllowed) {
            advertisementSubject.onSuccess(listOf())
            return
        }
        nativeAdLoader.setNativeBulkAdLoadListener(object : NativeBulkAdLoadListener {
            override fun onAdsLoaded(nativeAds: List<NativeAd>) {
                advertisementSubject.onSuccess(nativeAds)
            }

            override fun onAdsFailedToLoad(error: AdRequestError) {
                advertisementSubject.onSuccess(listOf())
            }
        })

        val nativeAdRequestConfiguration =
            NativeAdRequestConfiguration.Builder("demo-interstitial-yandex")
                .setParameters(parameters).build()
        nativeAdLoader.loadAds(nativeAdRequestConfiguration, 8)
    }
}