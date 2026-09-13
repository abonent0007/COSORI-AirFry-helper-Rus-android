package com.cosory.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cosory.app.ui.theme.Motion

@Composable
private fun roundedHalo(
    color: Color,
    cornerRadius: Dp,
    alpha: Float,
    spread: Dp,
): Modifier = Modifier.drawBehind {
    val base = spread.toPx()
    val radius = cornerRadius.toPx()
    for (layer in 3 downTo 1) {
        val s = base * layer / 3f
        drawRoundRect(
            color = color.copy(alpha = alpha / (layer + 1)),
            topLeft = Offset(-s, -s),
            size = Size(size.width + s * 2, size.height + s * 2),
            cornerRadius = CornerRadius(radius + s, radius + s),
        )
    }
}

@Composable
fun Modifier.ctaGlow(enabled: Boolean = true, cornerRadius: Dp = 16.dp): Modifier {
    if (!enabled) return this
    return roundedHalo(
        color = MaterialTheme.colorScheme.primary,
        cornerRadius = cornerRadius,
        alpha = 0.55f,
        spread = 8.dp,
    )
}

@Composable
fun Modifier.pulseGlow(enabled: Boolean = true, cornerRadius: Dp = 16.dp): Modifier {
    if (!enabled || !Motion.animationsEnabled()) return this
    val transition = rememberInfiniteTransition(label = "pulseGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(Motion.PULSE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    return roundedHalo(
        color = MaterialTheme.colorScheme.primary,
        cornerRadius = cornerRadius,
        alpha = alpha,
        spread = 10.dp,
    )
}

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(Motion.PRESS_MS, easing = Motion.EmphasizedDecelerate),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun glowTextStyle(
    base: TextStyle,
    color: Color? = null,
    alpha: Float = 0.55f,
    blur: Float = 10f,
): TextStyle {
    val glowColor = color ?: MaterialTheme.colorScheme.primary
    return base.copy(shadow = Shadow(color = glowColor.copy(alpha = alpha), blurRadius = blur))
}

@Composable
fun GlowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pulsing: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)
    val glow = if (pulsing) {
        Modifier.pulseGlow(enabled = enabled, cornerRadius = 16.dp)
    } else {
        Modifier.ctaGlow(enabled = enabled, cornerRadius = 16.dp)
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        interactionSource = interaction,
        modifier = modifier
            .pressScale(interaction)
            .then(glow),
    ) {
        content()
    }
}
