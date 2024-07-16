package ru.denale.podcastlistener.feature.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.denale.podcastlistener.data.Author

class AuthorsDiffUtlCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        val areTheSameClasses = oldItem::class.java == newItem::class.java
        if (!areTheSameClasses) return false
        return when (oldItem) {
            is Author -> oldItem.id == (newItem as Author).id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return if (oldItem is Author && newItem is Author) {
             oldItem == newItem
        } else false
    }
}