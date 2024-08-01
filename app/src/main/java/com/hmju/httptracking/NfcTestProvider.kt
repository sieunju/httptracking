package com.hmju.httptracking

import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.tracking.hardware.HardwareTrackingModel

class NfcTestProvider(
    private val activity: AppCompatActivity
) {

    private val adapter: NfcAdapter by lazy { NfcAdapter.getDefaultAdapter(activity) }

    private val callback = NfcAdapter.ReaderCallback { tag ->
        if (tag == null) return@ReaderCallback
        try {
            val ndef = Ndef.get(tag) ?: throw NullPointerException("Ndef is Null")
            if (!ndef.isConnected) {
                ndef.connect()
            }
            TrackingDataManager.getInstance().add(HardwareTrackingModel(ndef))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun startTag() {
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V
        val config = Bundle()
        config.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 20)
        adapter.enableReaderMode(activity, callback, flags, config)
    }
}