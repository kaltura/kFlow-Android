package com.kaltura.kflow.data.entity

/**
 * Created by alex_lytvynenko on 17.06.2021.
 */
data class MediaImageEntity(
    val ratio: String?,
    val width: Int?,
    val height: Int?,
    val url: String?,
    val version: Int?,
    val id: String?,
    val isDefault: Boolean?,
    val imageTypeId: Long?
)