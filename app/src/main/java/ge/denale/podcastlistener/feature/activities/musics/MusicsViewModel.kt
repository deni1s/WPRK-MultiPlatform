package ge.denale.podcastlistener.feature.activities.musics

import ge.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ge.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ge.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ge.denale.podcastlistener.common.MusicPlayerSignleObserver
import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.data.repo.MusicRepository
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.yandex.mobile.ads.nativeads.NativeAd
import ge.denale.podcastlistener.data.AdvertisementMixer
import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class MusicsViewModel(
    bundle: Bundle?,
    private val musicRepository: MusicRepository,
    private val adMixer: AdvertisementMixer,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val musicLiveData = PublishSubject.create<List<Any>>()
    val progressLiveData = MutableLiveData<Boolean>()
    private var authorId: String? = null
    private var categoryId: String? = null

    init {
        progressLiveData.value = true

        authorId = bundle?.getString(EXTRA_AUTHOR_ID_KEY_DATA)
        categoryId = bundle?.getString(EXTRA_GENRE_ID_KEY_DATA)
        if (authorId != null) {
            loadMusicByAuthor(authorId!!, 0)
        } else {
            loadMusicByGenre(categoryId, 0)
        }
    }

    private fun loadMusicByAuthor(authorId: String, offset: Int) {
        Single.zip(
            getAuthorsSource(authorId, offset),
            getAdvertisementSource(offset)
        ) { authors: List<Music>, advertisment: List<NativeAd> ->
            adMixer.simpleMix(authors, advertisment, 6)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
                override fun onSuccess(t: List<Any>) {
                    musicLiveData.onNext(t)
                }
            })
    }

    private fun getAuthorsSource(authorId: String, offset: Int): Single<List<Music>> {
        return musicRepository.getMusicsByAuthor(authorId, offset)
    }

    private fun loadMusicByGenre(genreId: String?, offset: Int) {
        Single.zip(
            getGenreSource(genreId, offset),
            getAdvertisementSource(offset)
        ) { authors: List<Music>, advertisment: List<NativeAd> ->
            adMixer.simpleMix(authors, advertisment, 3)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<List<Any>>(compositeDisposable) {
                override fun onSuccess(t: List<Any>) {
                    musicLiveData.onNext(t)
                }
            })
    }


    private fun getGenreSource(genreId: String?, offset: Int): Single<List<Music>> {
        return musicRepository.getMusics(genreId, offset)
    }

    private fun getAdvertisementSource(offset: Int): Single<List<NativeAd>> {
        return if (offset == 0) {
            advertisementRepository.getHorizontalNativeAdvList(6)
                .timeout(3000, TimeUnit.MILLISECONDS)
                .onErrorReturnItem(emptyList())
        } else {
            Single.just(emptyList())
        }
    }

    fun loanNextNewsPart(totalItemsCount: Int) {
        if (authorId != null) {
            loadMusicByAuthor(authorId!!, totalItemsCount)
        } else {
            loadMusicByGenre(categoryId, totalItemsCount)
        }
    }
}