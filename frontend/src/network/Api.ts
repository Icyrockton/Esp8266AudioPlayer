import axios from "axios";
import {HumidityItem, TemperatureItem} from "../store/SensorData";
import { SearchSong} from "../store/MusicData";


export class Api {

    //axios实例
    private _axios = axios.create({
        baseURL: "http://127.0.0.1:4000/"
    })

    constructor() {

    }

    async getTemperatureData() {
        const response = await this._axios.get("sensor/temperature");
        return response.data as TemperatureItem[]
    }

    async getHumidityData() {
        const response = await this._axios.get("sensor/humidity");
        return response.data as HumidityItem[]
    }
    async searchSong(str:string) {
        const response = await this._axios.get(`music/search/${str}`);
        return response.data as SearchSong[]
    }

    async playMusic(songID:number){
        const response = await this._axios.get(`music/play/${songID}`);
        return response
    }

    async musicDetail (songID:number) {
        return await this._axios.get(`music/detail/${songID}`)
    }

    async pauseMusic() {
        return await  this._axios.get(`music/pause`)
    }

    async cancelMusic() {
        return await  this._axios.get(`music/cancel`)
    }

    async resumeMusic() {
        return await  this._axios.get(`music/resume`)

    }

    async setVolume(vol: number) {
        return await  this._axios.get(`music/volume/${vol}`)
    }
}


export const EspApi = new Api()
