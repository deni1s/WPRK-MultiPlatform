package ru.denale.podcastlistener.feature.home

import android.content.SharedPreferences
import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import androidx.lifecycle.MutableLiveData
import com.yandex.mobile.ads.nativeads.NativeAd
import ru.denale.podcastlistener.data.*
import ru.denale.podcastlistener.data.repo.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.denale.podcastlistener.BuildConfig
import java.util.concurrent.TimeUnit

private const val TOP_BANNER_TYPE = "top"
private const val BOTTOM_BANNER_TYPE = "bottom"

class HomeViewModel(
    bannerRepository: BannerRepository,
    categoryRepository: CategoryRepository,
    authorRepository: AuthorRepository,
    userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences,
    private val advertisementRepository: AdvertisementRepository,
    private val adMixer: AdvertisementMixer
) : MusicPlayerOnlineViewModel() {

    val topBannerLiveData = MutableLiveData<List<Banner>>()
    val bottomBannerLiveData = MutableLiveData<List<Banner>>()
    val progressLiveData = MutableLiveData<Boolean>()
    val categoryLiveData = MutableLiveData<List<Any>>()
    val authorsLiveData = MutableLiveData<List<Any>>()

    init {
        userRepository.getUserInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<User>(compositeDisposable) {
                override fun onSuccess(t: User) {
                    sharedPreferences.edit().putBoolean(IS_ADVERTISEMENT_ALLOWED, !t.showAdverisement).apply()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        bannerRepository.getBanners(TOP_BANNER_TYPE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Banner>>(compositeDisposable) {
                override fun onSuccess(t: List<Banner>) {
                    topBannerLiveData.value = t
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        bannerRepository.getBanners(BOTTOM_BANNER_TYPE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Banner>>(compositeDisposable) {
                override fun onSuccess(t: List<Banner>) {
                    bottomBannerLiveData.value = t
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        Single.zip(
            categoryRepository.getCategories()
                .subscribeOn(Schedulers.io()),
            getAdvertisementSource(BuildConfig.MAIN_CATEGORY_AD_UNIT_ID)
        ) { authors: List<Genre>, advertisment: List<NativeAd> ->
            adMixer.simpleMix(authors, advertisment, 2)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
                override fun onSuccess(t: List<Any>) {
                    categoryLiveData.value = t
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        Single.zip(
            authorRepository.getAuthors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { progressLiveData.value = false },
            getAdvertisementSource(BuildConfig.MAIN_AUTHOR_AD_UNIT_ID)
        ) { authors: List<Author>, advertisment: List<NativeAd> ->
            adMixer.simpleMix(authors, advertisment, 2)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
                override fun onSuccess(t: List<Any>) {
                    authorsLiveData.value = t
                }
            })
//
//        categoryRepository.getCategories()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doFinally {
//                progressLiveData.value = false
//            }
//            .subscribe(object : MusicPlayerSignleObserver<List<Genre>>(compositeDisposable) {
//                override fun onSuccess(t: List<Genre>) {
//                    categoryLiveData.value = t
//                }
//            })
//
//        authorRepository.getAuthors()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doFinally { progressLiveData.value = false }
//            .subscribe(object : MusicPlayerSignleObserver<List<Author>>(compositeDisposable) {
//                override fun onSuccess(t: List<Author>) {
//                    authorsLiveData.value = t
//                }
//            })
    }

    private fun getAdvertisementSource(adId: String): Single<List<NativeAd>> {
        return advertisementRepository.getNativeAdvList(1, adId)
              .timeout(3000, TimeUnit.MILLISECONDS)
            .onErrorReturnItem(emptyList())
    }
}