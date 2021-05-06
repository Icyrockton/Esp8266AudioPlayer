import {makeAutoObservable, runInAction} from "mobx";
import {EspApi} from "../network/Api";

export type SensorDataItem  = {
    id:number
    date : number
}
export type TemperatureItem =  SensorDataItem & { temperature : number}
export type HumidityItem =  SensorDataItem & { humidity : number}

export class SensorData{
    temperatureNumber : number = 0 //温度
    temperatureDataList : TemperatureItem [] = []
    humidityNumber: number = 0 //湿度
    humidityDataList : HumidityItem [] = []

    constructor() {
        makeAutoObservable(this)
        this.update()

        setInterval(()=>{
            this.update()
        },3000)
    }

    //更新数据
    async update(){
        const humidityData = await EspApi.getHumidityData();
        const temperatureData = await EspApi.getTemperatureData();
        runInAction(()=>{
            //更新数据
            this.temperatureNumber = temperatureData[temperatureData.length - 1].temperature
            this.humidityNumber = humidityData[humidityData.length -1].humidity
            this.humidityDataList = humidityData
            this.temperatureDataList = temperatureData
        })

    }
}
const useSensorData = new SensorData()
export default useSensorData
