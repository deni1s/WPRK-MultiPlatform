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
import kotlinx.android.synthetic.main.activity_musics.*
import kotlinx.android.synthetic.main.fragment_authors.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

@Deprecated("Это для боттом шита, сам экран на активити")
class AuthorsFragment : MusicPlayerOnlineFragment(), AuthorsAdapter.OnClickAuthor {
    val authorsViewModel: AuthorsViewModel by viewModel()
    val authorsAdapter:AuthorsAdapter by inject()
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycle_category_all.doOnNextLayout {
            val sColumnWidth = 110
            val spanCount = Math.floor((recycle_category_all.width / convertDpToPixel(sColumnWidth.toFloat(),requireContext())).toDouble()).toInt()
            authorsViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(requireContext(),spanCount)
            recycle_category_all.layoutManager = layoutManager
            recycle_category_all.adapter = authorsAdapter
            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    authorsViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_musics_all!!.addOnScrollListener(endlessScrollListener)
        }
        authorsAdapter.onAuthorClicked = this

        disposable = authorsViewModel.authorLiveData.subscribe {
            authorsAdapter.authorsArray.addAll(it)
            authorsAdapter.notifyItemRangeChanged(authorsAdapter.authorsArray.size, it.size)
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