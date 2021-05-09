package com.icyrockton.EspAudioPlayer

import androidx.annotation.DrawableRes
import com.icyrockton.EspAudioPlayer.R


sealed class Screen(val route:String, @DrawableRes val iconResourceID:Int,val description:String){
    object Sensor: Screen("sensor", R.drawable.ic_sensor,"传感器")
    object Music: Screen("music", R.drawable.ic_music,"音乐")
    object Car: Screen("car", R.drawable.ic_car,"遥控小车")

    override fun toString(): String {
        return route
    }
}



val  screens= listOf(
    Screen.Sensor,
    Screen.Music,
    Screen.Car,
)