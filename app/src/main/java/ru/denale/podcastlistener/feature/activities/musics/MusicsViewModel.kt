package ru.denale.podcastlistener.feature.activities.musics

import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.repo.MusicRepository
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.denale.podcastlistener.data.AuthorResponse

class MusicsViewModel(
    bundle: Bundle?,
    private val musicRepository: MusicRepository,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val musicLiveData = PublishSubject.create<List<Any>>()
    val warningLiveData = PublishSubject.create<String>()
    val errorLiveData = PublishSubject.create<String>()
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
        getAuthorsSource(authorId, offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<WaveResponse>(compositeDisposable) {
                override fun onSuccess(t: WaveResponse) {
                    musicLiveData.onNext(t.podcasts)
                    t.warning?.let { warningLiveData.onNext(it) }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                }
            })
    }

    private fun getAuthorsSource(authorId: String, offset: Int): Single<WaveResponse> {
        return musicRepository.getMusicsByAuthor(authorId, offset)
    }

    private fun loadMusicByGenre(genreId: String?, offset: Int) {
        getGenreSource(genreId, offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MusicPlayerSignleObserver<WaveResponse>(compositeDisposable) {
                override fun onSuccess(t: WaveResponse) {
                    musicLiveData.onNext(t.podcasts)
                    t.warning?.let { warningLiveData.onNext(it) }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                }
            })
    }

    private fun getGenreSource(genreId: String?, offset: Int): Single<WaveResponse> {
        return musicRepository.getMusics(genreId, offset)
    }

    fun loanNextNewsPart(totalItemsCount: Int) {
        if (authorId != null) {
            loadMusicByAuthor(authorId!!, totalItemsCount)
        } else {
            loadMusicByGenre(categoryId, totalItemsCount)
        }
    }

    fun isAdvertisementAllowed(): Boolean {
        return advertisementRepository.isAdvertisementAllowed()
    }
}