import express, {response} from 'express'
import {writeFile} from "fs";
import {artist_album, banner, top_playlist, toplist_artist} from 'NeteaseCloudMusicApi'
import NeteaseMusic from "simple-netease-cloud-music";
import * as mqtt from "mqtt"
import {MqttESPClient} from "./mqtt";
import {Mysql} from "./mysql";

const app = express();
const mysql = new Mysql();
const mqttESPClient = new MqttESPClient(mysql);

var music = new NeteaseMusic();

// music.search("慢热").then(data=>{
//     console.log(data.result)
// })

// music.url('1826675290').then(data=>{
//     console.log(data)
// })
app.use(function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "*"); // update to match the domain you will make the request from
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
})

app.get("/", ((req, res) => {
    res.send("ESP8266AudioPlayer")
}))

app.get("/sensor/temperature", (req, res) => {
    mysql.queryTemperature().then(data => {
        res.send(data)
    })
})
app.get("/sensor/humidity", (req, res) => {
    mysql.queryHumidity().then(data => {
        res.send(data)
    })
})
type  SearchSong = {
    name: string //歌曲名称
    id: number //ID编号
    ar: {
        id: number  //歌手ID
        name: string  //歌手名称
    },
    al: {
        id: number //专辑ID
        picUrl: string //歌曲图片
    },
    alia: string[] //歌曲别名
}
type SearchResult = {
    result: { songs: SearchSong[], songCount: number },
    code: string
}

//获取歌曲
app.get("/music/search/:queryStr", (req, res) => {
    const queryStr: string = req.params.queryStr;
    music.search(queryStr, undefined, 15).then((data) => {
        res.send(data.result.songs)
    })
})


type SongDetail = {
    id: number,
    url: string | null,  //最终的播放视频URL 可能为null
    type: string,
    size: number
    code: number // -110 无权限    200有权限
}
app.get("/music/play/:id", ((req, res) => {
    const songID = req.params.id;

    music.url(songID).then(data => {
        res.send(data.data[0])
        if (data.data[0].url) {  //不为Null 通过MQTT发送播放地址
            mqttESPClient.playMusic(data.data[0].url)
        }
    })
}))
app.get("/music/detail/:id", ((req, res) => {
    const songID = req.params.id;

    music.song(songID).then(data => {
        const song = data["songs"][0];
        const response = {
            id: song["id"],
            name : song["name"],
            artist : song["ar"][0]["name"],
            cd:song["al"]["name"],
            picUrl:song ["al"]["picUrl"]
        }
        res.send(response)

    })
}))
//暂停音乐
app.get("/music/pause",((req, res) => {
    mqttESPClient.pauseMusic()
    res.send("ok")
}))
//取消音乐
app.get("/music/cancel",((req, res) => {
    mqttESPClient.cancelMusic()
    res.send("ok")
}))
//取消音乐
app.get("/music/resume",((req, res) => {
    mqttESPClient.resumeMusic()
    res.send("ok")
}))
//设置音量
//取消音乐
app.get("/music/volume/:vol",((req, res) => {
    const volume = req.params.vol;
    mqttESPClient.setVolume(parseInt(volume))
    res.send("ok")
}))

app.get("/music/hot",(req, res) => {
    music.playlist("3778678").then(data => {
        res.send(data["playlist"]["tracks"].slice(0,20))
    })
})

app.listen(4000, () => {
    console.log('服务器启动中')
})

