package com.kaltura.kflow.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kaltura.kflow.data.entity.ProgramAssetEntity

/**
 * Created by alex_lytvynenko on 14.06.2021.
 */
@Database(entities = [ProgramAssetEntity::class], version = 5)
@TypeConverters(
    MultilingualStringValueConverter::class,
    MultilingualStringValueArrayConverter::class,
    MediaImageConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun programAssetDao(): ProgramAssetDao
}