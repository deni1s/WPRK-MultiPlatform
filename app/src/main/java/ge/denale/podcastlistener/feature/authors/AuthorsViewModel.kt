package ge.denale.podcastlistener.feature.authors

import androidx.lifecycle.MutableLiveData
import com.yandex.mobile.ads.nativeads.NativeAd
import ge.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ge.denale.podcastlistener.common.MusicPlayerSignleObserver
import ge.denale.podcastlistener.data.AdvertisementMixer
import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.data.repo.AdvertisementRepository
import ge.denale.podcastlistener.data.repo.AuthorRepository
import ge.denale.podcastlistener.feature.adapter.AuthorsDiffUtlCallback
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

private const val LIMIT_SIZE = 20

class AuthorsViewModel(
    private val authorRepository: AuthorRepository,
    private val adMixer: AdvertisementMixer,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val authorLiveData = PublishSubject.create<List<Any>>()
    val progressbarLiveData = MutableLiveData<Boolean>()
    private var rowsCount = 3

    init {
        progressbarLiveData.value = true
        loadAuthors(0)
    }

    fun setRowsCount(rows: Int) {
        this.rowsCount = rows
    }

    fun loanNextNewsPart(totalItemsCount: Int) {
        loadAuthors(totalItemsCount)
    }

    private fun loadAuthors(offset: Int) {
        Single.zip(
            getAuthorsSource(offset),
            getAdvertisementSource(offset)
        ) { authors: List<Author>, advertisment: List<NativeAd> ->
            adMixer.mixAdvertisement(authors, advertisment, rowsCount)
        }.subscribeOn(Schedulers.io())
            .subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
                override fun onSuccess(t: List<Any>) {
                    authorLiveData.onNext(t)
                }
            })
    }

    private fun getAuthorsSource(offset: Int): Single<List<Author>> {
        return authorRepository.getAuthors(offset, LIMIT_SIZE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                progressbarLiveData.value = false
            }
    }

    private fun getAdvertisementSource(offset: Int): Single<List<NativeAd>> {
        return if (offset == 0) {
            advertisementRepository.getNativeAdvList(8)
                .timeout(3000, TimeUnit.MILLISECONDS)
                .onErrorReturnItem(emptyList())
        } else {
            Single.just(emptyList())
        }
    }
}