package org.skyfaced.circlechart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import org.skyfaced.circlechart.ui.CircleChart
import org.skyfaced.circlechart.ui.theme.CircleChartTheme
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random.Default.nextFloat

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalFoundationApi::class,
        androidx.constraintlayout.compose.ExperimentalMotionApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        com.google.accompanist.pager.ExperimentalPagerApi::class,
        dev.chrisbanes.snapper.ExperimentalSnapperApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircleChartTheme {
//                Asd(viewModel)
                HorizontalPagerScreen(viewModel)
            }
        }
    }
}

val String.Companion.Empty get() = ""

val Color.Companion.Random get(): Color = Color((nextFloat() * 16777215).toInt() or (0xFF shl 24))

val Color.Companion.Rainbow by lazy<List<Color>>(mode = LazyThreadSafetyMode.NONE) {
    val colors = mutableListOf<Color>()

    val frequency = 0.2f
    for (i in 0 until 32) {
        val red = sin(frequency * i + 0) * 127 + 128
        val green = sin(frequency * i + 2) * 127 + 128
        val blue = sin(frequency * i + 4) * 127 + 128
        colors.add(Color(red.roundToInt(), green.roundToInt(), blue.roundToInt()))
    }

    colors
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column {
        CircleChart(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 48.dp),
            viewSize = 150,
            maxProgress = state.maxProgress,
            currentProgress = state.folders[pagerState.currentPage].size,
//            useRainbow = true,
            circleColor = Color.Random
        )

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
            },
        ) {
            state.folders.forEachIndexed { idx, it ->
                Tab(
                    modifier = Modifier.heightIn(min = 50.dp),
                    selected = idx == pagerState.currentPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(idx) } }
                ) {
                    Text(it.name)
                }
            }
        }

        HorizontalPager(
            count = state.folders.size,
            state = pagerState,
            key = { state.folders[it].name }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = calculateCurrentOffsetForPage(it).absoluteValue
                        lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f)).also { scale ->
                            scaleX = scale
                            scaleY = scale
                        }

                        alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    },
                contentPadding = PaddingValues(24.dp)
            ) {
                items(state.folders[it].files) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            text = it.name
                        )
                    }
                }
            }
        }
    }
}

fun lerp(start: Float, stop: Float, fraction: Float) = (start * (1 - fraction) + stop * fraction)

@OptIn(ExperimentalFoundationApi::class,
    androidx.constraintlayout.compose.ExperimentalMotionApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
    com.google.accompanist.pager.ExperimentalPagerApi::class,
    dev.chrisbanes.snapper.ExperimentalSnapperApi::class)
@Composable
fun Asd(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    val scene = LocalContext.current.resources
        .openRawResource(R.raw.motion)
        .readBytes()
        .decodeToString()
    val swipingState = rememberSwipeableState(initialValue = SwipingStates.EXPANDED)
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                return if (delta < 0) {
                    swipingState.performDrag(delta).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                return swipingState.performDrag(delta).toOffset()
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                swipingState.performFling(velocity = available.y)
                return super.onPostFling(consumed, available)
            }

            private fun Float.toOffset() = Offset(0f, this)
        }
    }

    Box(
        modifier = Modifier
            .swipeable(
                state = swipingState,
                anchors = mapOf(
                    0f to SwipingStates.COLLAPSED,
                    400f to SwipingStates.EXPANDED
                ),
                orientation = Orientation.Vertical,
            )
            .nestedScroll(connection)
    ) {

        MotionLayout(
            motionScene = MotionScene(scene),
            progress = if (swipingState.progress.to == SwipingStates.COLLAPSED) swipingState.progress.fraction else 1f - swipingState.progress.fraction
        ) {

            val properties = motionProperties(id = "chart")
            CircleChart(
                viewSize = 200,
                modifier = Modifier
                    .layoutId("chart")
                    .background(properties.value.color("background"))
                    .fillMaxWidth()
                    .height(200.dp),
                maxProgress = 5000,
                currentProgress = 2340,
                circleColor = Color.Random
            )

            val pagerState = rememberPagerState()
            val scope = rememberCoroutineScope()
            val tabWidths = remember {
                val tabWidthStateList = mutableStateListOf<Dp>()
                repeat(state.folders.size) {
                    tabWidthStateList.add(0.dp)
                }
                tabWidthStateList
            }
            val density = LocalDensity.current
            ScrollableTabRow(
                modifier = Modifier.layoutId("tab_layout"),
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
//                                TabRowDefaults.Indicator(
//                                    Modifier
//                                        .background(Color.White, RoundedCornerShape(0.1.dp))
//                                        .pagerTabIndicatorOffset(pagerState, tabPositions)
//                                )
                    Box(
                        modifier = Modifier
                            .pagerTabIndicatorOffset(pagerState, tabPositions)
//                                        .customTabIndicatorOffset(tabPositions[pagerState.currentPage], tabWidths[pagerState.currentPage])
                            .height(4.dp)
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(2.dp))
                    )
                }
            ) {
                state.folders.forEachIndexed { idx, it ->
                    Tab(
                        modifier = Modifier.height(50.dp),
                        selected = pagerState.currentPage == idx,
                        onClick = {
                            scope.launch { pagerState.scrollToPage(idx) }
//                            viewModel.changeFolder(idx)
                        }
                    ) {
                        BadgedBox(badge = { Badge { Text(it.files.size.toString()) } }) {
                            Text(it.name, onTextLayout = {
                                tabWidths[pagerState.currentPage] =
                                    with(density) { it.size.width.toDp() }
                            })
                        }
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier.layoutId("recycler"),
                count = state.folders.size,
                state = pagerState,
                contentPadding = PaddingValues(20.dp),
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState
                ),
                key = { page -> page }
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.folders[currentPage].files) {

                    }
                }
            }
        }
    }
}

fun Modifier.customTabIndicatorOffset(
    currentTabPosition: TabPosition,
    tabWidth: Dp,
): Modifier = composed {
    val currentTabWidth by animateDpAsState(
        targetValue = tabWidth,
        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = ((currentTabPosition.left + currentTabPosition.right - tabWidth) / 2),
        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset, y = 2.dp)
        .width(currentTabWidth)
}

enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CircleChartTheme {

    }
}