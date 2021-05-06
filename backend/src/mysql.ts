
import {createConnection, createPool} from "mysql2/promise"

export class Mysql {

    private connection = createPool({
        host: 'localhost',
        user: 'root',
        database : 'esp8266',
        password : 'pop123456'
    })
    constructor() {

    }

    async insertTemperature(date:number,temperature:number){
        await this.connection.query('insert into `temperature` values (?,?,?)  ',[0,temperature,date]);
    }

    async insertHumidity(date : number , humidity:number){
        await this.connection.query('insert into `humidity` values (?,?,?)  ',[0,humidity,date]);
    }

    async queryTemperature(){
        let sqlData = await  this.connection.query("select * from `temperature`");
        return sqlData[0]
    }
    async queryHumidity(){
        let sqlData = await  this.connection.query("select * from `humidity`");
        return sqlData[0]
    }

}
