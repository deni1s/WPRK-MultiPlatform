package ge.denale.podcastlistener.feature.authors

import androidx.recyclerview.widget.DiffUtil

class AuthrosDiffUtilCallback: DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        val areSameClasses = oldItem::class.java == newItem::class.java
        return true
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        TODO("Not yet implemented")
    }

}