package ge.denale.podcastlistener.feature.category

import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.EXTRA_KEY_DATA
import ge.denale.podcastlistener.common.MusicPlayerOnlineFragment
import ge.denale.podcastlistener.common.convertDpToPixel
import ge.denale.podcastlistener.data.Genre
import ge.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ge.denale.podcastlistener.feature.adapter.CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ge.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_musics.*
import kotlinx.android.synthetic.main.fragment_category.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

@Deprecated("Это для боттом шита, сам экран на активити")
class CategoryFragment : MusicPlayerOnlineFragment(),CategoryAdapter.OnClickCategory {
    val categoryViewModel: CategoryViewModel by viewModel()
    val categoryAdapter:CategoryAdapter by inject()
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycle_category_all.post {
            val sColumnWidth = 110
            val spanCount = Math.floor((recycle_category_all.width / convertDpToPixel(sColumnWidth.toFloat(),requireContext())).toDouble()).toInt()
            categoryViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(requireContext(), spanCount)
            recycle_category_all.layoutManager = layoutManager
            recycle_category_all.adapter = categoryAdapter

            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    categoryViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_category_all!!.addOnScrollListener(endlessScrollListener)

        }
        categoryAdapter.onClickCategory = this
        disposable = categoryViewModel.categoryLiveData.subscribe {
            categoryAdapter.categoryArray.addAll(it)
            categoryAdapter.notifyItemRangeChanged(categoryAdapter.categoryArray.size, it.size)
        }

        categoryViewModel.progressbarLiveData.observe(viewLifecycleOwner) {
            setProgressIndicator(it)
        }
    }

    override fun onClick(category: Genre) {
        startActivity(Intent(requireContext(), MusicsActivity::class.java).apply {
            putExtra(EXTRA_KEY_DATA, category.id)
        })
    }

    override fun onDestroyView() {
        disposable?.dispose()
        disposable = null
        super.onDestroyView()
    }
}
