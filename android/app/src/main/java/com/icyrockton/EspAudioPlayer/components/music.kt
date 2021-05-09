package com.icyrockton.EspAudioPlayer.components

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import coil.transform.RoundedCornersTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.icyrockton.EspAudioPlayer.R
import com.icyrockton.EspAudioPlayer.data.SearchSong
import com.icyrockton.EspAudioPlayer.ui.theme.Typography
import com.icyrockton.EspAudioPlayer.viewmodel.MusicViewModel
import com.icyrockton.EspAudioPlayer.viewmodel.SearchState
import org.koin.androidx.compose.getViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun Music() {
    val musicViewModel = getViewModel<MusicViewModel>()
    val searchSongList by musicViewModel.musicList.observeAsState()
    val searchState by musicViewModel.searchState.observeAsState()
    val playToolBarVisibility by musicViewModel.playToolBar.observeAsState()
    val playToolBarInfo by musicViewModel.playInfo.observeAsState()
    val playing by musicViewModel.playing.observeAsState()
    LaunchedEffect(true) {
        musicViewModel.hotMusic()
    }
    DisposableEffect(true){
        onDispose {
            musicViewModel.clearData()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF5F5F5)
//            .background(
//                Color(0xFFFF0000)

            )
    ) {
        var searchString by remember {
            mutableStateOf("")
        }
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            value = searchString,
            onValueChange = { str -> searchString = str },
            label = {
                Text(
                    text = "搜索音乐"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            keyboardOptions = KeyboardOptions(
                KeyboardCapitalization.None,
                true,
                KeyboardType.Text,
                ImeAction.Search
            ),
            maxLines = 1, singleLine = true,
            keyboardActions = KeyboardActions(onSearch = {
                musicViewModel.searchMusic(searchString)
//                搜索音乐
                keyboardController?.hide()
            })
        )
        when (searchState) {
            SearchState.initial -> {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth()
                ) {
                    Text(text = "上方搜索歌曲")
                }
            }
            SearchState.loading -> {
                val coroutineScope = rememberCoroutineScope()
                var loadingText by remember {
                    mutableStateOf("加载中.")
                }
                LaunchedEffect(true) {
                    coroutineScope.launch {
                        while (true) {
                            loadingText = "加载中."
                            delay(500L)
                            loadingText = "加载中.."
                            delay(500L)
                            loadingText = "加载中..."
                            delay(500L)

                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    LoadingIcon(modifier = Modifier.size(60.dp))
                    Text(text = loadingText, modifier = Modifier.padding(top = 20.dp))
                }
            }
            SearchState.finished -> {
                if (searchSongList == null) {

                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1.0f),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        items(searchSongList!!, key = { it.id }) {
                            MusicItem(song = it) {
                                musicViewModel.playMusic(it.id)
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = (playToolBarVisibility ?: false),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            enter = slideInVertically(
                initialOffsetY = { 270.dp.value.toInt() }, animationSpec = tween(800)
            ) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),

            ) {
            if (playToolBarInfo != null) {
//           底部播放栏信息


                var volumeTabVisibility by remember {
                    mutableStateOf(false)
                }
                val swipeableState = rememberSwipeableState(0)
                LaunchedEffect(swipeableState.currentValue) {
                    musicViewModel.setVolume(abs(swipeableState.currentValue - 10))
                }
                val sizePx = with(LocalDensity.current) { 3.dp.toPx() }
                val anchors = mapOf(
                    0f to 0,
                    sizePx to 1,
                    sizePx * 2.0f to 2,
                    sizePx * 3.0f to 3,
                    sizePx * 4.0f to 4,
                    sizePx * 5.0f to 5,
                    sizePx * 6.0f to 6,
                    sizePx * 7.0f to 7,
                    sizePx * 8.0f to 8,
                    sizePx * 9.0f to 9,
                    sizePx * 10.0f to 10
                ) // Maps anchor points (in px) to states

                Surface(
                    modifier = Modifier
                        .fillMaxSize(), elevation = 10.dp, color = MaterialTheme.colors.primary
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE9E9E9))
                        )
                        Row(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = rememberCoilPainter(request = playToolBarInfo!!.picUrl,
                                    requestBuilder = {
                                        transformations(RoundedCornersTransformation(20.dp.value))
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(60.dp)

                            )
                            Column(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .fillMaxHeight()
                                    .padding(start = 10.dp)
                            ) {
                                Text(
                                    text = playToolBarInfo!!.name,
                                    style = Typography.subtitle1,
                                    modifier = Modifier.padding(top = 5.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${playToolBarInfo!!.artist}-${playToolBarInfo!!.cd}",
                                    style = Typography.caption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
                            ) {
//                                音量调整弹出界面
                                if (volumeTabVisibility) {
                                    Popup(
                                        alignment = Alignment.TopStart, offset = IntOffset(
                                            0,
                                            (-(510.dp.value)).toInt()
                                        ), onDismissRequest = { volumeTabVisibility = false }
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(130.dp),
                                            elevation = 4.dp, shape = RectangleShape,
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(0.dp, 5.dp)
                                                        .swipeable(
                                                            state = swipeableState,
                                                            orientation = Orientation.Vertical,
                                                            anchors = anchors,
                                                            enabled = true,
                                                            thresholds = { _, _ ->
                                                                FractionalThreshold(
                                                                    1.0f
                                                                )
                                                            },
                                                        )

                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .fillMaxHeight()
                                                            .padding(horizontal = 19.dp),
                                                        color = Color(0xFFC39EF8)
                                                    ) {

                                                    }
                                                    Surface(
                                                        modifier = Modifier
                                                            .width(10.dp)
                                                            .height(10.dp)
                                                            .offset(
                                                                15.dp,
                                                                swipeableState.offset.value.dp
                                                            ),
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colors.primary
                                                    ) {

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { volumeTabVisibility = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_volume),
                                        contentDescription = "volume",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))

                                IconButton(
                                    onClick = {
                                        if (playing == null || playing == false) {
                                            musicViewModel.musicResume()

                                        } else {
                                            musicViewModel.musicPause()

                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    if (playing == null || playing == false) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = "play",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_pause),
                                            contentDescription = "pause",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(
                                    onClick = { musicViewModel.musicCancel() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_cancel),
                                        contentDescription = "cancel",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                            }
                        }
                    }
                }


            }
        }
        Spacer(modifier = Modifier.height(56.dp))

    }
}


@Composable
fun LoadingIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val startAngle_A by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (360.0f * 3.0 - 10.0f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0.0f at 0 with LinearEasing  //0-500MS 是FastOutSlowInEasing

                (360.0f * 3.0 - 10.0f).toFloat() at 4000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val sweepAngle_A by infiniteTransition.animateFloat(
        initialValue = 10.0f,
        targetValue = 10.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                10.0f at 0 with LinearEasing  //0-500MS 是FastOutSlowInEasing
                250.0f at 1500   //0-500MS 是FastOutSlowInEasing
                250.0f at 2500 with LinearEasing   //0-500MS 是FastOutSlowInEasing
                10.0f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val startAngle_B by infiniteTransition.animateFloat(
        initialValue = 0f + 180.0f,
        targetValue = (360.0f * 3.0 - 10.0f + 180.0f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                180.0f at 0 with LinearEasing  //0-500MS 是FastOutSlowInEasing

                (360.0f * 3.0 - 10.0f + 180.0f).toFloat() at 4000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val sweepAngle_B by infiniteTransition.animateFloat(
        initialValue = 10.0f,
        targetValue = 10.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                10.0f at 0 with LinearEasing  //0-500MS 是FastOutSlowInEasing
                250.0f at 1500   //0-500MS 是FastOutSlowInEasing
                250.0f at 2500 with LinearEasing   //0-500MS 是FastOutSlowInEasing
                10.0f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF00FF90),
                startAngle = startAngle_A,
                sweepAngle = sweepAngle_A,
                useCenter = false,
                style = Stroke(width = 40.dp.value, cap = StrokeCap.Round)
            )

            drawArc(
                color = Color(0xFF6600FF),
                startAngle = startAngle_B,
                sweepAngle = sweepAngle_B,
                useCenter = false,
                style = Stroke(width = 40.dp.value, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MusicItem(song: SearchSong, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 3.dp, vertical = 5.dp)
            .fillMaxWidth()
            .height(100.dp), shape = RoundedCornerShape(5.dp), elevation = 0.dp
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberCoilPainter(song.al.picUrl, requestBuilder = {
                    transformations(RoundedCornersTransformation(80.dp.value))
                }, fadeIn = true, previewPlaceholder = R.drawable.ic_netease),
                null, modifier = Modifier.padding(10.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 15.dp)
                    .weight(1.0f)
            ) {
                Text(text = song.name, style = Typography.subtitle1)
                Text(
                    text = "${song.ar[0].name}-${song.al.name}",
                    style = Typography.caption,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(40.dp)
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}