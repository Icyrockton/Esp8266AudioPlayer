import * as mqtt from "mqtt";
import {MqttClient} from "mqtt";
import {ClientSubscribeCallback} from "mqtt/types/lib/client";
import {Mysql} from "./mysql";
import {stringify} from "querystring";

type CommonData = {
    data:number
}
type TemperatureData = CommonData
type HumidityData = CommonData

export class MqttESPClient {

    //broker地址
    static  readonly  BROKER_URL = ""
    static readonly TemperatureTopic = "sensor/temperature" //温度
    static readonly HumidityTopic = "sensor/humidity" //湿度
    static readonly MusicPlayTopic = "music/play"
    static readonly MusicPauseTopic = "music/pause"
    static readonly MusicResumeTopic = "music/resume"
    static readonly MusicCancelTopic = "music/cancel"
    static readonly MusicVolumeTopic = "music/volume"
    private client : MqttClient
    private _mysql: Mysql;

    constructor(mysql: Mysql) {
        this._mysql = mysql;
        this.client = mqtt.connect(MqttESPClient.BROKER_URL,{port:1883});
        this.client.on("connect", ()=>{
            console.log('MQTT服务器连接成功')
            this.client.subscribe(MqttESPClient.TemperatureTopic)
            this.client.subscribe(MqttESPClient.HumidityTopic)
        })
        this.client.on("message", ((topic, payload) => {
            switch (topic) {
                case MqttESPClient.TemperatureTopic :
                    console.log('插入温度')
                    const temperature = (JSON.parse(payload.toString()) as TemperatureData).data;
                    if (temperature == null)
                        return
                    this._mysql.insertTemperature(Date.now(),temperature)
                    break;
                case MqttESPClient.HumidityTopic:
                    console.log('插入湿度')
                    const humidity = (JSON.parse(payload.toString()) as HumidityData).data;
                    if (humidity == null)
                        return;
                    this._mysql.insertHumidity(Date.now(),humidity)
                    break;
            }
            console.log('有新的数据出来了')
            console.log(payload.toString())
        }))
    }


    playMusic(songUrl: string) {
        const data: PlayMusic = {
            songUrl : songUrl
        }
        console.log('播放音乐 ID:',songUrl)
        this.client.publish(MqttESPClient.MusicPlayTopic,JSON.stringify(data))
    }

    pauseMusic() {
        const data : PauseMusic ={
            pause:true
        }
        console.log('暂停音乐')
        this.client.publish(MqttESPClient.MusicPauseTopic,JSON.stringify(data))
    }

    resumeMusic() {
        const data : ResumeMusic ={
            resume:true
        }
        console.log('继续播放音乐')
        this.client.publish(MqttESPClient.MusicResumeTopic,JSON.stringify(data))
    }


    cancelMusic() {
        const data : CancelMusic ={
            cancel:true
        }
        console.log('取消音乐')
        this.client.publish(MqttESPClient.MusicCancelTopic,JSON.stringify(data))
    }

    setVolume(volume: number) {
        const data : Volume ={
            volume:volume
        }
        console.log('设置音量',volume)
        this.client.publish(MqttESPClient.MusicVolumeTopic,JSON.stringify(data))

    }
}
interface ResumeMusic {
    resume:boolean
}
interface PauseMusic {
    pause:boolean
}
interface CancelMusic {
    cancel:boolean
}
interface PlayMusic {
    songUrl:string
}
interface Volume{
    volume:number
}
