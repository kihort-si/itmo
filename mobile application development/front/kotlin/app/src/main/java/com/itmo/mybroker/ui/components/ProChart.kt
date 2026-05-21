package com.itmo.mybroker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itmo.mybroker.data.Candle
import com.itmo.mybroker.data.Ccy
import com.itmo.mybroker.data.Ticker
import com.itmo.mybroker.format.Fmt
import com.itmo.mybroker.ui.theme.LocalPalette
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun ProChart(
    candles: List<Candle>,
    ticker: Ticker,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
    kind: com.itmo.mybroker.ui.theme.ChartKind,
    showVol: Boolean = true,
    showMA: Boolean = true,
    showRSI: Boolean = false,
) {
    val palette = LocalPalette.current
    var hoverIdx by remember { mutableIntStateOf(-1) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val ma20 = remember(candles, showMA) {
        if (!showMA) null else IntArray(candles.size).let { _ ->
            val out = DoubleArray(candles.size)
            for (i in candles.indices) {
                val lo = max(0, i - 19)
                var s = 0.0
                for (k in lo..i) s += candles[k].c
                out[i] = s / (i - lo + 1)
            }
            out
        }
    }

    val rsi = remember(candles, showRSI) {
        if (!showRSI) null else computeRsi(candles, 14)
    }

    val lastPrice = candles.last().c
    val dp = if (ticker.ccy == Ccy.RUB && abs(lastPrice) > 1000) 1 else 2
    val hovered = if (hoverIdx in candles.indices) candles[hoverIdx] else candles.last()

    Box(modifier = modifier.fillMaxWidth().height(height)) {

        OhlcReadout(
            o = hovered.o,
            h = hovered.h,
            l = hovered.l,
            c = hovered.c,
            v = hovered.v,
            dp = dp,
            upColor = palette.up,
            downColor = palette.down,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(candles.size) {
                    detectDragGestures(
                        onDragStart = { offset -> hoverIdx = idxAtX(offset.x, canvasSize.width, candles.size) },
                        onDragEnd = { hoverIdx = -1 },
                        onDragCancel = { hoverIdx = -1 },
                        onDrag = { change, _ ->
                            hoverIdx = idxAtX(change.position.x, canvasSize.width, candles.size)
                            change.consume()
                        }
                    )
                }
        ) {
            canvasSize = size

            val padR = 52f.dp.toPx()
            val padL = 8f.dp.toPx()
            val padT = 8f.dp.toPx()
            val rsiH = if (showRSI) 56f.dp.toPx() else 0f
            val volH = if (showVol) 44f.dp.toPx() else 0f
            val padB = 22f.dp.toPx() + rsiH + (if (showVol) 4f.dp.toPx() else 0f) + (if (showRSI) 4f.dp.toPx() else 0f)
            val priceH = max(80f.dp.toPx(), size.height - padT - padB - volH)

            var mn = Double.POSITIVE_INFINITY
            var mx = Double.NEGATIVE_INFINITY
            for (c in candles) {
                if (c.l < mn) mn = c.l
                if (c.h > mx) mx = c.h
            }
            val padPad = (mx - mn) * 0.08
            val yMin = mn - padPad
            val yMax = mx + padPad
            val yToPx: (Double) -> Float = { p -> (padT + ((yMax - p) / (yMax - yMin)) * priceH).toFloat() }
            val innerW = max(40f, size.width - padL - padR)
            val cw = innerW / candles.size
            val candleW = max(1.2f, cw * 0.7f)
            val xToPx: (Int) -> Float = { i -> padL + i * cw + cw / 2f }

            val ticks = 5
            for (i in 0..ticks) {
                val v = yMin + (yMax - yMin) * (ticks - i).toDouble() / ticks
                val y = yToPx(v)
                drawLine(palette.grid, Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 1f)

                drawText(
                    text = Fmt.num(v, dp),
                    x = size.width - padR + 6.dp.toPx(),
                    y = y + 3.dp.toPx(),
                    color = palette.textDim,
                )
            }

            if (kind == com.itmo.mybroker.ui.theme.ChartKind.Line) {

                val path = Path()
                candles.forEachIndexed { i, c ->
                    val x = xToPx(i); val y = yToPx(c.c)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                val area = Path().apply {
                    addPath(path)
                    lineTo(xToPx(candles.size - 1), padT + priceH)
                    lineTo(xToPx(0), padT + priceH)
                    close()
                }
                drawPath(area, color = palette.accent, alpha = 0.12f)
                drawPath(path, color = palette.accent, style = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            } else {

                candles.forEachIndexed { i, c ->
                    val up = c.c >= c.o
                    val x = xToPx(i)
                    val yH = yToPx(c.h); val yL = yToPx(c.l)
                    val yO = yToPx(c.o); val yC = yToPx(c.c)
                    val top = min(yO, yC); val bottom = max(yO, yC)
                    val color = if (up) palette.up else palette.down
                    drawLine(color, Offset(x, yH), Offset(x, yL), strokeWidth = 1f)
                    drawRect(
                        color = color,
                        topLeft = Offset(x - candleW / 2f, top),
                        size = Size(candleW, max(0.8f, bottom - top))
                    )
                }
            }

            if (showMA && ma20 != null) {
                val path = Path()
                ma20.forEachIndexed { i, v ->
                    val x = xToPx(i); val y = yToPx(v)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = palette.ma, alpha = 0.85f, style = Stroke(width = 1.2f.dp.toPx()))
            }

            val lastY = yToPx(lastPrice)
            drawLine(
                color = palette.accent,
                start = Offset(padL, lastY),
                end = Offset(size.width - padR, lastY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f.dp.toPx(), 3f.dp.toPx())),
                alpha = 0.6f,
            )
            drawRect(
                color = palette.accent,
                topLeft = Offset(size.width - padR + 2.dp.toPx(), lastY - 9.dp.toPx()),
                size = Size(padR - 4.dp.toPx(), 18.dp.toPx()),
            )
            drawText(
                text = Fmt.num(lastPrice, dp),
                x = size.width - padR + (padR - 4.dp.toPx()) / 2 + 2.dp.toPx(),
                y = lastY + 3.dp.toPx(),
                color = palette.accentFg,
                anchorCenter = true,
            )

            if (showVol) {
                val volTop = padT + priceH + 4f.dp.toPx()
                var mxV = 0.0
                for (c in candles) if (c.v > mxV) mxV = c.v
                if (mxV > 0) {
                    candles.forEachIndexed { i, c ->
                        val x = xToPx(i)
                        val up = c.c >= c.o
                        val h = (c.v / mxV) * (volH - 4f.dp.toPx())
                        drawRect(
                            color = if (up) palette.up else palette.down,
                            topLeft = Offset(x - candleW / 2f, (volTop + (volH - 4f.dp.toPx()) - h).toFloat()),
                            size = Size(candleW, h.toFloat()),
                            alpha = 0.55f,
                        )
                    }
                }
                drawText(text = "VOL", x = padL + 4.dp.toPx(), y = volTop + 11.dp.toPx(), color = palette.textDim)
            }

            if (showRSI && rsi != null) {
                val rsiTop = padT + priceH + volH + (if (showVol) 4f.dp.toPx() else 0f) + 4f.dp.toPx()
                drawRect(
                    color = palette.surface2,
                    topLeft = Offset(padL, rsiTop),
                    size = Size(innerW, rsiH),
                    alpha = 0.6f,
                )
                drawLine(palette.grid, Offset(padL, rsiTop + rsiH * 0.3f), Offset(size.width - padR, rsiTop + rsiH * 0.3f), strokeWidth = 1f)
                drawLine(palette.grid, Offset(padL, rsiTop + rsiH * 0.7f), Offset(size.width - padR, rsiTop + rsiH * 0.7f), strokeWidth = 1f)

                val rsiToPx: (Double) -> Float = { v -> (rsiTop + ((100 - v) / 100) * rsiH).toFloat() }
                val path = Path()
                var started = false
                rsi.forEachIndexed { i, v ->
                    if (v == null) { started = false; return@forEachIndexed }
                    val x = xToPx(i); val y = rsiToPx(v)
                    if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
                }
                drawPath(path, color = palette.accent, style = Stroke(width = 1.2f.dp.toPx()))
                drawText("RSI 14", x = padL + 4.dp.toPx(), y = rsiTop + 11.dp.toPx(), color = palette.textDim)
                drawText("70", x = size.width - padR + 6.dp.toPx(), y = rsiTop + rsiH * 0.3f + 3.dp.toPx(), color = palette.textDim)
                drawText("30", x = size.width - padR + 6.dp.toPx(), y = rsiTop + rsiH * 0.7f + 3.dp.toPx(), color = palette.textDim)
            }

            if (hoverIdx in candles.indices) {
                val x = xToPx(hoverIdx)
                val y = yToPx(candles[hoverIdx].c)
                val crossEnd = padT + priceH + (if (showVol) volH + 4f.dp.toPx() else 0f) + (if (showRSI) rsiH + 4f.dp.toPx() else 0f)
                drawLine(
                    color = palette.textDim,
                    start = Offset(x, padT),
                    end = Offset(x, crossEnd),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f.dp.toPx(), 3f.dp.toPx())),
                )
                drawLine(
                    color = palette.textDim,
                    start = Offset(padL, y),
                    end = Offset(size.width - padR, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f.dp.toPx(), 3f.dp.toPx())),
                )
                drawCircle(color = palette.accent, radius = 2.5f.dp.toPx(), center = Offset(x, y))
                drawRect(
                    color = palette.surface2,
                    topLeft = Offset(size.width - padR + 2.dp.toPx(), y - 9.dp.toPx()),
                    size = Size(padR - 4.dp.toPx(), 18.dp.toPx()),
                )
                drawText(
                    text = Fmt.num(candles[hoverIdx].c, dp),
                    x = size.width - padR + (padR - 4.dp.toPx()) / 2 + 2.dp.toPx(),
                    y = y + 3.dp.toPx(),
                    color = palette.text,
                    anchorCenter = true,
                )
            }
        }
    }
}

private fun idxAtX(x: Float, w: Float, n: Int): Int {
    if (n <= 0 || w <= 0) return -1
    val padL = 8f
    val padR = 52f
    val innerW = (w - padL - padR).coerceAtLeast(40f)
    val cw = innerW / n
    val i = ((x - padL) / cw).toInt()
    return i.coerceIn(0, n - 1)
}

private fun computeRsi(candles: List<Candle>, period: Int): Array<Double?> {
    val out = arrayOfNulls<Double>(candles.size)
    if (candles.size <= period) return out
    var gains = 0.0
    var losses = 0.0
    for (i in 1..period) {
        val d = candles[i].c - candles[i - 1].c
        if (d > 0) gains += d else losses -= d
    }
    val avgG = gains / period
    val avgL = losses / period
    out[period] = 100 - 100 / (1 + (avgG / if (avgL == 0.0) 1e-9 else avgL))

    var g = gains
    var l = losses
    for (i in (period + 1) until candles.size) {
        val d = candles[i].c - candles[i - 1].c
        if (d > 0) {
            g = g - g / period + d
            l = l - l / period
        } else {
            g = g - g / period
            l = l - l / period - d
        }
        out[i] = 100 - 100 / (1 + (g / if (l == 0.0) 1e-9 else l))
    }
    return out
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    anchorCenter: Boolean = false,
) {
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgb()
        textSize = 10f.sp.toPx()
        isAntiAlias = true
        textAlign = if (anchorCenter) android.graphics.Paint.Align.CENTER else android.graphics.Paint.Align.LEFT
        typeface = android.graphics.Typeface.MONOSPACE
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

@Composable
private fun OhlcReadout(
    o: Double, h: Double, l: Double, c: Double, v: Double,
    dp: Int, upColor: Color, downColor: Color, modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReadoutPair("O", Fmt.num(o, dp), palette.textDim, palette.text)
        ReadoutPair("H", Fmt.num(h, dp), palette.textDim, palette.text)
        ReadoutPair("L", Fmt.num(l, dp), palette.textDim, palette.text)
        ReadoutPair("C", Fmt.num(c, dp), palette.textDim, if (c >= o) upColor else downColor)
        ReadoutPair("V", Fmt.vol(v), palette.textDim, palette.text)
    }
}

@Composable
private fun ReadoutPair(label: String, value: String, labelColor: Color, valueColor: Color) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
    ) {
        androidx.compose.material3.Text(
            label,
            color = labelColor,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
        androidx.compose.material3.Text(
            value,
            color = valueColor,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        )
    }
}
