package com.example.ainote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ainote.ui.theme.NoteYellow

@Composable
fun CustomRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Int = 28,
    activeColor: Color = NoteYellow,
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val starWidth = size.width.toFloat() / maxRating
                    val newRating = (offset.x / starWidth).coerceIn(0f, maxRating.toFloat())
                    onRatingChanged(kotlin.math.ceil(newRating.toDouble()).toFloat())
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxRating) {
            val filled = i <= rating
            val scale by animateFloatAsState(
                targetValue = if (filled) 1.1f else 1f,
                animationSpec = tween(200),
                label = "starScale"
            )
            val color by animateColorAsState(
                targetValue = if (filled) activeColor else inactiveColor,
                animationSpec = tween(200),
                label = "starColor"
            )

            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Star $i",
                tint = color,
                modifier = Modifier
                    .size(starSize.dp)
                    .scale(scale)
            )
        }
    }
}
