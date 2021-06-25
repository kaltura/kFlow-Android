package com.kaltura.kflow.data.entity

/**
 * Created by alex_lytvynenko on 16.06.2021.
 */
data class MultilingualStringValueArrayEntity(
    val objects: List<MultilingualStringValueEntity>?,
    val objectType: String = "KalturaMultilingualStringValueArray"
)