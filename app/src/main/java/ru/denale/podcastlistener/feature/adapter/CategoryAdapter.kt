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

private const val CATEGORY_VIEW_TYPE = 1
private const val ADV_VIEW_TYPE = 2

class CategoryAdapter(val imageLoadingService: ImageLoadingService) :
    RecyclerView.Adapter<ViewHolder>() {

    var onClickCategory:OnClickCategory?=null
    var categoryArray = ArrayList<Any>()
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
        return when (viewType) {
            CATEGORY_VIEW_TYPE -> CategoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
            )
            ADV_VIEW_TYPE -> AdvViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.advertisement_item, parent, false)
            )
            else -> CategoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (categoryArray[position]) {
            is Author -> CATEGORY_VIEW_TYPE
            is NativeAd -> ADV_VIEW_TYPE
            else -> -1
        }
    }

    override fun getItemCount(): Int = categoryArray.size

    interface OnClickCategory {
        fun onClick(category: Genre)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is CategoryAdapter.CategoryViewHolder -> holder.bindCategory(categoryArray[position] as Genre)
            is AdvViewHolder -> holder.bindCategory(categoryArray[position] as NativeAd)
        }
    }
}