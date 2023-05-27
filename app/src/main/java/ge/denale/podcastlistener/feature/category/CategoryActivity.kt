package ge.denale.podcastlistener.feature.category

import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ge.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ge.denale.podcastlistener.common.convertDpToPixel
import ge.denale.podcastlistener.data.Genre
import ge.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ge.denale.podcastlistener.feature.adapter.CategoryAdapter
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import ge.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ge.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.fragment_category.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CategoryActivity : MusicPlayerOnlineActivity(),CategoryAdapter.OnClickCategory {
    val categoryAdapter: CategoryAdapter by inject()
    val categoryViewModel: CategoryViewModel by viewModel()
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        recycle_category_all_activity.post {
            val sColumnWidth = 110
            val spanCount = Math.floor(
                (recycle_category_all_activity.width / convertDpToPixel(
                    sColumnWidth.toFloat(),
                    this
                )).toDouble()
            ).toInt()
            categoryViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(this, spanCount)

            recycle_category_all_activity.layoutManager = layoutManager
            recycle_category_all_activity.adapter = categoryAdapter
            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    categoryViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_category_all_activity!!.addOnScrollListener(endlessScrollListener)
        }

        disposable = categoryViewModel.categoryLiveData.subscribe {
            categoryAdapter.categoryArray.addAll(it)
            categoryAdapter.notifyDataSetChanged()
            textViewEmpty.isVisible = categoryAdapter.categoryArray.isEmpty()
        }
        categoryAdapter.onClickCategory = this

        img_back_category.setOnClickListener {
            finish()
        }
    }

    override fun onClick(genre: Genre) {
        startActivity(Intent(this, MusicsActivity::class.java).apply {
            putExtra(SCREEN_TITLE_DATA, genre.name)
            putExtra(EXTRA_GENRE_ID_KEY_DATA, genre.id)
        })
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}