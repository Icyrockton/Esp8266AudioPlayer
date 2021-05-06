import {makeAutoObservable, runInAction} from "mobx";
import {EspApi} from "../network/Api";
export type  SearchSong = {
    name: string //歌曲名称
    id: number //ID编号
    ar: {
        id: number  //歌手ID
        name: string  //歌手名称
    }[],
    al: {
        id: number //专辑ID
        picUrl: string //歌曲图片
    },
    alia: string[] //歌曲别名
}
export type SongDetail = {
    id: number,
    url: string | null,  //最终的播放视频URL 可能为null
    type: string,
    size: number
    code :number // -110 无权限    200有权限
}
export enum MusicState {
    init,
    loading,
    playing,
    error
}
export type MusicDetail ={
    id:number //id
    name: string //歌曲名称
    artist :string //作者
    cd :string //专辑
    picUrl: string //专辑图片
    playing : boolean
}
export class MusicData{
    searchResult : SearchSong[] | null = null
    loading : boolean = false
    playingMusicID : number = -1  //正在播放的音乐ID
    playingMusicState : MusicState  =  MusicState.loading //播放状态
    playingMusicDetail : MusicDetail | null = null
    constructor() {
        makeAutoObservable(this)
    }

    async searchSong  (str:string){
        runInAction(()=>{
            this.loading = true
        })
        const songs = await  EspApi.searchSong(str);
        runInAction(()=>{
            this.searchResult = songs
            this.loading =false
        })
    }

    clearSearchResult (){
        this.searchResult = null
    }

    //播放歌曲
    async playMusic(songID: number) {
        runInAction(()=>{
            this.playingMusicState = MusicState.loading
            this.playingMusicID = songID
        })
        const response = await EspApi.playMusic(songID);
        const detail   = (await EspApi.musicDetail(songID)).data as MusicDetail;
        detail.playing = true
        const data = response.data as SongDetail; //数据
        //如果url是null 则播放失败 否则开始播放
        runInAction(()=>{
            console.log(data)
            if (data.url == null){
                this.playingMusicState = MusicState.error
            }
            else{
                this.playingMusicState = MusicState.playing
                this.playingMusicDetail = detail
            }
        })
    }

    async pauseMusic() {
        runInAction(()=>{
            if (this.playingMusicDetail){
                this.playingMusicDetail.playing = false
            }
        })
       await EspApi.pauseMusic()
    }

    async resumeMusic(){
        runInAction(()=>{
            if (this.playingMusicDetail){
                this.playingMusicDetail.playing = true
            }
        })
        await EspApi.resumeMusic()

    }

    async cancelMusic() {
        runInAction(()=>{
            this.playingMusicDetail = null //恢复为null
        })
        EspApi.cancelMusic()
        runInAction(()=>{
            this.playingMusicID=-1
            this.playingMusicState = MusicState.loading
        })
    }

    async setVolume(vol: number) {
       await EspApi.setVolume(vol)

    }
}

const useMusicData = new MusicData()
export default useMusicData
