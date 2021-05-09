package com.icyrockton.EspAudioPlayer

import android.app.Application
import com.icyrockton.EspAudioPlayer.network.networkModule
import com.icyrockton.EspAudioPlayer.viewmodel.allViewModels
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.logging.Logger

@ExperimentalSerializationApi
class ESPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        注入koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ESPApplication)
            modules(networkModule)
            modules(allViewModels)

        }
    }
}