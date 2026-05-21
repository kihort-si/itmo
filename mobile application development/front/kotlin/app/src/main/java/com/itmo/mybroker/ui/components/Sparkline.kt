package com.itmo.mybroker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Sparkline(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 22.dp,
    fill: Boolean = false,
) {
    Canvas(modifier = modifier.size(width = width, height = height)) {
        if (data.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val mn = data.minOrNull() ?: 0.0
        val mx = data.maxOrNull() ?: 0.0
        val span = (mx - mn).takeIf { it > 0 } ?: 1.0
        val step = w / (data.size - 1)
        val pts = data.mapIndexed { i, v ->
            Offset(
                x = i * step,
                y = (h - ((v - mn) / span) * (h - 2) - 1).toFloat()
            )
        }
        val line = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (k in 1 until pts.size) lineTo(pts[k].x, pts[k].y)
        }
        if (fill) {
            val area = Path().apply {
                addPath(line)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(area, color = color, alpha = 0.12f)
        }
        drawPath(
            line,
            color = color,
            style = Stroke(width = 1.4f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
