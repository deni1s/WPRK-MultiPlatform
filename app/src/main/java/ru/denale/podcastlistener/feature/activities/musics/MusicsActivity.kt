package ru.denale.podcastlistener.feature.activities.musics

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic1
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_authors.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_musics.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MusicsActivity : MusicPlayerOnlineActivity(), MusicAdapter.SetOnClick {
    val musicAdapter: MusicAdapter by inject()
    val musicsViewModel: MusicsViewModel by viewModel { parametersOf(intent.extras) }
    private var disposable: MutableList<Disposable> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musics)

        intent.getStringExtra(SCREEN_TITLE_DATA)?.let {
            textViewMusicTitle.text = it
        }

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycle_musics_all.layoutManager = layoutManager
        recycle_musics_all.adapter = musicAdapter
        val endlessScrollListener = object : EndlessScroll(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                musicsViewModel.loanNextNewsPart(totalItemsCount)
            }
        }
        recycle_musics_all!!.addOnScrollListener(endlessScrollListener)

        musicAdapter.setOnClick = this
        disposable.add(musicsViewModel.musicLiveData.subscribe {
            musicAdapter.authorsArray.addAll(it)
            musicAdapter.notifyItemRangeChanged(musicAdapter.authorsArray.size, it.size)
            text_view_empty_music.isVisible = musicAdapter.authorsArray.isEmpty()
            progress_music_list_activity.isVisible = false
        })

        disposable.add(musicsViewModel.errorLiveData.subscribe {
            if (musicAdapter.authorsArray.isEmpty()) {
                text_view_empty_music.isVisible = true
                text_view_empty_music.text = it
                progress_music_list_activity.isVisible = false
            }
        })

        img_back_musics.setOnClickListener {
            finish()
        }
    }

    override fun onClick(music: Music) {
        startActivity(Intent(this, PlayMusic1::class.java).apply {
            putExtra(EXTRA_MUSIC, music)
        })
    }

    override fun onDestroy() {
        disposable.forEach {
            it.dispose()
        }
        disposable.clear()
        super.onDestroy()
    }
}