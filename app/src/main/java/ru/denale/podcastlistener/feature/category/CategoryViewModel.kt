package ru.denale.podcastlistener.feature.category

import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.repo.CategoryRepository
import androidx.lifecycle.MutableLiveData
import io.appmetrica.analytics.AppMetrica
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.denale.podcastlistener.data.GenreResponse

private const val LIMIT_SIZE = 20

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val advertisementRepository: AdvertisementRepository
) : MusicPlayerOnlineViewModel() {

    val errorLiveData = PublishSubject.create<String>()


    private var rowsCount = 3

    fun setRowsCount(rows: Int) {
        this.rowsCount = rows
    }

    fun isAdvertisementAllowed() : Boolean {
        return advertisementRepository.isAdvertisementAllowed()
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
        getMainSource(offset).subscribe(object : MusicPlayerSignleObserver<GenreResponse>(compositeDisposable) {
            override fun onSuccess(t: GenreResponse) {
                categoryLiveData.onNext(t.list)
                AppMetrica.reportEvent("CategoryScreen", "success")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                errorLiveData.onNext("Произошла ошибка")
                AppMetrica.reportEvent("CategoryScreen", "error: ${e.message}")
            }
        })
    }

    private fun getMainSource(offset: Int): Single<GenreResponse> {
        return categoryRepository.getCategories(offset, LIMIT_SIZE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                progressbarLiveData.value = false
            }
    }
}