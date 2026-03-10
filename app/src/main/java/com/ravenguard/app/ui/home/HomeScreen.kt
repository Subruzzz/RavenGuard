package com.ravenguard.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ravenguard.app.R
import com.ravenguard.app.bluetooth.BleManager
import com.ravenguard.app.ui.components.AnimatedSOSButton
import com.ravenguard.app.ui.components.AnimatedStatusChip
import com.ravenguard.app.ui.components.GlassCard
import com.ravenguard.app.ui.components.StatusIndicator

@Composable
fun HomeScreen(
    permissionGranted: Boolean,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val connection by viewModel.connectionState.collectAsStateWithLifecycle()
    val battery by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()
    val safety by viewModel.safetyStatus.collectAsStateWithLifecycle()

    var buzzerOn by remember { mutableStateOf(false) }
    val connectedComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.device_connected))

    androidx.compose.runtime.LaunchedEffect(permissionGranted) {
        viewModel.startBle(permissionGranted)
        viewModel.requestBattery()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) onOpenSettings()
                }
            }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️ RavenGuard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusIndicator(connection == BleManager.ConnectionState.CONNECTED)
                        Text("Connection: ${connection.name}")
                    }
                    Text("Battery: $battery%")
                    Text("Distance: ${"%.1f".format(distance)} m")
                    LottieAnimation(connectedComposition, modifier = Modifier.size(64.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Safety Status:")
                Spacer(modifier = Modifier.size(8.dp))
                AnimatedStatusChip(safety)
            }

            Button(onClick = {
                buzzerOn = !buzzerOn
                viewModel.toggleBuzzer(buzzerOn)
            }, modifier = Modifier.height(56.dp)) {
                Text("TEST WEARABLE")
            }

            Button(onClick = viewModel::stopPanic, modifier = Modifier.height(56.dp)) {
                Text("STOP PANIC")
            }

            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AnimatedSOSButton(onClick = viewModel::stopPanic)
            }
        }
    }
}
