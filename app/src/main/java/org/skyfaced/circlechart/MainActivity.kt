package org.skyfaced.circlechart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.MotionLayout
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import org.skyfaced.circlechart.ui.CircleChart
import org.skyfaced.circlechart.ui.theme.CircleChartTheme
import org.skyfaced.circlechart.util.Rainbow
import org.skyfaced.circlechart.util.Rotation
import org.skyfaced.circlechart.util.lerp
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircleChartTheme {
                HorizontalPagerScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class,
    androidx.constraintlayout.compose.ExperimentalMotionApi::class,
    ExperimentalMaterialApi::class)
@Composable
fun HorizontalPagerScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    val scale = remember { mutableStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale.value *= zoomChange
        offset.value += offsetChange
    }

    val swipingState = rememberSwipeableState(initialValue = SwipingStates.EXPANDED)

    MotionLayout(
        start = start,
        end = end,
        progress = if (swipingState.progress.to == SwipingStates.COLLAPSED) swipingState.progress.fraction else 1f - swipingState.progress.fraction
    ) {
        CircleChart(
            modifier = Modifier
                .layoutId("circle")
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offset.value.x,
                    translationY = offset.value.y
                )
                .transformable(transformState)
                .padding(vertical = 48.dp),
            viewSize = 150,
            maxProgress = state.maxProgress,
            currentProgress = state.folders[pagerState.currentPage].size,
            rainbow = Rainbow(true, Rotation.Clockwise),
        )

        ScrollableTabRow(
            modifier = Modifier.layoutId("tabs"),
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
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

        HorizontalPager(
            modifier = Modifier
                .layoutId("recycler")
                .background(Color.White),
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
                    }
                    .swipeable(
                        state = swipingState,
                        anchors = mapOf(
                            0f to SwipingStates.COLLAPSED,
                            with(LocalDensity.current) { 2 * 150.dp.toPx() } to SwipingStates.EXPANDED
                        ),
                        orientation = Orientation.Vertical,
                    )
                    .nestedScroll(connection),
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

val start = ConstraintSet {
    val circle = createRefFor("circle")
    val tabs = createRefFor("tabs")
    val recycler = createRefFor("recycler")

    constrain(circle) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    constrain(tabs) {
        top.linkTo(circle.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    constrain(recycler) {
        top.linkTo(tabs.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
}

val end = ConstraintSet {
    val circle = createRefFor("circle")
    val tabs = createRefFor("tabs")
    val recycler = createRefFor("recycler")

    constrain(circle) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        translationY = (-100).dp
    }

    constrain(tabs) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    constrain(recycler) {
        top.linkTo(tabs.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
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