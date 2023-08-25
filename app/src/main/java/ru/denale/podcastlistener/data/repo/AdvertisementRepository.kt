package ru.denale.podcastlistener.data.repo

import com.yandex.mobile.ads.nativeads.NativeAd
import io.reactivex.Single

interface AdvertisementRepository {
    fun getNativeAdvList(count: Int, adId: String): Single<List<NativeAd>>

    fun getHorizontalNativeAdvList(count: Int, adId: String): Single<List<NativeAd>>
}