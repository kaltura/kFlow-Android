package com.kaltura.kflow.data.entity

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kaltura.client.types.MultilingualStringValue
import com.kaltura.client.types.ProgramAsset

/**
 * Created by alex_lytvynenko on 14.06.2021.
 */
class ProgramAssetMapper {

    fun transform(collection: Collection<ProgramAssetEntity>): ArrayList<ProgramAsset> {
        val list = arrayListOf<ProgramAsset>()
        for (entity in collection)
            list.add(transform(entity))
        return list
    }

    fun transform(entity: ProgramAssetEntity) = with(entity) {
        ProgramAsset(JsonObject().apply {
            addProperty("id", id)
            addProperty("type", type)
            addProperty("name", name)
            addProperty("description", description)
            addProperty("startDate", startDate)
            addProperty("endDate", endDate)
            addProperty("createDate", createDate)
            addProperty("updateDate", updateDate)
            addProperty("externalId", externalId)
            addProperty("epgChannelId", epgChannelId)
            addProperty("epgId", epgId)
            addProperty("relatedMediaId", relatedMediaId)
            addProperty("crid", crid)
            addProperty("linearAssetId", linearAssetId)
            addProperty("enableCdvr", enableCdvr)
            addProperty("enableCatchUp", enableCatchUp)
            addProperty("enableStartOver", enableStartOver)
            addProperty("enableTrickPlay", enableTrickPlay)
//            add("metas", Gson().toJsonTree(metas))
//            add("tags", Gson().toJsonTree(tags))
//            add("images", Gson().toJsonTree(images))
        })
    }

    fun transformToEntity(collection: Collection<ProgramAsset>): ArrayList<ProgramAssetEntity> {
        val list = arrayListOf<ProgramAssetEntity>()
        for (entity in collection)
            list.add(transformToEntity(entity))
        return list
    }

    fun transformToEntity(entity: ProgramAsset) = with(entity) {
        ProgramAssetEntity(
            id,
            type,
            name,
            description,
            startDate,
            endDate,
            createDate,
            updateDate,
            externalId,
            epgChannelId,
            epgId,
            relatedMediaId,
            crid,
            linearAssetId,
            enableCdvr,
            enableCatchUp,
            enableStartOver,
            enableTrickPlay,
//            metas.mapValues { MultilingualStringValueEntity((it.value as? MultilingualStringValue)?.value) },
//            tags.mapValues {
//                MultilingualStringValueArrayEntity(it.value.objects.map {
//                    MultilingualStringValueEntity(it.value)
//                })
//            },
//            images.map {
//                MediaImageEntity(
//                    it.ratio,
//                    it.width,
//                    it.height,
//                    it.url,
//                    it.version,
//                    it.id,
//                    it.isDefault,
//                    it.imageTypeId
//                )
//            }
        )
    }
}