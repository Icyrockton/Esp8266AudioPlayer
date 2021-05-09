package com.icyrockton.EspAudioPlayer.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.icyrockton.EspAudioPlayer.data.HumidityItem
import com.icyrockton.EspAudioPlayer.data.TemperatureItem
import com.icyrockton.EspAudioPlayer.network.ESPApiService
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject


class SensorViewModel : ViewModel() {
    private val api: ESPApiService by inject(ESPApiService::class.java)

    //    湿度
    private val _humidity = MutableLiveData<List<HumidityItem>>()
    val humidity: LiveData<List<HumidityItem>> = _humidity

    //    气温
    private val _temperature = MutableLiveData<List<TemperatureItem>>()
    val temperature: LiveData<List<TemperatureItem>> = _temperature

    //    最新的温度
    val newestTemperature = _temperature.map { list: List<TemperatureItem>? -> list?.lastOrNull()?.temperature ?: 0.0 }
    val newestHumidity = _humidity.map { list: List<HumidityItem>? -> list?.lastOrNull()?.humidity ?:0.0 }

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    fun resetSensorData(){
        _humidity.value=emptyList()
        _temperature.value = emptyList()

        Log.e("SensorViewModel", "resetSensorData: ", )
    }

    //    刷新数据
    suspend fun refreshSensorData() {
        viewModelScope.launch {
            _text.postValue("refreshSensorData: 刷新数据中...")
            val humidity = api.getHumidityData().body()
            val temperature = api.getTemperatureData().body()
            if (humidity != null && temperature != null) {
                this@SensorViewModel._humidity.postValue(humidity)
                this@SensorViewModel._temperature.postValue(temperature)
            }
            _text.postValue("刷新完毕")
        }
    }


}