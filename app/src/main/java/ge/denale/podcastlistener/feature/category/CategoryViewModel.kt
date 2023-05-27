package ge.denale.podcastlistener.feature.category

import ge.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ge.denale.podcastlistener.common.MusicPlayerSignleObserver
import ge.denale.podcastlistener.data.Genre
import ge.denale.podcastlistener.data.repo.CategoryRepository
import androidx.lifecycle.MutableLiveData
import com.yandex.mobile.ads.nativeads.NativeAd
import ge.denale.podcastlistener.data.AdvertisementMixer
import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

private const val LIMIT_SIZE = 20

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val adMixer: AdvertisementMixer,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

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
        return advertisementRepository.getNativeAdvList(8)
            //  .timeout(3000, TimeUnit.MILLISECONDS)
            .onErrorReturnItem(emptyList())
    }
}