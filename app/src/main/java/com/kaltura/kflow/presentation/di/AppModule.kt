package com.kaltura.kflow.presentation.di

import androidx.room.Room
import com.kaltura.kflow.data.cache.AppDatabase
import com.kaltura.kflow.data.entity.ProgramAssetMapper
import com.kaltura.kflow.manager.AwsManager
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Created by alex_lytvynenko on 12.02.2020.
 */
val appModule = module {
    single { PreferenceManager(androidContext()) }
    single { PhoenixApiManager(get()) }
    single { AwsManager(androidContext()) }
    // Cache
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "kflow_database"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<AppDatabase>().programAssetDao() }

    factory { ProgramAssetMapper() }
}