package ru.denale.podcastlistener.data.database

import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse

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
            imageUrl = entity.imageUrl
        )
    }
}

fun List<Music>.toStorageModel(type: String, timeStamp: Long): List<MusicBdEntity> {
    return this.map { entity ->
        MusicBdEntity(
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
            type = type,
            timestamp = timeStamp,
            screenTitle = ""
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
            author = entity.author,
            authorId = entity.authorId,
            genreId = entity.genreId,
            durationString = entity.durationString,
            mediaUrl = entity.mediaUrl,
            imageUrl = entity.imageUrl,
            type = type,
            timestamp = timeStamp,
            screenTitle = screenTitle
        )
    }
}
