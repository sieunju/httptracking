package com.hmju.httptracking

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

internal class MainActivity : AppCompatActivity() {

    private lateinit var httpTest: HttpTestProvider
    private lateinit var bleTest: BleTestProvider
    private lateinit var nfcTest: NfcTestProvider

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (!result.all { it.value }) {
            finishAffinity()
        }
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        httpTest = HttpTestProvider(this).onInit()
        bleTest = BleTestProvider(this)
        nfcTest = NfcTestProvider(this)

        findViewById<Button>(R.id.bFileUpload).setOnClickListener {
            httpTest.startFileUpload()
        }
        findViewById<Button>(R.id.bNfc).setOnClickListener { nfcTest.startTag() }

        findViewById<Button>(R.id.bHttp).setOnClickListener { httpTest.startHttpTest() }
        findViewById<Button>(R.id.bBleAdv).setOnClickListener { bleTest.startBleAdv() }
        findViewById<Button>(R.id.bBleConnect).setOnClickListener {
            bleTest.startConnection(
                macAddress = "11:22:33:44:55:66",
                findUuid = "00002a00-0000-1000-8000-00805f9b34fb" // GAP, Device Name (표준 UUID)
            )
        }

        permissionsLauncher.launch(getPermissions())
        onBackPressedDispatcher.addCallback(this, backPressCallback)
    }

    private fun getPermissions(): Array<String> {
        val list = mutableListOf<String>()
        list.add(Manifest.permission.BLUETOOTH_ADMIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        list.add(Manifest.permission.NFC)
        return list.toTypedArray()
    }
}