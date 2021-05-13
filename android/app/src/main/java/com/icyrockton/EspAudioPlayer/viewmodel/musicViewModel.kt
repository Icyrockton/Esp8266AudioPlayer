package com.icyrockton.EspAudioPlayer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyrockton.EspAudioPlayer.data.SearchSong
import com.icyrockton.EspAudioPlayer.data.SongDetail
import com.icyrockton.EspAudioPlayer.data.SongPlayInfo
import com.icyrockton.EspAudioPlayer.network.ESPApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent


enum class MusicState {
    initial,
    loading,
    playing,
    error
}

//搜索状态
enum class SearchState {
    initial,
    loading,
    finished
}


class MusicViewModel : ViewModel() {
    //   查找结果
    private val _musicList = MutableLiveData<List<SearchSong>>()
    val musicList: LiveData<List<SearchSong>> = _musicList
    private val api: ESPApiService by KoinJavaComponent.inject(ESPApiService::class.java)
    private val _searchState = MutableLiveData<SearchState>(SearchState.initial)
    val searchState: LiveData<SearchState> = _searchState
    private val _playToolBarVisible = MutableLiveData<Boolean>(false)
    val playToolBar: LiveData<Boolean> = _playToolBarVisible
    private val _playInfo = MutableLiveData<SongPlayInfo>()
    val playInfo: LiveData<SongPlayInfo> = _playInfo
    private val _playing = MutableLiveData<Boolean>()
    val playing: LiveData<Boolean> = _playing
    suspend fun searchMusic(keyWord: String) {
            this@MusicViewModel._searchState.postValue(SearchState.loading)
            val response = api.searchSong(keyWord).body()
            if (response != null) {
                this@MusicViewModel._musicList.postValue(response)
            }
            this@MusicViewModel._searchState.postValue(SearchState.finished)
    }

    suspend fun musicCancel() {
            val response = api.musicCancel().body()
            this@MusicViewModel._playToolBarVisible.postValue(false)
            this@MusicViewModel._playing.postValue(false)
    }

    suspend fun musicResume() {
            val response = api.musicResume().body()
            this@MusicViewModel._playing.postValue(true)
    }

   suspend fun musicPause() {
            val response = api.musicPause().body()
            this@MusicViewModel._playing.postValue(false)
    }

   suspend fun setVolume(vol: Int) {
            Log.e("data", "setVolume: ${vol}")
            val reponse = api.setVolume(vol).body()
    }

    suspend fun playMusic(songID: Long) {
            val reponse = api.playMusic(songID).body()
            if (reponse?.url != null) {
                val playInfo = api.musicDetail(songID).body()
                if (playInfo != null) {
                    this@MusicViewModel._playInfo.postValue(playInfo)
                    this@MusicViewModel._playing.postValue(true)
                    this@MusicViewModel._playToolBarVisible.postValue(true)

                }
            }
    }

     suspend fun hotMusic() {
                this@MusicViewModel._searchState.postValue(SearchState.loading)

                var response = api.hotMusic().body()

                while (response == null) {
                    response = api.hotMusic().body()
                    delay(1000L)
                }
                if (response != null) {
                    Log.e("refresh", "hotMusic: ")
                    this@MusicViewModel._musicList.postValue(response)
                }
                this@MusicViewModel._searchState.postValue(SearchState.finished)
    }

     fun clearData() {
        this._searchState.postValue(SearchState.loading)
    }


}