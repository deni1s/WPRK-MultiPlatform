package ru.denale.podcastlistener.feature.activities.musics

import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.repo.MusicRepository
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.yandex.metrica.YandexMetrica
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE

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
    var type: String? = null

    init {
        progressLiveData.value = true

        type = bundle?.getString(EXTRA_MUSIC_TYPE)
        authorId = bundle?.getString(EXTRA_AUTHOR_ID_KEY_DATA)
        categoryId = bundle?.getString(EXTRA_GENRE_ID_KEY_DATA)
        if (authorId != null) {
            loadMusicByAuthor(authorId!!, 0)
        } else if (categoryId != null) {
            loadMusicByGenre(categoryId, 0)
        } else {
            getTypeData(type.orEmpty())
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

                    YandexMetrica.reportEvent(
                        "AuthorListPodcasts",
                        mapOf(
                            "result" to "success",
                            "authorId" to authorId,
                        )
                    )
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                    YandexMetrica.reportEvent(
                        "AuthorListPodcasts",
                        mapOf(
                            "result" to "error: ${e.message}",
                            "authorId" to authorId,
                        )
                    )
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
                    YandexMetrica.reportEvent(
                        "CategoryListPodcasts", mapOf(
                            "result" to "success",
                            "categoryId" to categoryId
                        )
                    )
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                    YandexMetrica.reportEvent(
                        "CategoryListPodcasts",
                        mapOf(
                            "result" to "error: ${e.message}",
                            "categoryId" to categoryId
                        )
                    )
                }
            })
    }

    fun getTypeData(id: String) {
        musicRepository.getPreviousMusics(id).flatMap {
            Single.just(
                WaveResponse(
                    podcasts = it.list,
                    title = it.title,
                    type = it.type,
                    warning = null
                )
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<WaveResponse>(compositeDisposable) {
                override fun onSuccess(t: WaveResponse) {
                    musicLiveData.onNext(t.podcasts)
                    t.warning?.let { warningLiveData.onNext(it) }
                    YandexMetrica.reportEvent("WaveListPodcasts", mapOf("result" to "success", "type" to t.type) )
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                    YandexMetrica.reportEvent("WaveListPodcasts", mapOf("result" to "error: ${e.message}", "type" to type) )
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