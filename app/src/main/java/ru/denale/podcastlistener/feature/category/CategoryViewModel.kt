package ru.denale.podcastlistener.feature.category

import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.data.repo.CategoryRepository
import androidx.lifecycle.MutableLiveData
import com.yandex.mobile.ads.nativeads.NativeAd
import ru.denale.podcastlistener.data.AdvertisementMixer
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.denale.podcastlistener.BuildConfig

private const val LIMIT_SIZE = 20

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val adMixer: AdvertisementMixer,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val errorLiveData = PublishSubject.create<String>()


    private var rowsCount = 3

    fun setRowsCount(rows: Int) {
        this.rowsCount = rows
    }

    fun loanNextNewsPart(totalItemsCount: Int) {
        loadCategories(totalItemsCount)
    }

    val categoryLiveData = PublishSubject.create<List<Any>>()
    val progressbarLiveData = MutableLiveData<Boolean>()

    init {
        progressbarLiveData.value = true
        loadCategories(0)
    }

    private fun loadCategories(offset: Int) {
        Single.zip(
            getMainSource(offset),
            getAdvertisementSource()
        ) { authors: List<Genre>, advertisment: List<NativeAd> ->
            adMixer.mixAdvertisement(authors, advertisment, rowsCount)
        }.subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
            override fun onSuccess(t: List<Any>) {
                categoryLiveData.onNext(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                errorLiveData.onNext("Произошла ошибка")
            }
        })
    }

    private fun getMainSource(offset: Int): Single<List<Genre>> {
        return categoryRepository.getCategories(offset, LIMIT_SIZE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                progressbarLiveData.value = false
            }
    }

    private fun getAdvertisementSource(): Single<List<NativeAd>> {
        return advertisementRepository.getNativeAdvList(8, BuildConfig.CATEGORY_AD_UNIT_ID)
            //  .timeout(3000, TimeUnit.MILLISECONDS)
            .onErrorReturnItem(emptyList())
    }
}