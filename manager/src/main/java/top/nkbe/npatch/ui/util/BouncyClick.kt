package top.nkbe.npatch.ui.util

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    showRipple: Boolean = false,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isBouncy = top.nkbe.npatch.config.Configs.bouncyAnimations
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && isBouncy) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 350f
        ),
        label = "bouncyScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interactionSource,
        indication = if (showRipple || !isBouncy) androidx.compose.foundation.LocalIndication.current else null,
        enabled = enabled,
        onClick = onClick
    )
}

fun Modifier.shimmerWave(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_wave")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface
    
    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.6f),
            highlightColor.copy(alpha = 0.3f),
            baseColor.copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim - 250f, 0f),
        end = Offset(translateAnim, 0f)
    )

    this.background(brush)
}
