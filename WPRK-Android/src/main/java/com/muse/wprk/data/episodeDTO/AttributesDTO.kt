package com.muse.wprk.data.episodeDTO

import kotlinx.serialization.Serializable

@Serializable
data class AttributesDTO(
    val title: String,
    val description: String,
    val number: Int,
    val duration_in_mmss: String,
    val media_url: String
)