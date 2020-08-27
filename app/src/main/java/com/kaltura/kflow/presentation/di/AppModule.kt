package com.kaltura.kflow.presentation.di

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
}