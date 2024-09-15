package ru.denale.podcastlistener.feature.category

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineFragment
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.databinding.FragmentCategoryBinding

@Deprecated("Это для боттом шита, сам экран на активити")
class CategoryFragment : MusicPlayerOnlineFragment(),CategoryAdapter.OnClickCategory {
    val categoryViewModel: CategoryViewModel by viewModel()
    val categoryAdapter:CategoryAdapter by inject()
    private var disposable: Disposable? = null
    private lateinit var binding: FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleCategoryAll.post {
            val sColumnWidth = 110
            val spanCount = Math.floor((binding.recycleCategoryAll.width / convertDpToPixel(sColumnWidth.toFloat(),requireContext())).toDouble()).toInt()
            categoryViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(requireContext(), spanCount)
            binding.recycleCategoryAll.layoutManager = layoutManager
            binding.recycleCategoryAll.adapter = categoryAdapter

            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    categoryViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            binding.recycleCategoryAll!!.addOnScrollListener(endlessScrollListener)

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
