package ge.denale.podcastlistener.feature.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessScroll : RecyclerView.OnScrollListener {
    // Минимальное количество оставшихся элементов в адаптере, после достижение
    // первого элемента из которых начнется загрузка данных
    private var visibleThreshold = 5

    // Текущий индекс в коллекции загруженных данных
    private var currentPage = 0

    // Общее количество элементов после предыдущей подгрузки новых данных
    private var previousTotalItemCount = 0

    // True - если мы в ожидании загрузки свежей порции данных
    private var loading = true

    // Индекс стартовой страницы, с которой начинается загрузка данных
    private val startingPageIndex = 0

    // это поле также используется когда EndlessRecyclerViewScrollListener используется для GridLayoutManager
    private var linearLayoutManager: LinearLayoutManager? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

    constructor(layoutManager: LinearLayoutManager) {
        this.linearLayoutManager = layoutManager
    }

    constructor(layoutManager: GridLayoutManager) {
        // GridLayoutManager наследуется от LinearLayoutManager и не переопределяет
        // #findLastVisibleItemPosition() и#findFirstVisibleItemPosition методы
        this.linearLayoutManager = layoutManager
        visibleThreshold = visibleThreshold * layoutManager.spanCount
    }

    constructor(layoutManager: StaggeredGridLayoutManager) {
        this.staggeredGridLayoutManager = layoutManager
        visibleThreshold = visibleThreshold * layoutManager.spanCount
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // Этот метод вызывается каждый раз когда пользователь скроллит список.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
      //  if (dy == 0) return
        var lastVisibleItemPosition = 0
        var totalItemCount = 0

        // Получаем актуальное количество элементов в списке и позицию последнего видимого элемента
        // в зависимости от используемого LayoutManager
        if (linearLayoutManager != null) {
            totalItemCount = linearLayoutManager!!.itemCount
            lastVisibleItemPosition = linearLayoutManager!!.findLastVisibleItemPosition()
        } else if (staggeredGridLayoutManager != null) {
            totalItemCount = staggeredGridLayoutManager!!.itemCount
            val lastVisibleItemPositions =
                staggeredGridLayoutManager!!.findLastVisibleItemPositions(null)
            // get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            currentPage = this.startingPageIndex
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                loading = true
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            onLoadMore(currentPage, totalItemCount)
            loading = true
        }
    }

    // Call this method whenever performing new searches or refresh data
    fun resetState() {
        this.currentPage = this.startingPageIndex
        this.previousTotalItemCount = 0
        this.loading = true
    }

    // Defines the process for actually loading more data based on page
    abstract fun onLoadMore(page: Int, totalItemsCount: Int)

}