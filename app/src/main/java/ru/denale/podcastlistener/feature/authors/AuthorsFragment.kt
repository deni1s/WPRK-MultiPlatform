package ru.denale.podcastlistener.feature.authors

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineFragment
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.GridLayoutManager
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.databinding.FragmentAuthorsBinding
import ru.denale.podcastlistener.databinding.FragmentHomeBinding

@Deprecated("Это для боттом шита, сам экран на активити")
class AuthorsFragment : MusicPlayerOnlineFragment(), AuthorsAdapter.OnClickAuthor {
    val authorsViewModel: AuthorsViewModel by viewModel()
    val authorsAdapter:AuthorsAdapter by inject()
    private var disposable: Disposable? = null
    private lateinit var binding: FragmentAuthorsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleCategoryAll.doOnNextLayout {
            val sColumnWidth = 110
            val spanCount = Math.floor((binding.recycleCategoryAll.width / convertDpToPixel(sColumnWidth.toFloat(),requireContext())).toDouble()).toInt()
            authorsViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(requireContext(),spanCount)
            binding.recycleCategoryAll.layoutManager = layoutManager
            binding.recycleCategoryAll.adapter = authorsAdapter
            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    authorsViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            binding.recycleCategoryAll!!.addOnScrollListener(endlessScrollListener)
        }
        authorsAdapter.onAuthorClicked = this

        disposable = authorsViewModel.authorLiveData.subscribe {
            authorsAdapter.authorsArray.addAll(it.list)
            authorsAdapter.notifyItemRangeChanged(authorsAdapter.authorsArray.size, it.list.size)
        }

        authorsViewModel.progressbarLiveData.observe(viewLifecycleOwner) {
            setProgressIndicator(it)
        }
    }

    override fun onClick(author: Author) {
        startActivity(Intent(requireContext(), MusicsActivity::class.java).apply {
            putExtra(EXTRA_AUTHOR_ID_KEY_DATA, author.id)
        })
    }

    override fun onDestroyView() {
        disposable?.dispose()
        disposable = null
        super.onDestroyView()
    }
}