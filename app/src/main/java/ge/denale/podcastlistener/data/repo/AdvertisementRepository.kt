package ge.denale.podcastlistener.data.repo

import com.yandex.mobile.ads.nativeads.NativeAd
import io.reactivex.Single

interface AdvertisementRepository {
    fun getNativeAdvList(count: Int): Single<List<NativeAd>>

    fun getHorizontalNativeAdvList(count: Int): Single<List<NativeAd>>
}