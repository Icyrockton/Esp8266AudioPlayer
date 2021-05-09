package com.icyrockton.EspAudioPlayer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.icyrockton.EspAudioPlayer.ui.theme.ESPAudioPlayerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.icyrockton.EspAudioPlayer.components.Car
import com.icyrockton.EspAudioPlayer.components.Music
import com.icyrockton.EspAudioPlayer.components.Sensor
import kotlin.math.log

class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ESPAudioPlayerTheme {
                mainContent()
            }
        }
    }
}


@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun mainContent() {
    var selectedItem by remember {
        mutableStateOf(0)
    }
    val scaffoldState = rememberScaffoldState()

    val navController = rememberNavController()
    Scaffold(scaffoldState = scaffoldState,
//        顶部导航栏
        topBar = {
            TopAppBar {
                Icon(painter = painterResource(id = R.drawable.ic_cpu)  , contentDescription = null,modifier = Modifier.padding(10.dp))

                Text(text = "ESP8266 AudioPlayer")
            }
        },
        bottomBar = {
//                    底部导航栏
        BottomNavigation {
            screens.forEachIndexed { index, screen ->
//                导航栏子项
                BottomNavigationItem(
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        navController.navigate(screen.route) {
                            popUpTo = navController.graph.startDestination //pop到startDestination
                            launchSingleTop = true
                        }
                    },
                    alwaysShowLabel = true,
                    label = {
                        Text(text = screen.description)
                    },
                    icon = {
                        Icon(
                            painterResource(id = screen.iconResourceID),
                            contentDescription = screen.toString()
                        )
                    }
                )
            }
        }
    }) {
        NavHost(navController = navController, startDestination = "car") {
            composable(Screen.Sensor.route) { Sensor() }
            composable(Screen.Music.route) { Music() }
            composable(Screen.Car.route) { Car() }
        }
    }
}