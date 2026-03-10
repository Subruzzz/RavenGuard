package com.ravenguard.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravenguard.app.bluetooth.BleConstants
import com.ravenguard.app.bluetooth.BleManager
import com.ravenguard.app.ui.components.SafetyStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bleManager: BleManager
) : ViewModel() {

    val connectionState = bleManager.connectionState
    val batteryLevel = bleManager.batteryLevel
    val distance = bleManager.distanceMeters

    val safetyStatus: StateFlow<SafetyStatus> = combine(connectionState, distance) { connection, dist ->
        when {
            connection == BleManager.ConnectionState.DISCONNECTED -> SafetyStatus.ALERT
            dist > 10 -> SafetyStatus.EMERGENCY
            else -> SafetyStatus.SAFE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SafetyStatus.ALERT)

    fun startBle(permissionGranted: Boolean) = bleManager.startScanAndConnect(permissionGranted)
    fun toggleBuzzer(isOn: Boolean) = bleManager.sendCommand(if (isOn) BleConstants.START_BUZZER else BleConstants.STOP_BUZZER)
    fun stopPanic() = bleManager.sendCommand(BleConstants.STOP_PANIC)
    fun requestBattery() = bleManager.requestBattery()
}
