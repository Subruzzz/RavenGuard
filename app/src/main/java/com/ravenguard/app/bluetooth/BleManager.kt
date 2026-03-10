package com.ravenguard.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class BleManager @Inject constructor(
    private val context: Context
) {
    enum class ConnectionState { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, FAILED }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner get() = bluetoothAdapter?.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var currentDevice: BluetoothDevice? = null
    private var reconnectJob: Job? = null
    private var rssiJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0.0)
    val distanceMeters: StateFlow<Double> = _distanceMeters.asStateFlow()

    private val _rssi = MutableStateFlow(-100)
    val rssi: StateFlow<Int> = _rssi.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startScanAndConnect(permissionGranted: Boolean) {
        if (!permissionGranted || scanner == null) return
        if (_connectionState.value == ConnectionState.CONNECTED || _connectionState.value == ConnectionState.SCANNING) return

        _connectionState.value = ConnectionState.SCANNING

        val filter = ScanFilter.Builder()
            .setDeviceName(BleConstants.DEVICE_NAME)
            .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(listOf(filter), settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        stopScan()
        currentDevice = device
        _connectionState.value = ConnectionState.CONNECTING
        bluetoothGatt?.close()
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        reconnectJob?.cancel()
        rssiJob?.cancel()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    @SuppressLint("MissingPermission")
    fun sendCommand(command: String) {
        val characteristic = commandCharacteristic ?: return
        characteristic.value = command.toByteArray(StandardCharsets.UTF_8)
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun requestBattery() {
        sendCommand(BleConstants.GET_BATTERY)
        batteryCharacteristic?.let { bluetoothGatt?.readCharacteristic(it) }
    }

    private fun startRssiUpdates() {
        rssiJob?.cancel()
        rssiJob = scope.launch {
            while (_connectionState.value == ConnectionState.CONNECTED) {
                bluetoothGatt?.readRemoteRssi()
                delay(3_000)
            }
        }
    }

    private fun updateDistance(rssi: Int) {
        val txPower = -59
        val n = 2.0
        val distance = 10.0.pow((txPower - rssi) / (10 * n))
        _distanceMeters.value = distance
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            repeat(5) { attempt ->
                delay(5_000)
                currentDevice?.let { connect(it); return@launch }
                if (attempt == 4) _connectionState.value = ConnectionState.FAILED
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name == BleConstants.DEVICE_NAME) {
                connect(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    gatt.discoverServices()
                    startRssiUpdates()
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    scheduleReconnect()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service: BluetoothGattService? = gatt.getService(BleConstants.SERVICE_UUID)
            commandCharacteristic = service?.getCharacteristic(BleConstants.COMMAND_CHARACTERISTIC_UUID)
            batteryCharacteristic = service?.getCharacteristic(BleConstants.BATTERY_CHARACTERISTIC_UUID)
            requestBattery()
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            _rssi.value = rssi
            updateDistance(rssi)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (characteristic.uuid == BleConstants.BATTERY_CHARACTERISTIC_UUID && value.isNotEmpty()) {
                _batteryLevel.value = value[0].toInt() and 0xFF
            }
        }
    }
}
