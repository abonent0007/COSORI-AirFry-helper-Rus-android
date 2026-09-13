package com.cosory.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cosory.app.ui.theme.Motion

@Composable
fun TimerRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 260.dp,
    strokeWidth: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val target = fraction.coerceIn(0f, 1f)
    val animated: Float = if (Motion.animationsEnabled()) {
        val value by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 950, easing = LinearEasing),
            label = "timerRing",
        )
        value
    } else {
        target
    }

    val urgency = ((0.2f - target) / 0.2f).coerceIn(0f, 1f)
    val progressColor = lerp(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.error,
        urgency,
    )
    val glowColor = progressColor.copy(alpha = 0.25f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke * 1.6f
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animated > 0.001f) {
                drawArc(
                    color = glowColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke * 2.4f, cap = StrokeCap.Round),
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}
