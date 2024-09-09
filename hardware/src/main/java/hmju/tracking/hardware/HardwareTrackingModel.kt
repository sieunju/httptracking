package hmju.tracking.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanResult
import android.nfc.tech.Ndef
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

    constructor(gatt: BluetoothGatt) {
        setSummary(initSummary(gatt))
        setReqModels(initReqModels(gatt))
    }

    constructor(
        gatt: BluetoothGatt,
        byteArray: ByteArray
    ) {
        setSummary(initSummary(gatt))
        setReqModels(initReqModels(gatt))
        setResModels(initResModels(byteArray))
    }

    constructor(ndef: Ndef) {
        setSummary(initSummary(ndef))
        setReqModels(initReqModels(ndef))
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
                if (!device.name.isNullOrEmpty()) device.name else "Empty Name",
                device.address,
                "${data.rssi}dBm"
            )
        )
    }

    private fun initSummary(data: BluetoothGatt): SummaryModel {
        val device = data.device
        return SummaryModel(
            colorHexCode = "#367CEE",
            titleList = listOf(
                "🛜BLE",
                "Connect",
                dateFmt.format(System.currentTimeMillis())
            ),
            contentsList = listOf(
                if (!device.name.isNullOrEmpty()) device.name else "Empty Name",
                device.address,
                "-"
            )
        )
    }

    private fun initSummary(data: Ndef): SummaryModel {
        return SummaryModel(
            colorHexCode = "#FFC619",
            titleList = listOf(
                "🏷️NFC",
                "Tag",
                dateFmt.format(System.currentTimeMillis())
            ),
            contentsList = listOf(
                data.type,
                "${data.maxSize}",
                "-"
            )
        )
    }

    /**
     * Ble Advertising Data
     * @param data BLE ADV Data
     */
    private fun initReqModels(
        data: ScanResult
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        val device = data.device
        list.add(TitleModel(hexCode = "#C62828", text = "[Device]"))
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

    private fun initReqModels(
        gatt: BluetoothGatt
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        val device = gatt.device
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
        }.run { list.add(ContentsModel(hexCode = "#222222", text = "DeviceType\n${this}")) }
        if (!gatt.services.isNullOrEmpty()) {
            gatt.services.forEach { service ->
                list.add(TitleModel("#C62828", "Service: ${service.uuid}"))
                when (service.type) {
                    BluetoothGattService.SERVICE_TYPE_PRIMARY -> "PRIMARY"
                    BluetoothGattService.SERVICE_TYPE_SECONDARY -> "SECONDARY"
                    else -> "Unknown Type"
                }.run { list.add(ContentsModel(hexCode = "#222222", text = "ServiceType: $this")) }
                service.characteristics.forEach { characteristic ->
                    val str = StringBuilder("Characteristic")
                    str.appendLine("\t${characteristic.uuid}")
                    when (characteristic.writeType) {
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT -> "DEFAULT"
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE -> "NO_RESPONSE"
                        BluetoothGattCharacteristic.WRITE_TYPE_SIGNED -> "SIG"
                        else -> "Unknown"
                    }.run { str.appendLine("WriteType: $this") }
                    if (!characteristic.descriptors.isNullOrEmpty()) {
                        str.appendLine("Descriptors")
                        characteristic.descriptors.forEach { descriptor ->
                            str.appendLine("\t${descriptor.uuid}")
                        }
                    }
                    characteristic.properties.also { properties ->
                        str.append("Properties:")
                        val propertyList = mutableListOf<String>()
                        if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                            propertyList.add("NOTIFICATION")
                        }
                        if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                            propertyList.add("INDICATE")
                        }
                        if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                            propertyList.add("READ")
                        }
                        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                            propertyList.add("WRITE")
                        }
                        str.appendLine(propertyList.joinToString(", "))
                    }
                    list.add(TitleModel("#222222", str))
                }
            }
        }
        return list
    }

    private fun initReqModels(
        data: Ndef
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        ContentsModel(
            hexCode = "#222222",
            text = "Type: ${data.type}"
        ).run { list.add(this) }
        ContentsModel(
            hexCode = "#222222",
            text = "Size: ${data.maxSize}"
        ).run { list.add(this) }
        val records = data.ndefMessage?.records
        if (records != null) {
            TitleModel(
                hexCode = "#C62828",
                text = "[RAW]"
            ).run { list.add(this) }
            val payload = records[0].payload
            ContentsModel(
                hexCode = "#222222",
                text = payload.joinToString { String.format("%02X", it) }
            ).run { list.add(this) }
        }
        val cacheRecords = data.cachedNdefMessage?.records
        if (cacheRecords != null) {
            TitleModel(
                hexCode = "#C62828",
                text = "[Cache RAW]"
            ).run { list.add(this) }
            val payload = cacheRecords[0].payload
            ContentsModel(
                hexCode = "#222222",
                text = payload.joinToString { String.format("%02X", it) }
            ).run { list.add(this) }
        }

        return list
    }

    private fun initResModels(
        bytes: ByteArray
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        list.add(TitleModel(hexCode = "#C62828", text = "[${bytes.size} Bytes]"))
        ContentsModel(
            hexCode = "#222222",
            text = bytes.joinToString { String.format("%02X", it) }
        ).run { list.add(this) }
        return list
    }
}