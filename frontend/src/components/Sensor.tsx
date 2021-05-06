//MQTT的数据显示
import React, {useEffect} from "react";
import {buildStyles, CircularProgressbar} from "react-circular-progressbar";
import 'react-circular-progressbar/dist/styles.css';
import useSensorData, {HumidityItem, SensorData, TemperatureItem} from "../store/SensorData";
import {observer} from "mobx-react-lite";
import {CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, AreaChart, Area} from "recharts";
import {TooltipProps} from "recharts/src/component/Tooltip";

type SensorProps = {
    sensorData: SensorData
}
type SensorCircularProps = {
    temperatureData: number
    humidityData: number
}
type SensorChartProps={
    temperatureList: TemperatureItem[]
    humidityList: HumidityItem[]
}
const SensorCircularProgressbar = ({temperatureData, humidityData}: SensorCircularProps) => (
    <ul className="h-52  flex flex-wrap m-10">
        <li className="w-1/2 sm:w-1/2  lg:max-w-xl   p-4 ">
            <div className="shadow-xl h-full bg-white rounded-2xl flex ">
                <div
                    className="ml-4 -mt-8   w-24 h-24 p-2 rounded-md  bg-gradient-to-b from-yellow-300 to-yellow-500  ">
                    <TemperatureIcon/>
                </div>

                <div className="w-40 antialiased  h-full p-6">
                    <CircularProgressbar value={temperatureData} maxValue={50} minValue={0} background
                                         backgroundPadding={4} styles={
                        buildStyles({
                            // Rotation of path and trail, in number of turns (0-1)
                            // Whether to use rounded or flat corners on the ends - can use 'butt' or 'round'
                            strokeLinecap: "round",
                            // Text size
                            textSize: '14px',
                            // How long animation takes to go from one percentage to another, in seconds
                            pathTransitionDuration: 0.5,
                            pathColor: `#FFB000`,
                            textColor: '#000',
                            trailColor: '#FFEABA',
                            backgroundColor: '#FFEABA',
                        })
                    }/>
                </div>

                <div className="flex flex-col py-6 flex-1 items-start ">
                    <h1 className="text-2xl leading-8 ml-6">室内温度</h1>

                    <h1 className="text-4xl  mt-6 text-center text-yellow-400 ml-6">{temperatureData}℃</h1>

                </div>


            </div>
        </li>

        <li className="w-1/2 sm:w-1/2  lg:max-w-xl   p-4 ">
            <div className="shadow-xl h-full bg-white rounded-2xl flex ">
                <div
                    className="ml-4 -mt-8   w-24 h-24 p-2 rounded-md  bg-gradient-to-b from-green-200 to-green-400  ">
                    <HumidityIcon/>
                </div>

                <div className="w-40 antialiased  h-full p-6">
                    <CircularProgressbar value={humidityData} maxValue={100} minValue={0} background
                                         backgroundPadding={4} styles={
                        buildStyles({
                            // Rotation of path and trail, in number of turns (0-1)
                            // Whether to use rounded or flat corners on the ends - can use 'butt' or 'round'
                            strokeLinecap: "round",
                            // Text size
                            textSize: '14px',
                            // How long animation takes to go from one percentage to another, in seconds
                            pathTransitionDuration: 0.5,
                            pathColor: `#34B53A`,
                            textColor: '#000',
                            trailColor: '#E3FBD7',
                            backgroundColor: '#E3FBD7',
                        })
                    }/>
                </div>

                <div className="flex flex-col py-6 flex-1 items-start ">
                    <h1 className="text-2xl leading-8 ml-6">室内湿度</h1>

                    <h1 className="text-4xl  mt-6 text-center text-green-400 ml-6">{humidityData}RH%</h1>

                </div>


            </div>
        </li>


    </ul>)

const TemperatureTooltip = ({label, payload, active}: TooltipProps<number, number>) => {
    if (active && payload && payload.length > 0) {
        const temperature = payload[0].value;
        const date = new Date(parseInt(label));
        return (

            <div className="w-52 h-32 p-4 rounded-md bg-gray-700">
                <h1 className="text-white">监测时间</h1>
                <h1 className="text-white ml-6">{date.toLocaleTimeString()}</h1>
                <h1 className="text-white">温度</h1>
                <h1 className="text-white ml-6"> {temperature}℃</h1>
            </div>
        )
    } else
        return <></>
}
const DateTick = ({x,y,width, height,payload}:any)=>{
    const time = new Date(parseInt(payload.value));
    return <text x={x} y={y+13}  textAnchor={"middle"}> {`${time.getHours()}:${time.getMinutes()}:${time.getSeconds()}`}</text>
}
const SensorChart = ({temperatureList,humidityList}:SensorChartProps) => (
    <div className="flex flex-col w-full p-10  ">
        <div className="box-border w-full  p-6 bg-white rounded-xl shadow-lg   ">
            <h1 className="mb-4 text-xl text-yellow-500">温度趋势线</h1>
            <ResponsiveContainer width={"99%"} height={400}>
                <AreaChart  margin={{bottom: 20, left: 10, right: 10}} data={temperatureList}>
                    <defs>
                        <linearGradient id="temperature" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#FFB000" stopOpacity={0.75}/>
                            <stop offset="15%" stopColor="#FFB000" stopOpacity={0.65}/>
                            <stop offset="95%" stopColor="#FFEABA" stopOpacity={0}/>
                        </linearGradient>
                    </defs>
                    <Area type="monotone" dataKey="temperature" stroke="#FFB000" fillOpacity={1}
                          fill="url(#temperature)"/>
                    <XAxis dataKey="date" label={{value: "采集时间", position: "center", dy: 20}}
                    tick={<DateTick/>}/>
                    <YAxis dataKey={"temperature"} type={"number"} ticks={[0,10,20,30,40,50,60] }
                           label={{value: "温度(℃)", position: "insideLeft", angle: -90, dy: -10}} />
                    <CartesianGrid stroke="#c3c3c3" strokeDasharray="3 3 "/>
                    <Tooltip content={<TemperatureTooltip/>}/>
                </AreaChart>
            </ResponsiveContainer>
        </div>

    {/*    湿度*/}
        <div className="box-border w-full  p-6 bg-white rounded-xl shadow-lg mt-12   ">
            <h1 className="mb-4 text-xl text-green-500">湿度趋势线</h1>
            <ResponsiveContainer width={"99%"} height={400}>
                <AreaChart margin={{bottom: 20, left: 10, right: 10}} data={humidityList} >
                    <defs>
                        <linearGradient id="humidity" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#34B53A" stopOpacity={0.75}/>
                            <stop offset="15%" stopColor="#34B53A" stopOpacity={0.65}/>
                            <stop offset="95%" stopColor="#E3FBD7" stopOpacity={0}/>
                        </linearGradient>
                    </defs>
                    <Area type="monotone" dataKey="humidity" stroke="#34B53A" fillOpacity={1}
                          fill="url(#humidity)"/>
                    <XAxis dataKey="date" label={{value: "采集时间", position: "center", dy: 20}}
                           tick={<DateTick/>}/>
                    <YAxis dataKey={"humidity"}
                           label={{value: "相对湿度(RH%)", position: "insideLeft", angle: -90, dy: -10}}/>
                    <CartesianGrid stroke="#c3c3c3" strokeDasharray="3 3 "/>
                    <Tooltip content={<HumidityTooltip/>}/>
                </AreaChart>
            </ResponsiveContainer>
        </div>
    </div>
)

const HumidityTooltip = ({label, payload, active}: TooltipProps<number, number>) => {
    if (active && payload && payload.length > 0) {
        const temperature = payload[0].value;
        const date = new Date(parseInt(label));
        return (

            <div className="w-52 h-32 p-4 rounded-md bg-gray-700">
                <h1 className="text-white">监测时间</h1>
                <h1 className="text-white ml-6">{date.toLocaleTimeString()}</h1>
                <h1 className="text-white">相对湿度</h1>
                <h1 className="text-white ml-6"> {temperature}RH%</h1>
            </div>
        )
    } else
        return <></>
}


export const Sensor = observer<SensorProps>(props => {
        const data = props.sensorData;

        return (
            <div className="flex flex-col overflow-clip">
                {/*传感器circular-progressbar*/}
                <SensorCircularProgressbar humidityData={data.humidityNumber} temperatureData={data.temperatureNumber}/>
            {/*    传感器图表*/}
                <SensorChart temperatureList={data.temperatureDataList} humidityList={data.humidityDataList}/>
            </div>
        )
    }
)


const TemperatureIcon = () => (<svg viewBox="0 0 1024 1024" version="1.1"
                                    xmlns="http://www.w3.org/2000/svg" p-id="5513">
    <path
        d="M460.458667 656.213333V374.784c0-25.088-20.650667-45.738667-45.738667-45.738667-25.088 0-45.738667 20.650667-45.738667 45.738667v281.429333c-52.565333 18.261333-91.477333 68.608-91.477333 130.389334 0 75.434667 61.781333 137.216 137.216 137.216s137.216-61.781333 137.216-137.216c0-59.562667-38.912-109.909333-91.477333-130.389334z m91.477333-52.736V145.92c0-75.434667-61.781333-137.216-137.216-137.216s-137.216 61.781333-137.216 137.216v457.557333c-54.954667 41.130667-91.477333 107.52-91.477333 182.954667 0 125.781333 102.912 228.864 228.864 228.864S643.413333 912.384 643.413333 786.602667c0-75.605333-36.522667-141.824-91.477333-183.125334z m-137.386667 366.08c-100.693333 0-183.125333-82.432-183.125333-183.125333 0-68.608 36.693333-125.781333 91.477333-157.866667V145.92c0-50.346667 41.130667-91.477333 91.477334-91.477333s91.477333 41.130667 91.477333 91.477333v482.816c54.954667 32.085333 91.477333 91.477333 91.477333 157.866667 0.341333 100.693333-82.090667 182.954667-182.784 182.954666z m0 0M851.285333 220.842667l8.533334-33.28s-105.984-39.424-146.944 59.562666c-25.429333 105.130667 63.317333 151.552 143.018666 121.344l-5.461333-33.28s-82.773333 30.208-95.914667-41.813333c-12.970667-72.533333 57.514667-90.282667 96.768-72.533333zM651.093333 166.741333c-25.6 0-46.421333 20.821333-46.421333 46.421334s20.821333 46.421333 46.421333 46.421333 46.421333-20.821333 46.421334-46.421333-20.821333-46.421333-46.421334-46.421334z m0 68.778667c-12.458667 0-22.357333-10.069333-22.357333-22.357333 0-12.458667 10.069333-22.357333 22.357333-22.357334 12.458667 0 22.357333 10.069333 22.357334 22.357334 0.170667 12.288-9.898667 22.357333-22.357334 22.357333z"
        fill="#ffffff" p-id="5514"></path>
</svg>)


const HumidityIcon = () => (
    <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
    >
        <path
            d="M427.775886 1024C227.135949 1024 64 867.392049 64 674.784109 64 596.032134 122.111982 459.840176 236.703946 270.080236 320.79992 130.784279 406.143893 11.776316 406.943893 10.560317A25.887992 25.887992 0 0 1 427.935886 0.00032c8.287997 0 16.095995 3.999999 20.799994 10.527997 69.151978 97.59997 134.175958 198.079938 195.039939 301.087906a23.551993 23.551993 0 0 1 1.599999 18.271994 24.351992 24.351992 0 0 1-11.839996 14.079996 25.983992 25.983992 0 0 1-34.079989-8.479998c-65.663979-112.575965-137.119957-217.855932-171.647947-267.487916-30.143991 43.679986-89.151972 130.335959-147.359954 226.815929-108.255966 179.583944-165.567948 310.911903-165.567948 379.967881 0 165.631948 140.479956 300.415906 313.055902 300.415906a325.119898 325.119898 0 0 0 94.911971-14.079995c13.119996-3.871999 26.911992 3.199999 31.39199 15.999995a24.127992 24.127992 0 0 1-15.999995 30.39999c-35.871989 10.879997-73.023977 16.479995-110.399966 16.479995z m-139.071956-165.855948a26.687992 26.687992 0 0 1-12.895996-3.327999c-65.47198-36.479989-105.151967-84.351974-117.919963-142.207956-22.431993-101.599968 48.191985-195.455939 51.295984-199.231937a26.431992 26.431992 0 0 1 35.199989-5.119999c11.295996 7.615998 13.919996 22.495993 5.887998 33.24799-1.056 1.376-60.511981 81.279975-42.559987 161.599949 9.759997 43.903986 41.407987 81.055975 93.983971 110.335966 9.887997 5.471998 14.591995 16.575995 11.615996 26.975991a25.471992 25.471992 0 0 1-24.639992 17.727995z m427.839866 121.279962c-134.367958-0.128-243.423924-103.231968-243.519924-230.463928 0-51.519984 37.823988-139.551956 112.479965-261.439918 54.399983-88.927972 109.663966-164.703949 110.239966-165.503949a25.887992 25.887992 0 0 1 20.799993-10.399996c8.319997 0 16.127995 3.871999 20.831994 10.399996 0.576 0.8 55.679983 76.575976 110.079965 165.535949C922.175732 609.40813 959.99972 697.408102 959.99972 748.992086c-0.128 127.19996-109.055966 230.303928-243.455924 230.399928z m0-600.831812c-68.447979 97.855969-192.92794 291.967909-192.92794 370.495884 0 100.799969 86.399973 182.559943 192.95994 182.559943 106.527967 0 192.79994-81.759974 192.79994-182.559943 0-78.527975-124.479961-272.767915-192.83194-370.495884z m-7.807997 511.77584a29.919991 29.919991 0 0 1-15.295996-4.223999c-50.623984-30.87999-80.639975-69.279978-89.279972-114.079964-14.143996-73.727977 36.223989-136.479957 38.399988-139.135957a29.503991 29.503991 0 0 1 37.855988-4.895998c11.967996 7.871998 14.847995 22.847993 6.335998 33.823989-0.576 0.8-39.007988 49.375985-28.511991 102.207968 6.079998 31.07199 28.287991 58.527982 66.01598 81.599975a23.199993 23.199993 0 0 1 10.719996 27.455991c-3.679999 10.271997-14.271996 17.375995-26.239991 17.279995z"
            p-id="2670" fill="#ffffff"></path>
    </svg>
)
