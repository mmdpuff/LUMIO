package com.mmdpuff.lumio

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.UUID

@SuppressLint("MissingPermission")
class MainActivityg : ComponentActivity() {

    // 1. ESP32 information
    private val deviceName = "PY-RECEIVER"

    private val serviceId = UUID.fromString(
        "12345678-1234-5678-1234-56789abcdef0"
    )

    private val characteristicId = UUID.fromString(
        "12345678-1234-5678-1234-56789abcdef1"
    )

    // 2. Android Bluetooth objects
    private val adapter: BluetoothAdapter? by lazy {
        val manager = getSystemService(
            BLUETOOTH_SERVICE
        ) as BluetoothManager

        manager.adapter
    }

    private val handler = Handler(Looper.getMainLooper())

    private var gatt: BluetoothGatt? = null
    private var textCharacteristic: BluetoothGattCharacteristic? = null
    private var scanning = false

    // 3. Screen state
    private var status by mutableStateOf("هنوز متصل نیستیم")
    private var lastRead by mutableStateOf("-")
    private var ready by mutableStateOf(false)
    private var busy by mutableStateOf(false)

    // 4. Start the Android screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var message by remember {
                mutableStateOf("SALAM")
            }

            val permissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    if (hasPermissions()) {
                        startScan()
                    } else {
                        status = "مجوز Bluetooth داده نشد"
                    }
                }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {

                    Text(
                        "ESP32 Bluetooth",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (hasPermissions()) {
                                startScan()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                )
                            }
                        }
                    ) {
                        Text("1. Scan & Connect")
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Status: $status")

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { readFromEsp() },
                        enabled = ready && !busy
                    ) {
                        Text("2. Read from ESP32")
                    }

                    Text("Received: $lastRead")

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { sendToEsp(message) },
                        enabled = ready && !busy
                    ) {
                        Text("3. Send to ESP32")
                    }
                }
            }
        }
    }

    // 5. Check permissions
    private fun hasPermissions(): Boolean {
        val scanAllowed = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        val connectAllowed = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        return scanAllowed && connectAllowed
    }

    // 6. Search for ESP32
    private fun startScan() {
        if (!hasPermissions()) return

        val bt = adapter

        if (bt?.isEnabled != true) {
            status = "Bluetooth گوشی را روشن کن"
            return
        }

        val scanner = bt.bluetoothLeScanner

        if (scanner == null) {
            status = "BLE Scanner در دسترس نیست"
            return
        }

        gatt?.close()
        gatt = null
        textCharacteristic = null
        ready = false
        busy = false
        lastRead = "-"

        scanning = true
        status = "در حال جست‌وجوی ESP32..."

        scanner.startScan(scanCallback)

        handler.postDelayed({
            if (scanning) {
                stopScan()
                status = "ESP32 پیدا نشد"
            }
        }, 10_000L)
    }

    // 7. Receive scan results
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            val foundName = result.scanRecord?.deviceName

            if (foundName == deviceName) {
                runOnUiThread {
                    if (scanning) {
                        stopScan()
                        connectToEsp(result.device)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                scanning = false
                status = "Scan failed: $errorCode"
            }
        }
    }

    private fun stopScan() {
        if (!scanning) return

        scanning = false

        if (hasPermissions()) {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    // 8. Connect to ESP32
    private fun connectToEsp(device: BluetoothDevice) {
        status = "ESP32 پیدا شد؛ در حال اتصال..."

        gatt = device.connectGatt(
            this,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    // 9. Handle connection and data events
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            connection: BluetoothGatt,
            result: Int,
            newState: Int
        ) {
            if (
                result == BluetoothGatt.GATT_SUCCESS &&
                newState == BluetoothProfile.STATE_CONNECTED
            ) {
                runOnUiThread {
                    status = "متصل شد؛ در حال کشف سرویس‌ها..."
                }

                if (!connection.discoverServices()) {
                    runOnUiThread {
                        status = "شروع کشف سرویس‌ها ناموفق بود"
                    }
                }

            } else {
                connection.close()

                runOnUiThread {
                    if (gatt === connection) {
                        gatt = null
                        textCharacteristic = null
                        ready = false
                        busy = false
                        status = "ارتباط قطع شد یا اتصال ناموفق بود: $result"
                    }
                }
            }
        }

        override fun onServicesDiscovered(
            connection: BluetoothGatt,
            result: Int
        ) {
            val characteristic =
                if (result == BluetoothGatt.GATT_SUCCESS) {
                    connection.getService(serviceId)
                        ?.getCharacteristic(characteristicId)
                } else {
                    null
                }

            runOnUiThread {
                if (characteristic != null) {
                    textCharacteristic = characteristic
                    ready = true
                    status = "آمادهٔ Read و Write"
                } else {
                    status = "سرویس یا Characteristic پیدا نشد"
                }
            }
        }

        override fun onCharacteristicRead(
            connection: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            result: Int
        ) {
            showReadResult(value, result)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            connection: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            result: Int
        ) {
            showReadResult(
                characteristic.value ?: byteArrayOf(),
                result
            )
        }

        override fun onCharacteristicWrite(
            connection: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            result: Int
        ) {
            runOnUiThread {
                busy = false
                status = if (result == BluetoothGatt.GATT_SUCCESS) {
                    "پیام با موفقیت نوشته شد"
                } else {
                    "خطای Write: $result"
                }
            }
        }
    }

    // 10. Show the received text
    private fun showReadResult(
        data: ByteArray,
        result: Int
    ) {
        runOnUiThread {
            busy = false

            if (result == BluetoothGatt.GATT_SUCCESS) {
                lastRead = String(data, Charsets.UTF_8)
                status = "Read موفق بود"
            } else {
                status = "خطای Read: $result"
            }
        }
    }

    // 11. Read from ESP32
    private fun readFromEsp() {
        val connection = gatt ?: return
        val characteristic = textCharacteristic ?: return

        busy = true
        status = "در حال خواندن..."

        if (!connection.readCharacteristic(characteristic)) {
            busy = false
            status = "درخواست Read شروع نشد"
        }
    }

    // 12. Send text to ESP32
    @Suppress("DEPRECATION")
    private fun sendToEsp(text: String) {
        val connection = gatt ?: return
        val characteristic = textCharacteristic ?: return

        val data = text.toByteArray(Charsets.UTF_8)

        if (data.isEmpty() || data.size > 20) {
            status = "متن باید بین ۱ تا ۲۰ بایت باشد"
            return
        }

        busy = true
        status = "در حال ارسال..."

        val started = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            connection.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            characteristic.value = data
            characteristic.writeType =
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            connection.writeCharacteristic(characteristic)
        }

        if (!started) {
            busy = false
            status = "درخواست Write شروع نشد"
        }
    }

    // 13. Clean up
    override fun onDestroy() {
        stopScan()
        gatt?.close()
        gatt = null

        super.onDestroy()
    }
}
