package ge.denale.podcastlistener.feature.activities.playmusic

import android.content.SharedPreferences
import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.data.repo.MusicRepository
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ge.denale.podcastlistener.common.*
import ge.denale.podcastlistener.feature.home.ENTERANCE_COUNT
import ge.denale.podcastlistener.feature.home.FIRST_ADD_ENTERANCE_COUNT
import ge.denale.podcastlistener.feature.home.IS_ADVERTISEMENT_ALLOWED
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PlayMusicViewModel(
    bundle: Bundle,
    private val musicRepository: MusicRepository,
    sharedPreferences: SharedPreferences
) :
    MusicPlayerOnlineViewModel() {

    fun setTrackListened(genreId: String, authorId: String) {
        compositeDisposable.add(
            musicRepository.setTrackListened(genreId, authorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({
                    Log.d("listenre", "setTrackListened: success")
                }, {
                    Log.d("listenre", "setTrackListened: failure")
                })
        )
    }

    fun markTrackSeen(podcastId: String) {
        compositeDisposable.add(
            musicRepository.markTrackSeen(podcastId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({}, {})
        )
    }

//    val musicLiveData = MutableLiveData<List<Music>>()
//
//    init {
//        val podcast = bundle.getParcelable<Music>(EXTRA_KEY_DATA)!!
//        musicLiveData.value = listOf(podcast)
//    }

    val musicLiveData = MutableLiveData<List<Music>>()
    val isAdvertisementAvailableData = MutableLiveData<Boolean>()

    init {
        // progressLiveData.value = true

        val type = bundle.getString(EXTRA_MUSIC_TYPE)
        val music = bundle.getParcelable<Music>(EXTRA_MUSIC)

        var enterCount = sharedPreferences.getInt(ENTERANCE_COUNT, 0)
        val isAdvAllowed = sharedPreferences.getBoolean(IS_ADVERTISEMENT_ALLOWED, true)
        if (enterCount > FIRST_ADD_ENTERANCE_COUNT && isAdvAllowed) {
            isAdvertisementAvailableData.value = true
        } else {
            enterCount++
            sharedPreferences.edit().putInt(ENTERANCE_COUNT, enterCount).apply()
            isAdvertisementAvailableData.value = false
        }

        music?.let { musicLiveData.value = listOf(it) } ?: musicRepository.getMusics (type, 0)
        .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<List<Music>>(compositeDisposable) {
                override fun onSuccess(t: List<Music>) {
                    musicLiveData.value = t
                }
            })
    }

    fun loadPodcastId(podcastId: String) {
        musicRepository.getMusic(podcastId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<Music>(compositeDisposable) {
                override fun onSuccess(t: Music) {
                    musicLiveData.value = listOf(t)
                }
            })
    }
}