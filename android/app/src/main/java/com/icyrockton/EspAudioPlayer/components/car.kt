package com.icyrockton.EspAudioPlayer.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.icyrockton.EspAudioPlayer.R
import com.icyrockton.EspAudioPlayer.viewmodel.CarViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import kotlin.math.roundToInt

@Composable
fun Car() {

    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val carViewModel = getViewModel<CarViewModel>()
    val radius = remember {
        Animatable(20.dp.value)
    }
    val innerRadius = remember {
        Animatable(0.dp.value)
    }
    val rotate = remember {
        Animatable(90.0f)
    }
    val slideIn = remember {
        Animatable(-300.dp.value)
    }
    LaunchedEffect(true){
        slideIn.animateTo(0f,animationSpec = tween(1500))
    }
    LaunchedEffect(true) {
        rotate.animateTo(0f, animationSpec = tween(1500))
    }
    LaunchedEffect(true) {
        radius.animateTo(250.dp.value, animationSpec = tween(800))
    }
    LaunchedEffect(true) {
        innerRadius.animateTo(90.dp.value, animationSpec = tween(800))
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
        ) {
            Box(
                    modifier = Modifier
                            .size(260.dp),
                    contentAlignment = Alignment.Center
            ) {
                Surface(
                        modifier = Modifier
                                .size(Dp(radius.value))
                                .padding(10.dp)
                                .rotate(rotate.value),
                        shape = CircleShape,
                        MaterialTheme.colors.primary,
                        elevation = 10.dp
                ) {

//                图标
                    IconButton(
                            onClick = {

                            }, modifier = Modifier
                            .requiredSize(60.dp)
                            .offset(y = -95.dp), enabled = false
                    ) {
                        Icon(
                                Icons.Default.KeyboardArrowUp,
                                null,
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                        )
                    }

                    IconButton(
                            onClick = {

                            }, modifier = Modifier
                            .requiredSize(60.dp)
                            .offset(x = -95.dp), enabled = false
                    ) {
                        Icon(
                                Icons.Default.KeyboardArrowLeft,
                                null,
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                        )
                    }

                    IconButton(
                            onClick = {

                            }, modifier = Modifier
                            .requiredSize(60.dp)
                            .offset(x = 95.dp), enabled = false
                    ) {
                        Icon(
                                Icons.Default.KeyboardArrowRight,
                                null,
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                        )
                    }
                    IconButton(
                            onClick = {

                            }, modifier = Modifier
                            .requiredSize(60.dp)
                            .offset(y = 95.dp), enabled = false
                    ) {
                        Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                        )
                    }

                }

                Surface(modifier = Modifier
                        .size(Dp(innerRadius.value))
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .pointerInput(Unit) {

                            val circleRadius = 90.dp.toPx() * 90.dp.toPx()

                            forEachGesture {

                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    var change = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                        val newX = offsetX + over.x
                                        val newY = offsetY + over.y
                                        change.consumePositionChange()
                                        if (newX * newX + newY * newY < circleRadius) { //范围以内
                                            offsetX = newX
                                            offsetY = newY
                                        } else {  //范围以外
                                            val angle = Math.atan2(newY.toDouble(), newX.toDouble())
                                            offsetX =
                                                    (90.dp.toPx() * Math.cos(angle)).toFloat()
                                            offsetY =
                                                    (90.dp.toPx() * Math.sin(angle)).toFloat()
                                        }

                                        coroutineScope.launch {
                                            carViewModel.carDirection(offsetX, offsetY)
                                        }
                                    }
                                    while (change != null && change.pressed) {
                                        change = awaitDragOrCancellation(change.id)

                                        if (change != null && !change.pressed) { //抬起动作 按钮回归原位
                                            offsetX = 0f
                                            offsetY = 0f
                                        }

                                        if (change != null && change.pressed) {
                                            val newX = offsetX + change.positionChange().x
                                            val newY = offsetY + change.positionChange().y
                                            change.consumePositionChange()

                                            if (newX * newX + newY * newY < circleRadius) { //范围以内
                                                offsetX = newX
                                                offsetY = newY
                                            } else {  //范围以外
                                                val angle = Math.atan2(newY.toDouble(), newX.toDouble())
                                                offsetX =
                                                        (90.dp.toPx() * Math.cos(angle)).toFloat()
                                                offsetY =
                                                        (90.dp.toPx() * Math.sin(angle)).toFloat()
                                            }

                                            coroutineScope.launch {

                                                carViewModel.carDirection(offsetX, offsetY)
                                            }
                                        }
                                    }
                                }
                            }


                        }, shape = CircleShape, Color.White, elevation = 10.dp
                ) {

                }

            }
        }
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)

        ) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp, start = 20.dp)
                            .height(140.dp).offset(x = Dp(slideIn.value))
            ) {
                Surface(
                        modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                                .fillMaxHeight()
                                .width(80.dp)
                                .clickable {

                                }
                                .pointerInput(true) {
                                    forEachGesture {
                                        awaitPointerEventScope {
                                            awaitFirstDown()
                                            Log.e("compose", "Car: 按下", )
                                            coroutineScope.launch {
                                                carViewModel.carBrake()
                                            }
                                            waitForUpOrCancellation()
                                            Log.e("compose", "Car: 抬起", )

                                        }
                                    }
                                },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colors.primary,
                        elevation = 10.dp
                ) {
                    Icon(
                            painter = painterResource(R.drawable.ic_brake),
                            null,
                            modifier = Modifier
                                    .requiredSize(50.dp)
                                    .rotate(90f),
                            Color(0XFFFE5C5C)
                    )

                }
                Surface(
                        modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                                .fillMaxHeight()
                                .width(80.dp)
                                .clickable {

                                }
                                .pointerInput(true) {
                                    forEachGesture {
                                        awaitPointerEventScope {
                                            awaitFirstDown()
                                            Log.e("compose", "Car: 按下", )
                                            coroutineScope.launch {
                                                carViewModel.carStarAccelerate()
                                            }
                                            waitForUpOrCancellation()
                                            Log.e("compose", "Car: 抬起", )
                                            coroutineScope.launch {
                                                carViewModel.carStarDecelerate()
                                            }
                                        }
                                    }
                                },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colors.primary,
                        elevation = 10.dp
                ) {
                    Icon(
                            painter = painterResource(R.drawable.ic_accelerate),
                            null,
                            modifier = Modifier
                                    .requiredSize(50.dp)
                                    .rotate(90f)
                                    .size(20.dp)
                    )

                }
                Surface(
                        modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                                .fillMaxHeight()
                                .width(80.dp)
                                .clickable {

                                }
                                .pointerInput(true) {
                                    forEachGesture {
                                        awaitPointerEventScope {
                                            awaitFirstDown()
                                            Log.e("compose", "Car: 按下", )
                                            coroutineScope.launch {
                                                carViewModel.carStarAccelerateX2()
                                            }
                                            waitForUpOrCancellation()
                                            coroutineScope.launch {
                                                carViewModel.carStarDecelerate()
                                            }
                                        }
                                    }
                                },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colors.primary,
                        elevation = 10.dp
                ) {
                    Icon(
                            painter = painterResource(R.drawable.ic_accelerate_x2),
                            null,
                            modifier = Modifier
                                    .requiredSize(50.dp)
                                    .rotate(90f)
                                    .size(20.dp)
                    )

                }
            }
        }
    }

}


//val radius = remember {
//    Animatable(20.dp.value)
//}
//LaunchedEffect(true) {
//    radius.animateTo(170.dp.value, animationSpec = tween(800))
//}
//val coroutineScope = rememberCoroutineScope()
//val carViewModel = getViewModel<CarViewModel>()
//
//val rotate = remember {
//    Animatable(90.0f)
//}
//val infiniteTransition = rememberInfiniteTransition()
////    curveA最里面的信号
//val curveA_alpha by infiniteTransition.animateFloat(
//    0.0f, 1.0f, animationSpec = infiniteRepeatable(
//        animation = keyframes {
//            durationMillis = 5000
//            0.0f at 1000 with FastOutSlowInEasing
//            1.0f at 2000
//            1.0f at 4000 with FastOutSlowInEasing
//            0.0f at 5000
//        }, repeatMode = RepeatMode.Restart
//    )
//)
//
//val curveB_alpha by infiniteTransition.animateFloat(
//    0.0f, 1.0f, animationSpec = infiniteRepeatable(
//        animation = keyframes {
//            durationMillis = 5000
//            0.0f at 2000 with FastOutSlowInEasing
//            1.0f at 3000
//            1.0f at 4000 with FastOutSlowInEasing
//            0.0f at 5000
//        }, repeatMode = RepeatMode.Restart
//    )
//)
//
//val curveC_alpha by infiniteTransition.animateFloat(
//    0.0f, 1.0f, animationSpec = infiniteRepeatable(
//        animation = keyframes {
//            durationMillis = 5000
//            0.0f at 3000 with FastOutSlowInEasing
//            1.0f at 4000 with FastOutSlowInEasing
//            0.0f at 5000
//        }, repeatMode = RepeatMode.Restart
//    )
//)
//LaunchedEffect(true) {
//    rotate.animateTo(0f, animationSpec = tween(1000))
//}
//
//Box(
//modifier = Modifier
//.fillMaxSize()
//.background(Color(0xFFF5F5F5)),
//contentAlignment = Alignment.Center
//) {
//
//
//    Surface(
//        modifier = Modifier
//            .size(Dp(radius.value) + 130.dp)
//            .padding(10.dp),
//        color = MaterialTheme.colors.primary,
//        shape = CircleShape,
//        elevation = 10.dp
//    ) {
//        Box(
//            modifier
//            = Modifier
//                .fillMaxSize()
//                .rotate(rotate.value), contentAlignment = Alignment.Center
//        ) {
////                上方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//                        carViewModel.carForward()
//                    }
//                }, modifier = Modifier
//                    .size(60.dp)
//                    .offset(y = -110.dp)
//            ) {
//                Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.fillMaxSize())
//            }
//
//            //                上方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//                        carViewModel.carForwardLeft()
//                    }
//                },
//                modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = -78.dp, y = -78.dp)
//            ) {
//                Icon(
//                    Icons.Default.KeyboardArrowUp,
//                    null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .rotate(-45f)
//                )
//            }
//
//            //                上方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//
//                        carViewModel.carForwardRight()
//                    }
//                },
//                modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = 78.dp, y = -78.dp)
//            ) {
//                Icon(
//                    Icons.Default.KeyboardArrowUp,
//                    null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .rotate(45f)
//                )
//            }
//
//
////                下方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//
//                        carViewModel.carBackward()
//                    }
//                }, modifier = Modifier
//                    .size(60.dp)
//                    .offset(y = 110.dp)
//            ) {
//                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.fillMaxSize())
//            }
//
//            //                下方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//
//                        carViewModel.carBackwardLeft()
//                    }
//                },
//                modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = -78.dp, y = 78.dp)
//            ) {
//                Icon(
//                    Icons.Default.KeyboardArrowDown,
//                    null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .rotate(45f)
//                )
//            }
//
//            //                下方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//
//                        carViewModel.carBackwardRight()
//                    }
//                },
//                modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = 78.dp, y = 78.dp)
//            ) {
//                Icon(
//                    Icons.Default.KeyboardArrowDown,
//                    null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .rotate(-45f)
//                )
//            }
//
////                左方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//
//                        carViewModel.carLeft()
//                    }
//                }, modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = -110.dp)
//            ) {
//                Icon(Icons.Default.KeyboardArrowLeft, null, modifier = Modifier.fillMaxSize())
//            }
//
////                右方向
//            IconButton(
//                onClick = {
//                    coroutineScope.launch {
//                        carViewModel.carRight()
//                    }
//                }, modifier = Modifier
//                    .size(60.dp)
//                    .offset(x = 110.dp)
//            ) {
//                Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.fillMaxSize())
//            }
//
//            Surface(
//                modifier = Modifier.size(Dp(radius.value)),
//                color = Color(0xFFF5F5F5),
//                shape = CircleShape
//            ) {
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .offset(y = -180.dp)
//    ) {
////            无线电的粗细
//        val strokeWidth = with(LocalDensity.current) { 10.dp.toPx() }
//        val strokeColor = MaterialTheme.colors.primary
//        Canvas(
//            modifier = Modifier
//                .fillMaxHeight()
//                .fillMaxWidth()
//        ) {
//            val center = Offset(this.size.width / 2, this.size.height)
//            val a = this.size.minDimension * 1.4f  //长轴
//            val b = this.size.minDimension //短轴
//            var topLeft = Offset((this.size.width - a) / 2.0f, 0f)
//            drawArc(
//                strokeColor,
//                -135f,
//                90f,
//                false,
//                topLeft = topLeft,
//                size = Size(a, b),
//                style = Stroke(width = strokeWidth, cap = StrokeCap.Round), alpha = curveC_alpha
//            )
//
//            topLeft = Offset((this.size.width - a) / 2.0f, 200.0f)
//            drawArc(
//                strokeColor,
//                -115f,
//                50f,
//                false,
//                topLeft = topLeft,
//                size = Size(a, b),
//                style = Stroke(width = strokeWidth, cap = StrokeCap.Round), alpha = curveB_alpha
//            )
//
//            topLeft = Offset((this.size.width - a) / 2.0f, 400.0f)
//            drawArc(
//                strokeColor,
//                -100f,
//                20f,
//                false,
//                topLeft = topLeft,
//                size = Size(a, b),
//                style = Stroke(width = strokeWidth, cap = StrokeCap.Round), alpha = curveA_alpha
//            )
//
//        }
//
//    }
//
//
//}