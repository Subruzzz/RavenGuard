package com.ravenguard.app.bluetooth

import java.util.UUID

object BleConstants {
    const val DEVICE_NAME = "RavenGuard"
    val SERVICE_UUID: UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    val COMMAND_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000dcba-0000-1000-8000-00805f9b34fb")
    val BATTERY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

    const val START_BUZZER = "START_BUZZER"
    const val STOP_BUZZER = "STOP_BUZZER"
    const val STOP_PANIC = "STOP_PANIC"
    const val GET_BATTERY = "GET_BATTERY"
}
