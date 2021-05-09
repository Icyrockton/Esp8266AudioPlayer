package com.icyrockton.EspAudioPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icyrockton.EspAudioPlayer.viewmodel.SensorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.icyrockton.EspAudioPlayer.data.HumidityItem
import com.icyrockton.EspAudioPlayer.data.TemperatureItem
import com.icyrockton.EspAudioPlayer.ui.theme.Typography
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun Sensor() {
    val sensorViewModel = getViewModel<SensorViewModel>()
    val sensorCoroutineScope = rememberCoroutineScope()
    val sensorText by sensorViewModel.text.observeAsState()
//    刷新数据
    LaunchedEffect(true) {
        sensorCoroutineScope.launch(Dispatchers.IO) {
            while (true) {
                sensorViewModel.refreshSensorData()
                delay(5000L)
            }
        }
    }


    Column(modifier = Modifier.background(Color(0xFFF5F5F5))) {
        val nowTemperature by sensorViewModel.newestTemperature.observeAsState()
        val nowHumidity by sensorViewModel.newestHumidity.observeAsState()
        val humidityList by sensorViewModel.humidity.observeAsState()
        val temperatureList by sensorViewModel.temperature.observeAsState()
        Row(
            modifier = Modifier
                .padding(horizontal = 0.dp, vertical = 10.dp)
                .height(180.dp)
        ) {
            CircularProgressBar(
                title = "温度",
                currentValue = nowTemperature ?: 0.0,
                maxValue = 50.0,
                unit = "℃",
                startColor = Color(0xFFF9D423),
                endColor = Color(0xFFFF4E50),
                backGroundColor = Color(0xFFFFEABA),
                modifier = Modifier.weight(0.5f)
            )
            CircularProgressBar(
                title = "相对湿度",
                currentValue = nowHumidity ?: 0.0,
                maxValue = 100.0,
                unit = "RH%",
                startColor = Color(0xFFa8ff78),
                endColor = Color(0xFF78ffd6),
                backGroundColor = Color(0xFFE3FBD7),
                modifier = Modifier.weight(0.5f)
            )
        }
        TemperatureChart(
            modifier = Modifier.weight(0.5f),
            temperatureList
        )
        HumidityChart(modifier = Modifier.weight(0.5f),  humidityList)
        Spacer(modifier = Modifier.height(56.dp))
    }
}

//barchart
@Composable
fun TemperatureChart(
    modifier: Modifier = Modifier,
    temperatureList: List<TemperatureItem>?
) {
    val colorBrush = remember {
        Brush.linearGradient(listOf(Color(0xFFFF4E50),Color(0xFFF9D423),))
    }
    Card(modifier = Modifier
        .fillMaxWidth()
        .then(modifier)
        .padding(10.dp), elevation = 2.dp) {
        BoxWithConstraints {

            if (temperatureList == null) {
                Row(modifier = Modifier.fillMaxSize(),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                    Text(text = "暂无数据",textAlign = TextAlign.Center)
                }
            } else {
                Text(text = "温度概览",style = Typography.subtitle1,modifier = Modifier.padding(10.dp),color = Color.Gray)
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(temperatureList, key = { item -> item.id }) { temperature ->
//                    每个bar
                        val dateTime = Instant.fromEpochMilliseconds(temperature.date)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        BarChart(temperature.temperature , 50.0,"℃","${dateTime.hour}:${dateTime.minute}:${dateTime.second}",maxHeight,colorBrush = colorBrush)
                    }
                }
            }
        }
    }


}

//barchart
@Composable
fun HumidityChart(
    modifier: Modifier = Modifier,
    humidityList: List<HumidityItem>?
) {
    val colorBrush = remember {
        Brush.linearGradient(listOf(Color(0xFF38ef7d),Color(0xFF11998e),))
    }
    Card(modifier = Modifier
        .fillMaxWidth()
        .then(modifier)
        .padding(10.dp), elevation = 2.dp) {
        BoxWithConstraints {

            if (humidityList == null) {
                Row(modifier = Modifier.fillMaxSize(),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
                    Text(text = "暂无数据",textAlign = TextAlign.Center)
                }
            } else {
                Text(text = "湿度概览",style = Typography.subtitle1,modifier = Modifier.padding(10.dp),color = Color.Gray)
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(humidityList, key = { item -> item.id }) { temperature ->
//                    每个bar
                        val dateTime = Instant.fromEpochMilliseconds(temperature.date)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        BarChart(temperature.humidity , 100.0,"RH%","${dateTime.hour}:${dateTime.minute}:${dateTime.second}",maxHeight,colorBrush = colorBrush)
                    }
                }
            }
        }
    }


}

@Composable
fun BarChart(value:Double,maxValue: Double,unit:String ,label:String,maxHeight:Dp,colorBrush :Brush) {
    val animatedHeight = remember {
        Animatable(0.0f)
    }
    LaunchedEffect(value){
        animatedHeight.animateTo(((value / maxValue) * maxHeight.value).toFloat(),animationSpec = tween(500,0,easing = FastOutSlowInEasing))
    }
    Box(modifier = Modifier
        .width(50.dp)
        .fillMaxHeight()
        .padding(5.dp)
           ) {
        Column {
            Column(modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth(),verticalArrangement = Arrangement.Bottom) {
                Text(text = "${value} ${unit} ",textAlign = TextAlign.Center,style = Typography.caption,modifier = Modifier.fillMaxWidth())
                Box(modifier = Modifier
                    .background(colorBrush, shape = RoundedCornerShape(8.dp, 8.dp))
                    .height(Dp(animatedHeight.value))
                    .fillMaxWidth()) {
                }

            }

            Row(modifier = Modifier
                .weight(0.1f)
                .fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
                Text(text = label,textAlign = TextAlign.Center,fontSize = 9.sp)
            }
        }
    }
}



@Composable
fun CircularProgressBar(
    title: String,
    currentValue: Double, //最小值
    maxValue: Double,//最大值
    unit: String, //单位
    startColor: Color,
    endColor: Color,
    backGroundColor: Color,
    modifier: Modifier = Modifier
) {

    val animatedProgress = remember {
        Animatable(0f)
    }

    val animatedValue = remember {
        Animatable(0f)
    }

    LaunchedEffect(currentValue) { //动画效果
        launch {
            animatedProgress.animateTo(
                currentValue.toFloat(),
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animatedValue.animateTo(
                currentValue.toFloat(),
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .then(modifier)
            .padding(10.dp, vertical = 0.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 10.dp,start = 10.dp),
                    style = Typography.h6
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f), contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val circleRadius = this.size.minDimension / 2.5f
                    val arcWidth = 40.dp.value
                    val arcTopLeft = this.center - Offset(circleRadius, circleRadius) + Offset(
                        arcWidth / 2.0f,
                        arcWidth / 2.0f
                    )//弧形的左上角

                    //背景
                    drawCircle(color = backGroundColor, radius = circleRadius * 1.05f)

                    //圆弧
                    drawArc(
                        brush = Brush.linearGradient(0.0f to startColor, 1.0f to endColor),
                        startAngle = -90f,
                        sweepAngle = (360f * (animatedProgress.value / maxValue)).toFloat(),
                        useCenter = false,
                        style = Stroke(width = arcWidth, cap = StrokeCap.Round),
                        topLeft = arcTopLeft,
                        size = Size(
                            (circleRadius - arcWidth / 2.0f) * 2.0f,
                            (circleRadius - arcWidth / 2.0f) * 2.0f
                        )
                    )


                }
//                浮点数 固定显示一位小数
                Text(
                    text = "${String.format("%.1f", animatedValue.value)} ${unit}",
                    color = Color.Black
                )
            }
        }

    }
}
