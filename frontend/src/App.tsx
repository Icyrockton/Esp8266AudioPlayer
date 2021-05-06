import React, {useState} from 'react'
import logo from './logo.svg'
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link,
    Redirect,
    useHistory
} from "react-router-dom";
import {Sensor} from "./components/Sensor";
import {Music} from "./components/Music";
import useSensorData from "./store/SensorData";
import useMusicData from "./store/MusicData";

function App() {
    const [nav, setNav] = useState('sensor');
    const history = useHistory();
    const goToNav = (path: string) => {
        history.push(`/${path}`);
        setNav(path)
    }
    return (
        <div className="w-screen h-screen bg-gray-100 flex flex-col    ">
            <header
                className="w-full h-16 bg-white rounded-b-md  h-16 p-4 flex flex-wrap justify-start content-center items-center ">
                <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none"
                     stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="4" y="4" width="16" height="16" rx="2" ry="2"></rect>
                    <rect x="9" y="9" width="6" height="6"></rect>
                    <line x1="9" y1="1" x2="9" y2="4"></line>
                    <line x1="15" y1="1" x2="15" y2="4"></line>
                    <line x1="9" y1="20" x2="9" y2="23"></line>
                    <line x1="15" y1="20" x2="15" y2="23"></line>
                    <line x1="20" y1="9" x2="23" y2="9"></line>
                    <line x1="20" y1="14" x2="23" y2="14"></line>
                    <line x1="1" y1="9" x2="4" y2="9"></line>
                    <line x1="1" y1="14" x2="4" y2="14"></line>
                </svg>
                <div className="ml-4 font-semibold">
                    <h1 className="text-xl">ESP网络音乐播放器</h1>
                </div>
            </header>
            <div className="flex-1 min-h-0   flex my-2 ">
                {/*侧边栏*/}
                <aside className="w-1/6 md:w-72 h-full bg-white p-4 rounded-r-lg hidden md:block     ">
                    <ul className="flex flex-col ">
                        {/*传感器数据*/}
                        <li className={`flex justify-start items-center mb-6 cursor-pointer   rounded-xl p-2 ${nav == "sensor" ? 'bg-purple-500 text-white' : 'hover:text-purple-400'} `}
                            onClick={() => goToNav("sensor")}>
                            <svg className="stroke-current " width="40" height="40" viewBox="0 0 24 24" fill="none"
                                 stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M12 20v-6M6 20V10M18 20V4"/>
                            </svg>
                            <span className="ml-8 flex-1 text-base  font-bold ">传感器数据</span>
                        </li>

                        {/*音乐播放*/}
                        <li className={`flex justify-start items-center mb-6 cursor-pointer   rounded-xl p-2 ${nav == "music" ? 'bg-purple-500 text-white' : 'hover:text-purple-400'} `}
                            onClick={() => goToNav("music")}>
                            <svg className="stroke-current  " width="40" height="40" viewBox="0 0 24 24" fill="none"
                                 stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="5.5" cy="17.5" r="2.5"/>
                                <circle cx="17.5" cy="15.5" r="2.5"/>
                                <path d="M8 17V5l12-2v12"/>
                            </svg>
                            <span className="ml-8 flex-1 text-base font-bold ">音乐播放</span>
                        </li>

                    </ul>
                </aside>
                {/*内容区域*/}
                <main className="flex-1 h-full overflow-auto relative  ">
                    <Switch>
                        <Route path={"/sensor"}>
                            <Sensor sensorData={useSensorData}/>
                        </Route>

                        <Route path={"/music"}>

                            <Music musicData={useMusicData}/>
                        </Route>
                        <Route exact path={"/"}>
                            <Redirect to={"/sensor"}/>
                        </Route>
                    </Switch>
                </main>
            </div>

        </div>
    )
}

export default App
