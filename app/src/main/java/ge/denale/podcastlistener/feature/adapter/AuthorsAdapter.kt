package ge.denale.podcastlistener.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yandex.mobile.ads.nativeads.NativeAd
import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.implementSpringAnimationTrait
import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.services.ImageLoadingService

private const val AUTHORS_VIEW_TYPE = 1
private const val ADV_VIEW_TYPE = 2

class AuthorsAdapter(val imageLoadingService: ImageLoadingService) :
    ListAdapter<Any, ViewHolder>(AuthorsDiffUtlCallback()) {

    var onAuthorClicked: OnClickAuthor? = null
    var authorsArray = ArrayList<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class AuthorsViewHolder(itemView: View) : ViewHolder(itemView) {

        val categoryTv: TextView = itemView.findViewById(R.id.tv_category)
        val categoryIv: ImageView = itemView.findViewById(R.id.img_category)

        fun bindCategory(category: Author) {
            categoryTv.text = category.name
            imageLoadingService.load(categoryIv, category.imageUrl, itemView.context)

            itemView.implementSpringAnimationTrait()
            itemView.setOnClickListener {
                onAuthorClicked?.onClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            AUTHORS_VIEW_TYPE -> AuthorsViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
            )
            ADV_VIEW_TYPE -> AdvViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.advertisement_item, parent, false)
            )
            else -> AuthorsViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is AuthorsViewHolder -> holder.bindCategory(authorsArray[position] as Author)
            is AdvViewHolder -> holder.bindCategory(authorsArray[position] as NativeAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (authorsArray[position]) {
            is Author -> AUTHORS_VIEW_TYPE
            is NativeAd -> ADV_VIEW_TYPE
            else -> -1
        }
    }

    override fun getItemCount(): Int = authorsArray.size

    interface OnClickAuthor {
        fun onClick(category: Author)
    }
}