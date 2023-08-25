package ru.denale.podcastlistener.feature.authors

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_authors.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.img_back_category
import kotlinx.android.synthetic.main.activity_category.recycle_category_all_activity
import kotlinx.android.synthetic.main.activity_category.textViewEmpty
import kotlinx.android.synthetic.main.activity_musics.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class AuthorsActivity : MusicPlayerOnlineActivity(),AuthorsAdapter.OnClickAuthor {
    val authorsAdapter: AuthorsAdapter by inject()
    val autorsViewModel: AuthorsViewModel by viewModel()
    private var disposable: Disposable? = null
    private lateinit var endlessScrollListener: EndlessScroll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authors)

        progress_all_authors_activity.isVisible = true
        recycle_category_all_activity.post {
            val sColumnWidth = 110
            val spanCount = Math.floor(
                (recycle_category_all_activity.width / convertDpToPixel(
                    sColumnWidth.toFloat(),
                    this
                )).toDouble()
            ).toInt()
            autorsViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(this, spanCount)
            recycle_category_all_activity.layoutManager = layoutManager
            recycle_category_all_activity.adapter = authorsAdapter

            endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    autorsViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_category_all_activity!!.addOnScrollListener(endlessScrollListener)
        }

        disposable = autorsViewModel.authorLiveData.observeOn(AndroidSchedulers.mainThread()).subscribe {
       //     val previousSize = authorsAdapter.authorsArray.size
            authorsAdapter.authorsArray.addAll(it)
            authorsAdapter.notifyDataSetChanged()
            textViewEmpty.isVisible = authorsAdapter.authorsArray.isEmpty()
            progress_all_authors_activity.isVisible = false
         //   authorsAdapter.notifyItemRangeChanged(previousSize, authorsAdapter.authorsArray.size)
        }

        disposable = autorsViewModel.errorLiveData.observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (authorsAdapter.authorsArray.isEmpty()) {
                textViewEmpty.isVisible = true
                textViewEmpty.text = it
                progress_all_authors_activity.isVisible = false
            }
            //   authorsAdapter.notifyItemRangeChanged(previousSize, authorsAdapter.authorsArray.size)
        }

        authorsAdapter.onAuthorClicked = this

        img_back_category.setOnClickListener {
            finish()
        }
    }

    override fun onClick(author: Author) {
        startActivity(Intent(this, MusicsActivity::class.java).apply {
            putExtra(EXTRA_AUTHOR_ID_KEY_DATA, author.id)
            putExtra(SCREEN_TITLE_DATA, author.name)
        })
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}