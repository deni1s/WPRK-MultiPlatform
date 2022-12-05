package com.muse.wprk.data.podcastDTO

import kotlinx.serialization.Serializable

@Serializable
data class AttributesDTO(
    val title: String,
    val description: String?,
    val image_url: String
)