package com.mmdpuff.lumio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    initialColor: Color = Color(0xFF9B55F6),
    onColorChanged: (Color) -> Unit
) {

    var selectedColor by remember(initialColor) {
        mutableStateOf(initialColor)
    }

    var hue by remember(initialColor) {
        mutableFloatStateOf(
            android.graphics.Color.RGBToHSV(
                (initialColor.red * 255).toInt(),
                (initialColor.green * 255).toInt(),
                (initialColor.blue * 255).toInt(),
                FloatArray(3)
            ).let {
                val hsv = FloatArray(3)
                android.graphics.Color.RGBToHSV(
                    (initialColor.red * 255).toInt(),
                    (initialColor.green * 255).toInt(),
                    (initialColor.blue * 255).toInt(),
                    hsv
                )
                hsv[0]
            }
        )
    }

    var isDragging by remember { mutableStateOf(false) }

    fun updateColor(position: Offset, size: IntSize) {
        val center = Offset(
            size.width / 2f,
            size.height / 2f
        )

        val dx = position.x - center.x
        val dy = position.y - center.y

        val angle = Math.toDegrees(
            atan2(dy, dx).toDouble()
        ).toFloat()

        hue = (angle + 360f) % 360f

        selectedColor = Color.hsv(
            hue = hue,
            saturation = 0.65f,
            value = 0.98f
        )

        onColorChanged(selectedColor)
    }

    fun isOnRing(position: Offset, size: IntSize): Boolean {
        val center = Offset(
            size.width / 2f,
            size.height / 2f
        )

        val dx = position.x - center.x
        val dy = position.y - center.y

        val distance = sqrt(dx * dx + dy * dy)

        val dimension = min(size.width, size.height).toFloat()
        val outerRadius = dimension * 0.46f
        val ringWidth = dimension * 0.105f
        val innerRadius = outerRadius - ringWidth
        val middleRadius = (outerRadius + innerRadius) / 2f

        return kotlin.math.abs(distance - middleRadius) < dimension * 0.07f
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    if (isOnRing(position, size)) {
                        updateColor(position, size)
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { position ->
                        isDragging = isOnRing(position, size)

                        if (isDragging) {
                            updateColor(position, size)
                        }
                    },
                    onDrag = { change, _ ->
                        if (isDragging) {
                            updateColor(change.position, size)
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
    ) {

        val dimension = min(size.width, size.height)

        val center = Offset(
            size.width / 2f,
            size.height / 2f
        )

        val outerRadius = dimension * 0.46f
        val ringWidth = dimension * 0.105f

        val innerRadius = outerRadius - ringWidth
        val middleRadius = (outerRadius + innerRadius) / 2f
        val centerRadius = innerRadius - dimension * 0.025f

        // Hue Gradient
        val colors = listOf(
            Color.Red,
            Color(0xFFFF8547),
            Color.Yellow,
            Color(0xFF65E77B),
            Color(0xFF35DCEB),
            Color(0xFF478BFF),
            Color(0xFF9B55FF),
            Color(0xFFE34BEA),
            Color(0xFFFF426C),
            Color.Red
        )

        // Outer Ring
        drawCircle(
            brush = Brush.sweepGradient(colors),
            radius = outerRadius,
            center = center
        )

        // Inner Dark Border
        drawCircle(
            color = Color(0xFF0B0B12),
            radius = innerRadius,
            center = center
        )

        // Center Border
        drawCircle(
            color = Color(0xFF30213F),
            radius = centerRadius + dimension * 0.008f,
            center = center,
            style = Stroke(width = dimension * 0.004f)
        )

        // Selected Color
        drawCircle(
            color = selectedColor,
            radius = centerRadius,
            center = center
        )

        // Selector Position
        val angleRadians = hue * (PI.toFloat() / 180f)

        val selectorCenter = Offset(
            x = center.x + middleRadius * kotlin.math.cos(angleRadians),
            y = center.y + middleRadius * kotlin.math.sin(angleRadians)
        )

        val selectorRadius = dimension * 0.052f

        // Selector Shadow / Glow
        drawCircle(
            color = selectedColor.copy(alpha = 0.35f),
            radius = selectorRadius * 1.5f,
            center = selectorCenter
        )

        // White Outer Circle
        drawCircle(
            color = Color.White,
            radius = selectorRadius,
            center = selectorCenter
        )

        // Selector Fill
        drawCircle(
            color = selectedColor,
            radius = selectorRadius * 0.76f,
            center = selectorCenter
        )
    }
}