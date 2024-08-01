package hmju.tracking.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.SystemClock
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import hmju.tracking.model.ChildModel
import hmju.tracking.model.ContentsModel
import hmju.tracking.model.SummaryModel
import hmju.tracking.model.TitleModel
import hmju.tracking.model.TrackingModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("MissingPermission")
class HardwareTrackingModel : TrackingModel {

    companion object {
        private val dateFmt: SimpleDateFormat by lazy {
            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        }
    }

    constructor(
        data: ScanResult
    ) {
        setSummary(initSummary(data))
        setReqModels(initReqModels(data))
    }

    constructor(
        gatt: BluetoothGatt,
        byteArray: ByteArray
    ) {

    }

    /**
     * init Summary Bluetooth Advertising Type
     * @param data BLE ADV Data
     */
    private fun initSummary(data: ScanResult): SummaryModel {
        val bootTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val device = data.device
        return SummaryModel(
            colorHexCode = "#367CEE",
            titleList = listOf(
                "🛜BLE",
                "Advertising",
                dateFmt.format(bootTimeMillis + (data.timestampNanos / 1_000_000L))
            ),
            contentsList = listOf(
                if (!device.name.isNullOrEmpty()) device.name else "Unknown",
                device.address,
                "${data.rssi}dBm"
            )
        )
    }

    /**
     * Ble Advertising Data
     * @param data BLE ADV Data
     */
    private fun initReqModels(data: ScanResult): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        val device = data.device
        list.add(TitleModel("#C62828", "[Device]"))
        if (!device.name.isNullOrEmpty()) {
            list.add(ContentsModel(text = "Name:${device.name}"))
        }
        list.add(ContentsModel(hexCode = "#222222", text = device.address))
        when (device.type) {
            BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "Unknown"
            else -> "Invalid"
        }.run { list.add(ContentsModel(hexCode = "#222222", text = "Device type:${this}")) }
        list.add(ContentsModel(hexCode = "#222222", text = "📶${data.rssi}dBm"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val phy = when (data.primaryPhy) {
                BluetoothDevice.PHY_LE_1M -> "LE 1M"
                BluetoothDevice.PHY_LE_2M -> "LE 2M"
                BluetoothDevice.PHY_LE_CODED -> "LE Coded"
                else -> "Unknown"
            }
            list.add(ContentsModel(hexCode = "#222222", text = "Phy:${phy}"))
            list.add(ContentsModel(text = "Connectable:${data.isConnectable}"))
        }
        val scanRecord = data.scanRecord
        if (scanRecord != null) {
            if (!scanRecord.serviceUuids.isNullOrEmpty()) {
                list.add(TitleModel("#C62828", "[UUID]"))
                scanRecord.serviceUuids.forEach {
                    list.add(ContentsModel(hexCode = "#222222", text = it.uuid.toString()))
                }
            }
            if (scanRecord.manufacturerSpecificData.isNotEmpty()) {
                list.add(TitleModel("#C62828", "[Manufacture]"))
                scanRecord.manufacturerSpecificData.forEach { key, value ->
                    ContentsModel(
                        hexCode = "#222222",
                        text = "ID:${String.format("0x%04X", key)}"
                    ).run { list.add(this) }
                    ContentsModel(
                        hexCode = "#222222",
                        text = value.joinToString { String.format("%02X", it) }
                    ).run { list.add(this) }
                }
            }
        }
        return list
    }
}