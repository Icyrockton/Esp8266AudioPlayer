package com.icyrockton.EspAudioPlayer.network

import com.icyrockton.EspAudioPlayer.data.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.module
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.http.Path
import java.lang.reflect.Type

interface ESPApiService {
    @GET("sensor/humidity")
    suspend fun getHumidityData(): Response<List<HumidityItem>>

    @GET("sensor/temperature")
    suspend fun getTemperatureData(): Response<List<TemperatureItem>>

    @GET("music/search/{search}")
    suspend fun searchSong(@Path("search") search: String): Response<List<SearchSong>>

    @GET("music/hot")
    suspend fun hotMusic(): Response<List<SearchSong>>


    @GET("music/play/{songID}")
    suspend fun playMusic(@Path("songID") songID: Long): Response<SongDetail>

    @GET("music/detail/{songID}")
    suspend fun musicDetail(@Path("songID") songID: Long): Response<SongPlayInfo>

    @GET("music/pause")
    suspend fun musicPause(): Response<Unit>


    @GET("music/cancel")
    suspend fun musicCancel(): Response<Unit>

    @GET("music/resume")
    suspend fun musicResume(): Response<Unit>

    @GET("music/volume/{vol}")
    suspend fun setVolume(@Path("vol") vol: Int): Response<Unit>

    @GET("car/forward")
    suspend fun carForward(): Response<Unit>

    @GET("car/left")
    suspend fun carLeft(): Response<Unit>

    @GET("car/right")
    suspend fun carRight(): Response<Unit>

    @GET("car/backward")
    suspend fun carBackward(): Response<Unit>

    @GET("car/forwardLeft")
    suspend fun carForwardLeft(): Response<Unit>

    @GET("car/forwardRight")
    suspend fun carForwardRight(): Response<Unit>

    @GET("car/backwardLeft")
    suspend fun carBackwardLeft(): Response<Unit>

    @GET("car/backwardRight")
    suspend fun carBackwardRight(): Response<Unit>


}


//koin单例
@ExperimentalSerializationApi
val networkModule = module {
    single {
        Retrofit.Builder().baseUrl("http://192.168.123.179:4000").addConverterFactory(
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory(
                MediaType.parse("application/json")!!
            )
        ).build()
    }
    single {
        get<Retrofit>().create(ESPApiService::class.java)
    }
}
