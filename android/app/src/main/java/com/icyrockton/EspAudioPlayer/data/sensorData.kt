package com.icyrockton.EspAudioPlayer.data

import androidx.lifecycle.ViewModel
import com.icyrockton.EspAudioPlayer.network.ESPApiService
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.inject


abstract class SensorDataItem {
    abstract val id: Long
    abstract val date: Long
}

@Serializable
data class TemperatureItem(
    override val id: Long,
    override val date: Long,
    val temperature: Double
) : SensorDataItem()


@Serializable
data class HumidityItem(override val id: Long, override val date: Long, val humidity: Double) :
    SensorDataItem()


