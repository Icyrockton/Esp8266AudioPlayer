package com.icyrockton.EspAudioPlayer.viewmodel

import org.koin.dsl.module

//所有的viewmodel
val allViewModels = module {
    single {
        SensorViewModel()
    }
    single {
        MusicViewModel()
    }

    single {
        CarViewModel()
    }
}