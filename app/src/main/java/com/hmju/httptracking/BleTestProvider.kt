package com.hmju.httptracking

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.http.tracking_interceptor.model.ChildModel
import hmju.http.tracking_interceptor.model.ContentsModel
import hmju.http.tracking_interceptor.model.SummaryModel
import hmju.http.tracking_interceptor.model.TitleModel
import hmju.http.tracking_interceptor.model.TrackingModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale


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
                    TrackingDataManager.getInstance().add(getBleTrackingModel(result, dateFmt))
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                if (results == null) return
                results.forEach { result ->
                    val scanRecord = result.scanRecord ?: return@forEach
                    if (scanRecord.serviceUuids.isNullOrEmpty()) return@forEach
                    TrackingDataManager.getInstance().add(getBleTrackingModel(result, dateFmt))
                    Timber.w("[s] ======================================================")
                    Timber.d("Rssi ${result.rssi}dBm")
                    result.device.also { device ->
                        Timber.d("[s] Device ========================================")
                        Timber.d("Name ${device.name}")
                        Timber.d("Address ${device.address}")
                        Timber.d("[e] Device ========================================")
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val primaryPhyName = when (result.primaryPhy) {
                            BluetoothDevice.PHY_LE_1M -> "LE 1M"
                            BluetoothDevice.PHY_LE_2M -> "LE 2M"
                            BluetoothDevice.PHY_LE_CODED -> "LE Coded"
                            else -> "Unknown"
                        }
                        Timber.d("PrimaryPhy $primaryPhyName")
                        val secondaryPhyName = when (result.secondaryPhy) {
                            BluetoothDevice.PHY_LE_1M -> "LE 1M"
                            BluetoothDevice.PHY_LE_2M -> "LE 2M"
                            BluetoothDevice.PHY_LE_CODED -> "LE Coded"
                            else -> "Unknown"
                        }
                        Timber.d("SecondaryPhy $secondaryPhyName")
                        if (result.periodicAdvertisingInterval != ScanResult.PERIODIC_INTERVAL_NOT_PRESENT) {
                            Timber.d("Advertising Interval ${result.periodicAdvertisingInterval}")
                        }
                        if (result.advertisingSid != ScanResult.SID_NOT_PRESENT) {
                            Timber.d("Advertising Sid ${result.advertisingSid}")
                        }
//                        if (result.isConnectable) {
//                            Executors.newCachedThreadPool().submit {
//                                if (!connectionSet.contains(result.device.address)) {
//                                    startConnection(result.device.address)
//                                }
//                            }
//                        }
                        Timber.d("Connected ${result.isConnectable}")
                    }

//                    Timber.d("Time ${result.timestampNanos}")
//                    Timber.d("[s] ServiceData ===========================================")
//                    scanRecord.serviceData.forEach { entry ->
//                        Timber.d("UUID:${entry.key.uuid} Data:${entry.value.contentToString()}")
//                    }
//                    Timber.d("[e] ServiceData ===========================================")
//                    Timber.d("[s] Manufacture ===========================================")
//                    scanRecord.manufacturerSpecificData.forEach { key, value ->
//                        Timber.d("Key:$key Value:${value.contentToString()}")
//                    }
//                    Timber.d("[s] Manufacture ===========================================")
//                    Timber.w("[e] ======================================================")
                }
            }
        }
        val setting = ScanSettings.Builder()
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            // .setReportDelay(200)
            .build()

        val filter = ScanFilter.Builder()
            // .setServiceUuid(ParcelUuid.fromString("0000FF10-0000-1000-8000-1078CE9E8B00"))
            .build()
        val scanner = adapter.bluetoothLeScanner

        scanner.startScan(listOf(filter), setting, callback)
    }

    @SuppressLint("MissingPermission")
    private fun startConnection(
        macAddress: String
    ) {
        val callback = object : BleGattCallback() {
            override fun onConnected(gatt: BluetoothGatt) {
                connectionSet.add(macAddress)
                Timber.d("연결 완료 되었습니다. $macAddress 연결된 개수:${connectionSet.size}")
            }

            override fun onDisconnected(gatt: BluetoothGatt) {
                connectionSet.remove(macAddress)
                Timber.d("연결 해제 되었습니다. $macAddress 연결된 개수:${connectionSet.size}")
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

    @SuppressLint("MissingPermission")
    private fun getBleTrackingModel(
        data: ScanResult,
        dateFmt: SimpleDateFormat
    ): TrackingModel {
        val bootTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val scanTime = bootTimeMillis + (data.timestampNanos / 1_000_000L)
        val device = data.device
        val summary = SummaryModel(
            colorHexCode = "#367CEE",
            titleList = listOf(
                "🛜BLE",
                "Advertising",
                dateFmt.format(scanTime)
            ),
            contentsList = listOf(
                if (!device.name.isNullOrEmpty()) device.name else "Unknown",
                device.address,
                "${data.rssi}dBm"
            )
        )
        val req = mutableSetOf<ChildModel>()
        req.add(TitleModel("#C62828", "[Device]"))
        if (!device.name.isNullOrEmpty()) {
            req.add(ContentsModel(text = "Name:${device.name}"))
        }
        req.add(ContentsModel(hexCode = "#222", text = device.address))
        when (device.type) {
            BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "Unknown"
            else -> "Invalid"
        }.run { req.add(ContentsModel(hexCode = "#222", text = "Device type:${this}")) }
        req.add(ContentsModel(hexCode = "#222", text = "📶${data.rssi}dBm"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val phy = when (data.primaryPhy) {
                BluetoothDevice.PHY_LE_1M -> "LE 1M"
                BluetoothDevice.PHY_LE_2M -> "LE 2M"
                BluetoothDevice.PHY_LE_CODED -> "LE Coded"
                else -> "Unknown"
            }
            req.add(ContentsModel(hexCode = "#222", text = "Phy:${phy}"))
            req.add(ContentsModel(text = "Connectable:${data.isConnectable}"))
        }
        val scanRecord = data.scanRecord
        if (scanRecord != null) {
            req.add(TitleModel("#C62828", "[UUID]"))
            if (!scanRecord.serviceUuids.isNullOrEmpty()) {
                scanRecord.serviceUuids.forEach {
                    req.add(ContentsModel(hexCode = "#222", text = it.uuid.toString()))
                }
            }
            if (scanRecord.manufacturerSpecificData.isNotEmpty()) {
                req.add(TitleModel("#C62828", "[Manufacture]"))
                scanRecord.manufacturerSpecificData.forEach { key, value ->
                    ContentsModel(
                        hexCode = "#222",
                        text = "ID:${String.format("0x%04X", key)}"
                    ).run { req.add(this) }
                    ContentsModel(
                        hexCode = "#222",
                        text = value.joinToString { String.format("%02X", it) }
                    ).run { req.add(this) }
                }
            }
        }
        return TrackingModel(
            req = req.toList(),
            res = listOf(),
            summary = summary
        )
    }
}