import React, {KeyboardEvent, useState} from "react"
import {observer} from "mobx-react-lite";
import useMusicData, {MusicData, MusicState, SearchSong} from "../store/MusicData";
import Slider, {Range} from 'rc-slider';
import 'rc-slider/assets/index.css';

type MusicProps = {
    musicData: MusicData
}
type MusicResultProps = {
    loading: boolean,
    songs: SearchSong[] | null
    searchStr: string
    currentMusicID: number
    currentMusicState: MusicState
}

const SearchResult = ({loading, songs, searchStr, currentMusicID, currentMusicState}: MusicResultProps) => {

    if (loading) {
        return (<div className="flex flex-col  items-center mt-6 ">
            <svg xmlns="http://www.w3.org/2000/svg" className="animate-spin" width="36" height="36" viewBox="0 0 24 24"
                 fill="none" stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38"/>
            </svg>
            <h1 className="mt-6 text-xl font-bold mt-10 text-center">努力加载中...</h1>
        </div>)
    } else if (songs == null) {
        return <div className="w-full ">
            <h1 className="text-xl font-bold mt-10 text-center">请在上方搜索歌曲</h1>
        </div>
    } else {
        if (searchStr == "") {
            return <h1 className="text-xl font-bold mt-10 text-center">请在上方搜索歌曲</h1>
        } else {
            const SongList = songs!!.map(song => {
                    const svg = () => {
                        if (song.id == currentMusicID && currentMusicState != MusicState.init) {
                            if (currentMusicState == MusicState.loading) {
                                return (
                                    <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
                                         className="animate-spin fill-current text-green-400"
                                         p-id="2868">
                                        <path
                                            d="M911 548c-19.9 0-36-16.1-36-36 0-97-37.8-188.1-106.3-256.7S609 149 512 149c-19.9 0-36-16.1-36-36s16.1-36 36-36c58.7 0 115.7 11.5 169.3 34.2 51.8 21.9 98.3 53.3 138.3 93.2 39.9 39.9 71.3 86.5 93.2 138.3C935.5 396.3 947 453.3 947 512c0 19.9-16.1 36-36 36z"
                                            p-id="2869"></path>
                                    </svg>
                                )
                            } else if (currentMusicState == MusicState.playing) {
                                return (<svg xmlns="http://www.w3.org/2000/svg"
                                             className="stroke-current text-purple-500 transform hover:scale-125 ease-out transition cursor-pointer "
                                             viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2"
                                             strokeLinecap="round" strokeLinejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="10" y1="15" x2="10" y2="9"></line>
                                        <line x1="14" y1="15" x2="14" y2="9"></line>
                                    </svg>
                                )
                            } else if (currentMusicState == MusicState.error) {
                                return (
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                         className="stroke-current text-red-500 transform hover:scale-125 ease-out transition cursor-pointer "
                                         viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2"
                                         strokeLinecap="round" strokeLinejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="15" y1="9" x2="9" y2="15"></line>
                                        <line x1="9" y1="9" x2="15" y2="15"></line>
                                    </svg>
                                )
                            }
                        } else {
                            return (
                                <svg xmlns="http://www.w3.org/2000/svg"
                                     className="stroke-current transform hover:scale-125 ease-out transition cursor-pointer hover:text-purple-500 "
                                     onClick={() => useMusicData.playMusic(song.id)}
                                     viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2" strokeLinecap="round"
                                     strokeLinejoin="round">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <polygon points="10 8 16 12 10 16 10 8"></polygon>
                                </svg>
                            )
                        }
                    }
                    return (
                        <div className="w-full h-32 bg-white mb-3 flex rounded-2xl shadow-md overflow-hidden" key={song.id}>
                            <img src={song.al.picUrl} className="rounded-2xl h-32 w-32 object-fill"/>
                            <div className="p-4 flex flex-col justify-start flex-1 ">
                                <h1 className={` overflow-ellipsis ${song.name.length > 10 ? "text--md" : "text-lg"}  `}>{song.name}</h1>
                                <h1 className="mt-2">{song.ar[0].name}</h1>
                                {
                                    song.alia.length > 0 ?
                                        <h1 className={`text-gray-500 mt-2 ${song.alia[0].length > 15 ? "text-base" : "text-sm"}`}>{song.alia[0]}</h1> : <></>
                                }
                            </div>
                            <div className="w-24 p-5 self-center  ">
                                {svg()}
                            </div>
                        </div>

                    )
                }
            )
            return (
                <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 w-full gap-6 ">
                    {SongList}
                </div>
            )

        }
    }
    return <></>
}

export const Music = observer((props: MusicProps) => {
        const data = props.musicData;
        const musicDetail = data.playingMusicDetail;
        const [searchStr, setSearchStr] = useState("");

        const searchChange = (event: { target: { value: any; }; }) => {
            setSearchStr(event.target.value)
            if (searchStr == "") {
                useMusicData.clearSearchResult()
            }
        }
        const search = (event: KeyboardEvent) => {
            if (event.key == "Enter") {
                console.log('搜索中')
                useMusicData.searchSong(searchStr)
            }
        }


        return (
            <>
                <div className="flex flex-col w-full min-h-full p-10  items-center -mb-32  ">
                    <div className="w-full bg-white h-20  rounded-xl border-1 shadow-lg mb-10">
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-14 w-14 absolute my-3 ml-4" viewBox="0 0 24 24"
                             fill="none" stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input type={"text"} className="w-full h-full  pl-24 bg-transparent outline-none text-2xl"
                               placeholder={"查找音乐"} value={searchStr} onChange={searchChange} onKeyPress={search}/>
                    </div>

                    <div className="w-full flex-auto  ">
                        <SearchResult searchStr={searchStr} loading={data.loading} songs={data.searchResult}
                                      currentMusicID={data.playingMusicID} currentMusicState={data.playingMusicState}/>
                    </div>


                </div>
                {
                    musicDetail ?
                        <div className="bottom-0 w-full h-36  sticky  rounded-t-2xl px-4 sm:px-8 lg:px-16 flex-shrink   ">
                            <div
                                className="w-full opacity-90 items-center h-full bg-white rounded-t-2xl shadow-2xl flex p-5 pl-10 border-t-2 border-l-2 border-r-2 border-purple-500 ">
                                <img src={musicDetail.picUrl}
                                     className={`border-2 border-gray-300  shadow-2xl rounded-full     h-full ${musicDetail.playing ? "animate-spin-slow" : ""} `}/>
                                <div className="flex-col flex-1   ">
                                    <h1 className="text-center text-2xl font-bold">{musicDetail.name}</h1>
                                    <h1 className="text-center text-lg font-semibold mt-4">{musicDetail.artist}</h1>
                                    <h1 className="text-center text-lg font-semibold mt-2">{musicDetail.cd}</h1>
                                </div>
                                {
                                    //暂停音乐
                                    musicDetail.playing ?
                                        <svg xmlns="http://www.w3.org/2000/svg "
                                             onClick={() => useMusicData.pauseMusic()}
                                             className="p-2 w-20 stroke-current    text-purple-500 transform mx-auto block hover:scale-125 ease-out transition cursor-pointer "
                                             viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2"
                                             strokeLinecap="round" strokeLinejoin="round">
                                            <circle cx="12" cy="12" r="10"></circle>
                                            <line x1="10" y1="15" x2="10" y2="9"></line>
                                            <line x1="14" y1="15" x2="14" y2="9"></line>
                                        </svg>
                                        :
                                        //恢复音乐
                                        <svg xmlns="http://www.w3.org/2000/svg"
                                             className="p-2 w-20 stroke-current     transform mx-auto block hover:scale-125 ease-out transition cursor-pointer "
                                             onClick={() => useMusicData.resumeMusic()}
                                             viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2"
                                             strokeLinecap="round"
                                             strokeLinejoin="round">
                                            <circle cx="12" cy="12" r="10"></circle>
                                            <polygon points="10 8 16 12 10 16 10 8"></polygon>
                                        </svg>
                                }
                                <svg xmlns="http://www.w3.org/2000/svg"
                                     onClick={() => useMusicData.cancelMusic()}
                                     className="w-16 p-2 stroke-current  text-red-500 transform hover:scale-125 ease-out transition cursor-pointer "
                                     viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2" strokeLinecap="round"
                                     strokeLinejoin="round">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <line x1="15" y1="9" x2="9" y2="15"></line>
                                    <line x1="9" y1="9" x2="15" y2="15"></line>
                                </svg>
                                <Slider defaultValue={10} min={0} max={10} vertical
                                        trackStyle={{backgroundColor: "#8B5CF6"}}
                                        handleStyle={{borderColor: "#8B5CF6"}}
                                        onAfterChange={(vol) => useMusicData.setVolume(vol)}
                                        activeDotStyle={{background: "purple"}}/>
                            </div>
                        </div>
                        :
                        <></>
                }
            </>
        )
    }
)


