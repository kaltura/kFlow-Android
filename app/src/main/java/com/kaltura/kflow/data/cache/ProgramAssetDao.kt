package com.kaltura.kflow.data.cache

import androidx.room.*
import com.kaltura.kflow.data.entity.ProgramAssetEntity

/**
 * Created by alex_lytvynenko on 14.06.2021.
 */
@Dao
interface ProgramAssetDao {
    @Query("SELECT * from program_assets")
    suspend fun getAll(): List<ProgramAssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(programAssets: List<ProgramAssetEntity>)

    @Query("DELETE FROM program_assets")
    suspend fun deleteAll()
}