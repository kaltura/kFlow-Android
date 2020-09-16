package com.kaltura.kflow.presentation

import android.app.Application
import com.kaltura.kflow.presentation.di.appModule
import com.kaltura.kflow.presentation.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Created by alex_lytvynenko on 2019-06-25.
 */
class KFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@KFlowApplication)
            modules(listOf(appModule, viewModelModule))
        }
    }
}