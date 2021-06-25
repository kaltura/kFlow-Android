package com.kaltura.kflow.data.cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaltura.kflow.data.entity.MultilingualStringValueEntity

/**
 * Created by alex_lytvynenko on 16.06.2021.
 */
class MultilingualStringValueConverter {
    @TypeConverter
    fun fromString(value: String?): Map<String, MultilingualStringValueEntity>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, MultilingualStringValueEntity>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, MultilingualStringValueEntity>?): String? =
        if (map == null) null
        else Gson().toJson(map)
}