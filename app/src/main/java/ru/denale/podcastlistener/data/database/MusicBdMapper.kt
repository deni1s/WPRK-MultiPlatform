package ru.denale.podcastlistener.data.database

import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse

private const val SEPARATOR = ","

fun List<MusicBdEntity>.toDomainModel(): List<Music> {
    return this.map { entity ->
        Music(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            author = entity.author,
            authorId = entity.authorId,
            genreId = entity.genreId,
            durationString = entity.durationString,
            mediaUrl = entity.mediaUrl,
            imageUrl = entity.imageUrl,
            warningDescription = entity.warning,
            genreIds = entity.genreIds?.split(SEPARATOR) ?: listOf(entity.genreId) ,
            authorIds = entity.authorIds?.split(SEPARATOR) ?: listOf(entity.authorId)
        )
    }
}

fun WaveResponse.toStorageModel(type: String, timeStamp: Long): List<MusicBdEntity> {
    val screenTitle = this.title.orEmpty()
    return this.podcasts.map { entity ->
        MusicBdEntity(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            author = entity.author.orEmpty(),
            authorId = "",
            genreId = "",
            durationString = entity.durationString,
            mediaUrl = entity.mediaUrl,
            imageUrl = entity.imageUrl,
            type = type,
            timestamp = timeStamp,
            screenTitle = screenTitle,
            warning = entity.warningDescription,
            genreIds = entity.genreIds?.joinToString(SEPARATOR) ?: entity.genreId.orEmpty(),
            authorIds = entity.authorIds?.joinToString(SEPARATOR) ?: entity.authorId.orEmpty()
        )
    }
}
