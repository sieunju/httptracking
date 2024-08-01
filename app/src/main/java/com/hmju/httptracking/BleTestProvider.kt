package com.hmju.httptracking

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.tracking.hardware.HardwareTrackingModel
import timber.log.Timber
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors


class BleTestProvider(
    private val activity: AppCompatActivity
) {

    open class BleGattCallback {

        open fun onConnected(gatt: BluetoothGatt) {}
        open fun onDisconnected(gatt: BluetoothGatt) {}
        open fun onServicesDiscovered(gatt: BluetoothGatt) {}
        open fun onCharacteristicRead(gatt: BluetoothGatt, value: ByteArray) {}
        open fun onCharacteristicChanged(gatt: BluetoothGatt, value: ByteArray) {}

        val origin = object : BluetoothGattCallback() {

            @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (gatt == null) return
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                    onConnected(gatt)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onDisconnected(gatt)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (gatt == null) return
                onServicesDiscovered(gatt)
            }

            @Suppress("DEPRECATION")
            @Deprecated("Deprecated in Java")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (gatt == null || characteristic == null) return
                onCharacteristicRead(gatt, characteristic.value)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                onCharacteristicRead(gatt, value)
            }

            @Suppress("DEPRECATION")
            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                if (gatt == null || characteristic == null) return
                onCharacteristicChanged(gatt, characteristic.value)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                onCharacteristicChanged(gatt, value)
            }
        }
    }

    private val connectionSet = mutableSetOf<String>()
    private val manager: BluetoothManager by lazy {
        activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val adapter: BluetoothAdapter by lazy { manager.adapter }
    private val dateFmt: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    }

    @SuppressLint("MissingPermission")
    fun startBleAdv() {
        val duplicationSet = mutableSetOf<String>()
        val callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (result == null) return
                if (!duplicationSet.contains(result.device.address)) {
                    duplicationSet.add(result.device.address)
                    TrackingDataManager.getInstance().add(HardwareTrackingModel(result))
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                if (results == null) return
                results.forEach { result ->
                    TrackingDataManager.getInstance().add(HardwareTrackingModel(result))
                }
            }
        }
        val setting = ScanSettings.Builder()
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            // .setReportDelay(1000)
            .build()

        val filter = ScanFilter.Builder()
            .build()
        val scanner = adapter.bluetoothLeScanner

        scanner.startScan(listOf(filter), setting, callback)
    }

    @SuppressLint("MissingPermission")
    fun startConnection(
        macAddress: String,
        findUuid: String
    ) {
        val callback = object : BleGattCallback() {
            override fun onConnected(gatt: BluetoothGatt) {
                Timber.d("onConnected $gatt")
            }

            override fun onDisconnected(gatt: BluetoothGatt) {
                Timber.d("onDisconnected $gatt")
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt) {
                var characteristic: BluetoothGattCharacteristic? = null
                for (service in gatt.services) {
                    for (character in service.characteristics) {
                        val uuid = character.uuid.toString().uppercase()
                        if (findUuid.contains(uuid) || uuid.contains(findUuid)) {
                            characteristic = character
                            break
                        }
                    }
                }
                Timber.d("characteristic 찾습니다..$characteristic")
                if (characteristic == null) return
                val writeBytes = ByteBuffer.allocate(2)
                    .putShort(10000.toShort())
                    .array() + ByteBuffer.allocate(2)
                    .putShort(10387.toShort()).array()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Timber.d("데이터 사용합니다. ${writeBytes.contentToString()}")
                    gatt.writeCharacteristic(
                        characteristic,
                        writeBytes,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                    Executors.newSingleThreadExecutor().submit {
                        Thread.sleep(300)
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        gatt.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                    }
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, value: ByteArray) {
                TrackingDataManager.getInstance().add(HardwareTrackingModel(gatt, value))
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, value: ByteArray) {
                Timber.d("onCharacteristicChanged ${value.contentToString()}")
                TrackingDataManager.getInstance().add(HardwareTrackingModel(gatt, value))
            }
        }
        val device = adapter.getRemoteDevice(macAddress)
        device.connectGatt(
            activity,
            false,
            callback.origin,
            BluetoothDevice.TRANSPORT_LE
        ).also { it.requestMtu(300) }
    }
}