package com.icyrockton.EspAudioPlayer.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.icyrockton.EspAudioPlayer.viewmodel.CarViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun Car() {

    val radius = remember {
        Animatable(20.dp.value)
    }
    LaunchedEffect(true) {
        radius.animateTo(170.dp.value, animationSpec = tween(800))
    }
    val carViewModel = getViewModel<CarViewModel>()

    val rotate = remember {
        Animatable(90.0f)
    }
    val infiniteTransition = rememberInfiniteTransition()
//    curveA最里面的信号
    val curveA_alpha by infiniteTransition.animateFloat(
        0.0f, 1.0f, animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0.0f at 1000 with FastOutSlowInEasing
                1.0f at 2000
                1.0f at 4000 with FastOutSlowInEasing
                0.0f at 5000
            }, repeatMode = RepeatMode.Restart
        )
    )

    val curveB_alpha by infiniteTransition.animateFloat(
        0.0f, 1.0f, animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0.0f at 2000 with FastOutSlowInEasing
                1.0f at 3000
                1.0f at 4000 with FastOutSlowInEasing
                0.0f at 5000
            }, repeatMode = RepeatMode.Restart
        )
    )

    val curveC_alpha by infiniteTransition.animateFloat(
        0.0f, 1.0f, animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0.0f at 3000 with FastOutSlowInEasing
                1.0f at 4000 with FastOutSlowInEasing
                0.0f at 5000
            }, repeatMode = RepeatMode.Restart
        )
    )
    LaunchedEffect(true) {
        rotate.animateTo(0f, animationSpec = tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {


        Surface(
            modifier = Modifier
                .size(Dp(radius.value) + 130.dp)
                .padding(10.dp),
            color = MaterialTheme.colors.primary,
            shape = CircleShape,
            elevation = 10.dp
        ) {
            Box(
                modifier
                = Modifier
                    .fillMaxSize()
                    .rotate(rotate.value), contentAlignment = Alignment.Center
            ) {
//                上方向
                IconButton(onClick = {
                    carViewModel.carForward()
                }, modifier = Modifier
                    .size(60.dp)
                    .offset(y = -110.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.fillMaxSize())
                }

                //                上方向
                IconButton(
                    onClick = {
                              carViewModel.carForwardLeft()
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = -78.dp, y = -78.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-45f)
                    )
                }

                //                上方向
                IconButton(
                    onClick = {
                        carViewModel.carForwardRight()

                    },
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 78.dp, y = -78.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(45f)
                    )
                }


//                下方向
                IconButton(onClick = {
                    carViewModel.carBackward()
                }, modifier = Modifier
                    .size(60.dp)
                    .offset(y = 110.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.fillMaxSize())
                }

                //                下方向
                IconButton(
                    onClick = {
                        carViewModel.carBackwardLeft()
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = -78.dp, y = 78.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(45f)
                    )
                }

                //                下方向
                IconButton(
                    onClick = {
                        carViewModel.carBackwardRight()
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 78.dp, y = 78.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-45f)
                    )
                }

//                左方向
                IconButton(onClick = {
                    carViewModel.carLeft()
                }, modifier = Modifier
                    .size(60.dp)
                    .offset(x = -110.dp)) {
                    Icon(Icons.Default.KeyboardArrowLeft, null, modifier = Modifier.fillMaxSize())
                }

//                右方向
                IconButton(onClick = {
                    carViewModel.carRight()
                }, modifier = Modifier
                    .size(60.dp)
                    .offset(x = 110.dp)) {
                    Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.fillMaxSize())
                }

                Surface(
                    modifier = Modifier.size(Dp(radius.value)),
                    color = Color(0xFFF5F5F5),
                    shape = CircleShape
                ) {
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .offset(y = -180.dp)) {
//            无线电的粗细
            val strokeWidth = with(LocalDensity.current) { 10.dp.toPx() }
            val strokeColor = MaterialTheme.colors.primary
            Canvas(modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()) {
                val center = Offset(this.size.width / 2, this.size.height)
                val a = this.size.minDimension * 1.4f  //长轴
                val b = this.size.minDimension //短轴
                var topLeft = Offset((this.size.width - a) / 2.0f, 0f)
                drawArc(
                    strokeColor,
                    -135f,
                    90f,
                    false,
                    topLeft = topLeft,
                    size = Size(a, b),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),alpha = curveC_alpha
                )

                topLeft = Offset((this.size.width - a) / 2.0f, 200.0f)
                drawArc(
                    strokeColor,
                    -115f,
                    50f,
                    false,
                    topLeft = topLeft,
                    size = Size(a, b),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round) , alpha = curveB_alpha
                )

                topLeft = Offset((this.size.width - a) / 2.0f, 400.0f)
                drawArc(
                    strokeColor,
                    -100f,
                    20f,
                    false,
                    topLeft = topLeft,
                    size = Size(a, b),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round) , alpha = curveA_alpha
                )

            }

        }


    }
}