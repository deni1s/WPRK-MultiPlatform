package ru.denale.podcastlistener.feature.authors

import androidx.lifecycle.MutableLiveData
import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import ru.denale.podcastlistener.data.repo.AuthorRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.denale.podcastlistener.data.AuthorResponse

private const val LIMIT_SIZE = 20

class AuthorsViewModel(
    private val authorRepository: AuthorRepository,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val authorLiveData = PublishSubject.create<AuthorResponse>()
    val errorLiveData = PublishSubject.create<String>()
    val progressbarLiveData = MutableLiveData<Boolean>()
    private var rowsCount = 3

    init {
        progressbarLiveData.value = true
        loadAuthors(0)
    }

    fun setRowsCount(rows: Int) {
        this.rowsCount = rows
    }

    fun isAdvertisementAllowed() : Boolean {
        return advertisementRepository.isAdvertisementAllowed()
    }

    fun loanNextNewsPart(totalItemsCount: Int) {
        loadAuthors(totalItemsCount)
    }

    private fun loadAuthors(offset: Int) {
        getAuthorsSource(offset).subscribeOn(Schedulers.io())
            .subscribe(object : MusicPlayerSignleObserver<AuthorResponse>(compositeDisposable) {
                override fun onSuccess(t: AuthorResponse) {
                    authorLiveData.onNext(t)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    errorLiveData.onNext("Произошла ошибка")
                }
            })
    }

    private fun getAuthorsSource(offset: Int): Single<AuthorResponse> {
        return authorRepository.getAuthors(offset, LIMIT_SIZE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                progressbarLiveData.value = false
            }
    }
}