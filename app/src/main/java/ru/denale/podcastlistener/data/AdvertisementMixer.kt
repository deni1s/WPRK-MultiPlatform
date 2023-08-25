package ru.denale.podcastlistener.data

class AdvertisementMixer {

    fun mixAdvertisement(
        mainItems: List<Any>,
        advertisement: List<Any>,
        rowsCount: Int
    ): List<Any> {
        var advertisementIndex = 0
        val newList = mutableListOf<Any>()
        mainItems.forEachIndexed { index, author ->
            val humanIndex = index + 1 + advertisementIndex
            if (humanIndex % rowsCount == 0) {
                val deleting = humanIndex / rowsCount
                if (!isDoubledDivised(deleting)) {
                    advertisement.getOrNull(advertisementIndex)?.let {
                        newList.add(it)
                        advertisementIndex++
                    }
                    advertisement.getOrNull(advertisementIndex)?.let {
                        newList.add(it)
                        advertisementIndex++
                    }
                }
            }
            newList.add(author)
        }
        return newList
    }

    fun simpleMix(
        mainItems: List<Any>,
        advertisement: List<Any>,
        offset: Int
    ): List<Any> {
        var advertisementIndex = 0
        val newList = mutableListOf<Any>()
        mainItems.forEachIndexed { index, author ->
            val humanIndex = index + 1 + advertisementIndex
            if (humanIndex % offset == 0) {
                advertisement.getOrNull(advertisementIndex)?.let {
                    newList.add(it)
                    advertisementIndex++
                }
            }
            newList.add(author)
        }
        return newList
    }

    private fun isDoubledDivised(n: Int): Boolean {
        return n % 2 == 0
    }
}