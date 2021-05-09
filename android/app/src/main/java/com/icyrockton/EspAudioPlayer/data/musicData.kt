package com.icyrockton.EspAudioPlayer.data

import kotlinx.serialization.Serializable


@Serializable
data class SongDetail(
    val id: Long,
    val url: String? =null,  //最终的播放视频URL 可能为null
    val type: String ? = null,
    val size: Long,
    val code: Int // -110 无权限    200有权限
)

@Serializable
data class SongPlayInfo(
    val id:Long,
    val name:String,
    val artist:String,
    val cd:String,
    val picUrl: String,
)

@Serializable
data class MusicDetail (
    val id:Long, //id
    val name: String, //歌曲名称
    val artist :String, //作者
    val cd :String, //专辑
    val picUrl: String //专辑图片
)

@Serializable
data class SearchSong(
    val name: String, //歌曲名称
    val id: Long, //ID编号
    val ar:List<Artist>,
    val al:Cd,
    val alia:List<String> //歌曲别名
)

@Serializable
data class Artist( //歌手
    val id:Long,
    val name:String
)

@Serializable
data class Cd(
    val id: Long, //专辑ID
    val picUrl:String, //专辑图片URL
    val name:String //专辑名称
)