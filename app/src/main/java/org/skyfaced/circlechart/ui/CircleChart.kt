package org.skyfaced.circlechart.ui

import android.graphics.Paint.Align.CENTER
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.skyfaced.circlechart.ui.theme.CircleChartTheme
import org.skyfaced.circlechart.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * @param prefix applies before [currentProgress] without any formatting
 * @param suffix applies after [currentProgress] without any formatting
 * @param viewSize specify in mind as dp cause it's converts to dp under the hood
 * @param rainbow overrides static [circleColor]
 */
@Composable
fun CircleChart(
    currentProgress: Int,
    modifier: Modifier = Modifier,
    maxProgress: Int = currentProgress,
    viewSize: Int = CircleChartDefaults.ViewSize,
    circleBackgroundColor: Color = Color.LightGray.copy(alpha = 0.25f),
    circleColor: Color = Color.Black,
    circleWidth: Float = CircleChartDefaults.CircleWidth,
    fontSize: Float = CircleChartDefaults.FontSize,
    fontColor: Color = Color.Black,
    prefix: String = String.Empty,
    suffix: String = String.Empty,
    rainbow: Rainbow? = null,
    isDebug: Boolean = false,
) = CircleChart(
    currentProgress = currentProgress.toFloat(),
    modifier = modifier,
    maxProgress = maxProgress.toFloat(),
    viewSize = viewSize,
    circleBackgroundColor = circleBackgroundColor,
    circleColor = circleColor,
    circleWidth = circleWidth,
    fontSize = fontSize,
    fontColor = fontColor,
    prefix = prefix,
    suffix = suffix,
    rainbow = rainbow,
    isDebug = isDebug
)

/**
 * @param prefix applies before [currentProgress] without any formatting
 * @param suffix applies after [currentProgress] without any formatting
 * @param viewSize specify in mind as dp cause it's converts to dp under the hood
 * @param rainbow overrides static [circleColor]
 */
@Composable
fun CircleChart(
    currentProgress: Float,
    modifier: Modifier = Modifier,
    maxProgress: Float = currentProgress,
    viewSize: Int = CircleChartDefaults.ViewSize,
    circleBackgroundColor: Color = Color.LightGray.copy(alpha = 0.25f),
    circleColor: Color = Color.Black,
    circleWidth: Float = CircleChartDefaults.CircleWidth,
    fontSize: Float = CircleChartDefaults.FontSize,
    fontColor: Color = Color.Black,
    prefix: String = String.Empty,
    suffix: String = String.Empty,
    rainbow: Rainbow? = null,
    isDebug: Boolean = false,
) {
    val currentProgressState by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(700, easing = FastOutSlowInEasing)
    )
    val sweepAngleState by animateFloatAsState(
        targetValue = 360 * (currentProgress / maxProgress),
        animationSpec = tween(700, easing = FastOutSlowInEasing)
    )
    val circleColorState by animateColorAsState(
        targetValue = circleColor,
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    )
    val fontColorState by animateColorAsState(
        targetValue = fontColor,
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    )
    val rainbowTransition = rememberInfiniteTransition()
    val rainbowState = rainbowTransition.animateValue(
        initialValue = 0,
        targetValue = 360,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier.then(Modifier.size(viewSize.dp))
    ) {
        val innerCircleWidth = circleWidth * min(1f, size.maxDimension / viewSize)
        val innerFontSize = max(10f, fontSize * min(1f, size.maxDimension / viewSize))

        drawCircle(
            color = circleBackgroundColor,
            radius = size.minDimension / 2.0f - innerCircleWidth,
            style = Stroke(innerCircleWidth)
        )

        val left =
            if (size.width == size.maxDimension) (size.maxDimension - size.minDimension) / 2 + innerCircleWidth
            else innerCircleWidth
        val top =
            if (size.height == size.maxDimension) (size.maxDimension - size.minDimension) / 2 + innerCircleWidth
            else innerCircleWidth
        translate(left = left, top = top) {
            val brush =
                if (rainbow == null) {
                    Brush.linearGradient(listOf(circleColorState, circleColorState))
                } else Brush.sweepGradient(
                    if (rainbow.animate) {
                        if (rainbow.animationRotation == Rotation.Clockwise)
                            Color.RainbowHsl.leftShift(rainbowState.value)
                        else
                            Color.RainbowHsl.rightShift(rainbowState.value)
                    } else Color.RainbowHsl,
                    Offset(center.x - innerCircleWidth, center.y - innerCircleWidth)
                )
            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = sweepAngleState,
                useCenter = false,
                style = Stroke(
                    width = innerCircleWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                size = Size(
                    size.minDimension - innerCircleWidth * 2,
                    size.minDimension - innerCircleWidth * 2
                )
            )
        }

        drawText(
            text = "$prefix${currentProgressState.toInt()}$suffix",
            paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = innerFontSize.sp.toPx()
                color = fontColorState.toArgb()
                textAlign = CENTER
            },
            x = size.width / 2,
            y = size.height / 2 + innerFontSize / 2
        )

        if (isDebug) drawCrosshair()
    }
}

private fun DrawScope.drawText(
    text: String,
    x: Float,
    y: Float,
    paint: NativePaint,
) {
    drawIntoCanvas {
        it.nativeCanvas.drawText(text, x, y, paint)
    }
}

private fun DrawScope.drawCrosshair(
    color: Color = Color.Random,
) {
    drawLine(
        color = color,
        start = Offset(
            0f,
            size.height / 2f
        ),
        end = Offset(
            size.width,
            size.height / 2f
        ),
        strokeWidth = 10f
    )

    drawLine(
        color = color,
        start = Offset(
            size.width / 2f,
            0f
        ),
        end = Offset(
            size.width / 2f,
            size.height
        ),
        strokeWidth = 10f
    )
}

object CircleChartDefaults {
    const val ViewSize = 150
    const val CircleWidth = 25.0f
    const val FontSize = 25.0f
}

@Preview(showBackground = true)
@Composable
private fun CircleChartPreview() {
    CircleChartTheme {
        CircleChart(maxProgress = 300, currentProgress = 196)
    }
}
