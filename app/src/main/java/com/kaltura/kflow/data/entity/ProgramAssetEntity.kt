package com.kaltura.kflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by alex_lytvynenko on 14.06.2021.
 */
@Entity(tableName = "program_assets")
data class ProgramAssetEntity(
    @PrimaryKey val id: Long,
    val type: Int?,
    val name: String?,
    val description: String?,
    val startDate: Long?,
    val endDate: Long?,
    val createDate: Long?,
    val updateDate: Long?,
    val externalId: String?,
    val epgChannelId: Long?,
    val epgId: String?,
    val relatedMediaId: Long?,
    val crid: String?,
    val linearAssetId: Long?,
    val enableCdvr: Boolean?,
    val enableCatchUp: Boolean?,
    val enableStartOver: Boolean?,
    val enableTrickPlay: Boolean?,
//    val metas: Map<String, MultilingualStringValueEntity>?,
//    val tags: Map<String, MultilingualStringValueArrayEntity>?,
//    val images: List<MediaImageEntity>?
)