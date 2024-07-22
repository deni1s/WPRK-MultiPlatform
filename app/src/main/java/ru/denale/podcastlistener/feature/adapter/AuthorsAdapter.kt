package ru.denale.podcastlistener.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.implementSpringAnimationTrait
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.services.ImageLoadingService


class AuthorsAdapter(val imageLoadingService: ImageLoadingService) :
    RecyclerView.Adapter<ViewHolder>() {

    var onAuthorClicked: OnClickAuthor? = null
    var authorsArray = ArrayList<Author>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class AuthorsViewHolder(itemView: View) : ViewHolder(itemView) {

        val categoryTv: TextView = itemView.findViewById(R.id.tv_category)
        val categoryIv: ImageView = itemView.findViewById(R.id.img_category)

        fun bindCategory(category: Author) {
            categoryTv.text = category.name.trim()
            imageLoadingService.load(categoryIv, category.imageUrl, itemView.context)

            itemView.implementSpringAnimationTrait()
            itemView.setOnClickListener {
                onAuthorClicked?.onClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AuthorsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is AuthorsViewHolder -> holder.bindCategory(authorsArray[position])
        }
    }

    override fun getItemCount(): Int = authorsArray.size

    interface OnClickAuthor {
        fun onClick(category: Author)
    }
}