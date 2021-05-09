package com.icyrockton.EspAudioPlayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyrockton.EspAudioPlayer.network.ESPApiService
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent


class CarViewModel : ViewModel() {
    private val api: ESPApiService by KoinJavaComponent.inject(ESPApiService::class.java)

    fun carForward() {
        viewModelScope.launch {
            api.carForward()
        }
    }

    fun carLeft() {
        viewModelScope.launch {
            api.carLeft()
        }
    }

    fun carRight() {
        viewModelScope.launch {
            api.carRight()
        }
    }

    fun carBackward() {
        viewModelScope.launch {
            api.carBackward()
        }
    }

    fun carForwardLeft() {
        viewModelScope.launch {
            api.carForwardLeft()
        }
    }

    fun carForwardRight() {
        viewModelScope.launch {
            api.carForwardRight()
        }
    }

    fun carBackwardLeft() {
        viewModelScope.launch {
            carBackwardLeft()
        }
    }

    fun carBackwardRight() {
        viewModelScope.launch {
            carBackwardRight()
        }
    }
}