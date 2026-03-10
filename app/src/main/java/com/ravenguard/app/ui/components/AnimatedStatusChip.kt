package com.ravenguard.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SafetyStatus { SAFE, ALERT, EMERGENCY }

@Composable
fun AnimatedStatusChip(status: SafetyStatus) {
    val color = animateColorAsState(
        targetValue = when (status) {
            SafetyStatus.SAFE -> Color(0xFF00E676)
            SafetyStatus.ALERT -> Color(0xFFFFA726)
            SafetyStatus.EMERGENCY -> Color(0xFFFF1744)
        }, label = "chipColor"
    )
    Text(
        text = status.name,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color.value.copy(alpha = 0.8f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
