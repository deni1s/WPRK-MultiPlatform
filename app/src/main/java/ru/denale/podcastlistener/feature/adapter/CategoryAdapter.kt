package ru.denale.podcastlistener.feature.adapter

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.implementSpringAnimationTrait
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.services.ImageLoadingService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yandex.mobile.ads.nativeads.NativeAd
import ru.denale.podcastlistener.data.Author

class CategoryAdapter(val imageLoadingService: ImageLoadingService) :
    RecyclerView.Adapter<ViewHolder>() {

    var onClickCategory: OnClickCategory? = null
    var categoryArray = ArrayList<Genre>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class CategoryViewHolder(itemView: View) : ViewHolder(itemView) {

        val categoryTv: TextView = itemView.findViewById(R.id.tv_category)
        val categoryIv: ImageView = itemView.findViewById(R.id.img_category)

        fun bindCategory(category: Genre) {
            categoryTv.text = category.name
            imageLoadingService.load(categoryIv, category.imageUrl, itemView.context)

            itemView.implementSpringAnimationTrait()
            itemView.setOnClickListener {
                onClickCategory?.onClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        )
    }

    override fun getItemCount(): Int = categoryArray.size

    interface OnClickCategory {
        fun onClick(category: Genre)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is CategoryAdapter.CategoryViewHolder -> holder.bindCategory(categoryArray[position])
        }
    }
}