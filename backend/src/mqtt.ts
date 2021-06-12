import * as mqtt from "mqtt";
import {MqttClient} from "mqtt";
import {ClientSubscribeCallback} from "mqtt/types/lib/client";
import {Mysql} from "./mysql";
import {stringify} from "querystring";

type CommonData = {
    data: number
}
type TemperatureData = CommonData
type HumidityData = CommonData

export class MqttESPClient {

    //broker地址
    static readonly BROKER_URL = ""
    static readonly TemperatureTopic = "sensor/temperature" //温度
    static readonly HumidityTopic = "sensor/humidity" //湿度
    static readonly MusicPlayTopic = "music/play"
    static readonly MusicPauseTopic = "music/pause"
    static readonly MusicResumeTopic = "music/resume"
    static readonly MusicCancelTopic = "music/cancel"
    static readonly MusicVolumeTopic = "music/volume"
    static readonly CarForwardTopic = "car/forward"
    static readonly CarBackwardTopic = "car/backward"
    static readonly CarLeftTopic = "car/left"
    static readonly CarRightTopic = "car/right"
    static readonly CarForwardLeftTopic = "car/forwardLeft"
    static readonly CarForwardRightTopic = "car/forwardRight"
    static readonly CarBackwardLeftTopic = "car/backwardLeft"
    static readonly CarBackwardRightTopic = "car/backwardRight"
    static readonly CarAccelerateX2Topic = "car/accelerateX2"
    static readonly CarAccelerateTopic = "car/accelerate"
    static readonly CarBrakeTopic = "car/brake"
    static readonly CarDecelerateTopic = "car/decelerate"
    static readonly ModeCarTopic = "mode/car"
    static readonly ModeMusicTopic = "mode/music"
    static readonly ModeSensorTopic = "mode/sensor"

    private client: MqttClient
    private _mysql: Mysql;

    constructor(mysql: Mysql) {
        this._mysql = mysql;
        this.client = mqtt.connect(MqttESPClient.BROKER_URL, {port: 1883});
        this.client.on("connect", () => {
            console.log('MQTT服务器连接成功')
            this.client.subscribe(MqttESPClient.TemperatureTopic)
            this.client.subscribe(MqttESPClient.HumidityTopic)
        })
        this.client.on("message", ((topic, payload) => {
            console.log(topic)
            console.log(payload.toString())
            switch (topic) {
                case MqttESPClient.TemperatureTopic :
                    console.log('插入温度')
                    const temperature = (JSON.parse(payload.toString()) as TemperatureData).data;
                    if (temperature == null)
                        return
                    this._mysql.insertTemperature(Date.now(), temperature)
                    break;
                case MqttESPClient.HumidityTopic:
                    console.log('插入湿度')
                    const humidity = (JSON.parse(payload.toString()) as HumidityData).data;
                    if (humidity == null)
                        return;
                    this._mysql.insertHumidity(Date.now(), humidity)
                    break;
            }
            console.log('有新的数据出来了')
            console.log(payload.toString())
        }))
    }


    playMusic(songUrl: string) {
        const data: PlayMusic = {
            songUrl: songUrl
        }
        console.log('播放音乐 ID:', songUrl)
        this.client.publish(MqttESPClient.MusicPlayTopic, JSON.stringify(data))
    }

    pauseMusic() {
        const data: PauseMusic = {
            pause: true
        }
        console.log('暂停音乐')
        this.client.publish(MqttESPClient.MusicPauseTopic, JSON.stringify(data))
    }

    resumeMusic() {
        const data: ResumeMusic = {
            resume: true
        }
        console.log('继续播放音乐')
        this.client.publish(MqttESPClient.MusicResumeTopic, JSON.stringify(data))
    }


    cancelMusic() {
        const data: CancelMusic = {
            cancel: true
        }
        console.log('取消音乐')
        this.client.publish(MqttESPClient.MusicCancelTopic, JSON.stringify(data))
    }

    setVolume(volume: number) {
        const data: Volume = {
            volume: volume
        }
        console.log('设置音量', volume)
        this.client.publish(MqttESPClient.MusicVolumeTopic, JSON.stringify(data))
    }

    car(direction: CarDirection | undefined) {
        if (direction == undefined)
            return
        switch (direction) {
            case CarDirection.Forward:
                this.client.publish(MqttESPClient.CarForwardTopic, "@")
                console.log("前进")
                break
            case CarDirection.ForwardLeft:
                this.client.publish(MqttESPClient.CarForwardLeftTopic, "@")
                console.log("前左")
                break
            case CarDirection.ForwardRight:
                this.client.publish(MqttESPClient.CarForwardRightTopic, "@")
                console.log("前右")
                break
            case CarDirection.Backward:
                this.client.publish(MqttESPClient.CarBackwardTopic, "@")
                console.log("后退")
                break
            case CarDirection.BackwardLeft:
                this.client.publish(MqttESPClient.CarBackwardLeftTopic, "@")
                console.log("后左")
                break
            case CarDirection.BackwardRight:
                this.client.publish(MqttESPClient.CarBackwardRightTopic, "@")
                console.log("后右")
                break
            case CarDirection.Left:
                this.client.publish(MqttESPClient.CarLeftTopic, "@")
                console.log("左")
                break
            case CarDirection.Right:
                this.client.publish(MqttESPClient.CarRightTopic, "@")
                console.log("右")
                break
            default:
                console.log("未找到")
                break
        }
    }

    carAccelerate(level: number) {
        console.log("加速 ", level)

        if (level == 1) {
            this.client.publish(MqttESPClient.CarAccelerateTopic, "@")
        } else {
            this.client.publish(MqttESPClient.CarAccelerateX2Topic, "@")
        }
    }

    carBrake() {
        console.log("刹车 ")
        this.client.publish(MqttESPClient.CarBrakeTopic, "@")

    }

    carDecelerate() {
        console.log("减速 ")
        this.client.publish(MqttESPClient.CarDecelerateTopic, "@")

    }

    modeChange(mode: String) {
        switch (mode) {
            case "music":
                console.log("音乐模式")
                this.client.publish(MqttESPClient.ModeMusicTopic, "@")
                break
            case "sensor":
                console.log("传感器模式")
                this.client.publish(MqttESPClient.ModeSensorTopic, "@")
                break
            case "car":
                console.log("小车模式")
                this.client.publish(MqttESPClient.ModeCarTopic, "@")
                break
            default:
                break
        }
    }
}


export enum CarDirection {
    Forward,
    Backward,
    Left,
    Right,
    ForwardLeft,
    ForwardRight,
    BackwardLeft,
    BackwardRight
}

interface ResumeMusic {
    resume: boolean
}

interface PauseMusic {
    pause: boolean
}

interface CancelMusic {
    cancel: boolean
}

interface PlayMusic {
    songUrl: string
}

interface Volume {
    volume: number
}
