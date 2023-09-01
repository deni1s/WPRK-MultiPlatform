package ru.denale.podcastlistener.feature.home

import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import androidx.lifecycle.MutableLiveData
import ru.denale.podcastlistener.data.*
import ru.denale.podcastlistener.data.repo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private const val TOP_BANNER_TYPE = "top"
private const val BOTTOM_BANNER_TYPE = "bottom"

class HomeViewModel(
    bannerRepository: BannerRepository,
    categoryRepository: CategoryRepository,
    authorRepository: AuthorRepository,
    userRepository: UserRepository,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {
    fun isAdvertisementAllowed(): Boolean {
        return advertisementRepository.isAdvertisementAllowed()
    }

    val topBannerLiveData = MutableLiveData<List<Banner>>()
    val bottomBannerLiveData = MutableLiveData<List<Banner>>()
    val progressLiveData = MutableLiveData<Boolean>()
    val categoryLiveData = MutableLiveData<List<Any>>()
    val authorsLiveData = MutableLiveData<AuthorResponse>()

    init {
        advertisementRepository.increaseEnterance()
        userRepository.getUserInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<User>(compositeDisposable) {
                override fun onSuccess(t: User) {
                    advertisementRepository.setAdvertisementAllowed(!t.showAdverisement)
                }
            })

        bannerRepository.getBanners(TOP_BANNER_TYPE)
            .map { it.list }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Banner>>(compositeDisposable) {
                override fun onSuccess(t: List<Banner>) {
                    topBannerLiveData.value = t
                }
            })

        bannerRepository.getBanners(BOTTOM_BANNER_TYPE)
            .map { it.list }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Banner>>(compositeDisposable) {
                override fun onSuccess(t: List<Banner>) {
                    bottomBannerLiveData.value = t
                }
            })

        categoryRepository.getCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<GenreResponse>(compositeDisposable) {
                override fun onSuccess(t: GenreResponse) {
                    categoryLiveData.value = t.list
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        authorRepository.getAuthors()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { progressLiveData.value = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<AuthorResponse>(compositeDisposable) {
                override fun onSuccess(t: AuthorResponse) {
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
}