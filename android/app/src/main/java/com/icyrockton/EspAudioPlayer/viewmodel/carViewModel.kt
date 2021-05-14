package com.icyrockton.EspAudioPlayer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyrockton.EspAudioPlayer.network.ESPApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent


class CarViewModel : ViewModel() {
    private val api: ESPApiService by KoinJavaComponent.inject(ESPApiService::class.java)

    private suspend fun carForward() {
        api.carForward()
    }

    private suspend fun carLeft() {
        api.carLeft()
    }

    private suspend fun carRight() {
        api.carRight()
    }

    private suspend fun carBackward() {
        api.carBackward()
    }

    private suspend fun carForwardLeft() {
        api.carForwardLeft()
    }

    private suspend fun carForwardRight() {
        api.carForwardRight()
    }

    private suspend fun carBackwardLeft() {
        api.carBackwardLeft()
    }

    private suspend fun carBackwardRight() {
        api.carBackwardRight()
    }

    private var previousTime: Long = 0

    suspend fun carDirection(offsetX: Float, offsetY: Float) {
        if (System.currentTimeMillis() - previousTime > 500) {  //500ms触发一次
            previousTime = System.currentTimeMillis()
            val radian = Math.atan2(offsetX.toDouble(), offsetY.toDouble())
            val degrees = Math.toDegrees(radian)
            if (degrees < 22.5 && degrees > -22.5) {
                carRight()
            } else if (degrees >= 22.5 && degrees < 67.5) {
                carForwardRight()
            } else if (degrees >= 67.5 && degrees < 112.5) {
                carForward()
            } else if (degrees >= 112.5 && degrees < 157.5) {
                carForwardLeft()
            } else if (degrees >= -157.5 && degrees < -112.5) {
                carBackwardLeft()
            } else if (degrees >= -112.5 && degrees < -67.5) {
                carBackward()
            } else if (degrees >= -67.5 && degrees < -22.5) {
                carBackwardRight()
            } else if (degrees < -157.5 || degrees > 157.5) {
                carLeft()
            }

        }
    }

    suspend fun carAccelerateX2() {
        api.carAccelerateX2()
    }

    suspend fun carAccelerate() {
        api.carAccelerate()
    }

    suspend fun carBrake() = withContext(Dispatchers.IO) {
        api.carBrake()
    }


    private var go = false

    suspend fun carStarAccelerate() = withContext(Dispatchers.IO) {
        launch {
            go = true
            while (go) {
                api.carAccelerate()
                Log.e("compose", "carStarAccelerate: 小车加速中X1")
                delay(300L)
            }
        }
    }

    suspend fun carStarDecelerate() = withContext(Dispatchers.IO) {
        go = false
        api.carDecelerate()
    }

    suspend fun carStarAccelerateX2() = withContext(Dispatchers.IO) {
        go = true
        while (go) {
            api.carAccelerateX2()
            Log.e("compose", "carStarAccelerate: 小车加速中X2")
            delay(300L)
        }
    }
}