package com.kaltura.kflow.data.cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaltura.kflow.data.entity.MediaImageEntity

/**
 * Created by alex_lytvynenko on 16.06.2021.
 */
class MediaImageConverter {
    @TypeConverter
    fun fromString(value: String?): List<MediaImageEntity>? {
        if (value == null) return null
        val listType = object : TypeToken<List<MediaImageEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringList(list: List<MediaImageEntity>?): String? =
        if (list == null) null
        else Gson().toJson(list)
}