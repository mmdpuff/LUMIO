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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mmdpuff.lumio.ui.theme.ColorBox
import com.mmdpuff.lumio.ui.theme.FontRobotoMono
import com.mmdpuff.lumio.ui.theme.LUMIOTheme
import java.util.UUID

data class MyColor(
    val name: String,
    val color: Color
)

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    private val deviceName = "Lumio-ESP32"

    private val serviceId = UUID.fromString(
        "12345678-1234-5678-1234-567812345678"
    )

    private val characteristicId = UUID.fromString(
        "12345678-1234-5678-1234-567812345677"
    )
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
    private var status by mutableStateOf("Bluetooth Disconnected")
    private var lastRead by mutableStateOf("-")
    private var ready by mutableStateOf(false)
    private var busy by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                        status = "primision not allow"
                    }
                }

            LUMIOTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "LUMIO",
                                    fontSize = 25.sp,
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },

                            actions = {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(shape = CircleShape)

                                        .background(
                                            color = if (status == "Bluetooth connected") Color(
                                                0xff6CE3B1
                                            ) else Color.Red
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ESP32 ${if (status == "Bluetooth connected") "Online" else "Offline"}",
                                    fontFamily = FontRobotoMono,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp, color = Color(0xff9398AD)
                                )
                                Spacer(Modifier.width(10.dp))

                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )

                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(25.dp, 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            Modifier
//                .width(350.dp)
                                .height(76.dp)
                                .clip(shape = RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xffFFFFFF).copy(alpha = .07f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(ColorBox)
                        ) {
                            Row(
                                Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Box(
                                    Modifier
                                        .size(42.dp)
                                        .clip(shape = RoundedCornerShape(12.dp))
                                        .background(Color(0xffAD82FF).copy(alpha = .07f))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.bluetooth_on_svgrepo_com),
                                        contentDescription = null,
                                        tint = Color(0xffAD82FF), modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(
                                    Modifier
                                        .width(166.dp)
                                        .height(202.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        "Living room",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xffF5F5FC)
                                    )
                                    Text(
                                        "$status",
                                        fontSize = 10.sp,
                                        color = Color(0xff9398AD)
                                    )

                                }
                                Box(
                                    Modifier
                                        .width(94.dp)
                                        .height(34.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .background(Color(0xffFFFFFF).copy(alpha = 0.0235f))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.1098f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = true, onClick = {
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
                                        }), contentAlignment = Alignment.Center

                                ) {
                                    Text(
                                        "${if (status == "Bluetooth connected") "Connect" else "Disconncet"}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = Color(0xffF5F5FC)
                                    )
                                }

                            }

                        }
                        var color by remember {
                            mutableStateOf(Color(0xFF9B55F6))
                        }
                        val red = (color.red * 255).toInt()
                        val green = (color.green * 255).toInt()
                        val blue = (color.blue * 255).toInt()

                        val hex = "#%02X%02X%02X".format(red, green, blue)

                        Column {
                            Text(
                                "Make it your mood.",
                                fontSize = 16.sp,
                                color = Color(0xffF5F5FC),
                                fontWeight = FontWeight.Medium
                            )

                            ColorPicker(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                initialColor = color,
                                onColorChanged = {
                                    color = it
                                }
                            )

                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    Modifier
                                        .size(15.dp)
                                        .clip(shape = RoundedCornerShape(5.dp))
                                        .background(color)
                                )
                                Spacer(Modifier.width(5.dp))



                                Text(
                                    "${hex}",
                                    color = Color(0xffF5F5FC),
                                    fontFamily = FontRobotoMono, fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = " | RGB $red . $green . $blue",
                                    color = Color(0xff9398AD),
                                    fontFamily = FontRobotoMono,
                                    fontWeight = FontWeight.Normal, fontSize = 12.sp
                                )

                            }
                        }
                        val colors = listOf(
                            MyColor("Red", Color(0xfffa5758)),
                            MyColor("Blue", Color(0xff5787fa)),
                            MyColor("Green", Color(0xff57fa61)),
                            MyColor("Purple", Color(0xffa657fa)),
                            MyColor("Cyan", Color(0xff57fae2)),
                            MyColor("Yellow", Color(0xffcbfa57)),
                        )

                        Column {
                            Text(
                                "Quick colors",
                                fontSize = 11.sp,
                                color = Color(0xff9398AD),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                colors.forEach { item ->

                                    Box(
                                        modifier = Modifier
                                            .width(51.67.dp)
                                            .height(56.dp)
                                    ) {
                                        Column(
                                            Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(38.dp)
                                                    .border(
                                                        1.dp,
                                                        Color(0xffFFFFFF).copy(alpha = .2f),
                                                        shape = CircleShape
                                                    )
                                                    .padding(5.dp)

                                            ) {
                                                Box(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(shape = CircleShape)
                                                        .background(item.color)
                                                )
                                            }
                                            Text(
                                                item.name,
                                                fontSize = 9.sp,
                                                color = Color(0xff9398AD)
                                            )

                                        }
                                    }

                                }
                            }


                        }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(shape = RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xffFFFFFF).copy(alpha = .07f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(ColorBox)
                        ) {
                            Row(
                                Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(38.dp)
                                            .clip(shape = RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.board),
                                            contentDescription = null,
                                            tint = Color(0xff9398AD),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))

                                    Column(
                                        Modifier
                                            .width(166.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            "Onboard LED",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xffF5F5FC)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(shape = CircleShape)

                                                    .background(color = MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "ON",
                                                fontFamily = FontRobotoMono,
                                                fontSize = 10.sp,
                                                letterSpacing = 0.6.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(4.dp))

                                            Text(
                                                "· GPIO 2 . $message",
                                                fontFamily = FontRobotoMono,
                                                fontSize = 9.sp,
                                                letterSpacing = 0.6.sp, color = Color(0xff666E87)
                                            )
                                        }

                                    }
                                }
                                var light by remember { mutableStateOf(0) }

                                Box(
                                    Modifier
                                        .size(50.dp)

                                        .clip(shape = CircleShape)
                                        .background(Color(0xff171927))
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xffAD82FF).copy(alpha = 0.1961f),
                                            shape = CircleShape
                                        )
                                        .clickable(
                                            onClick = {
                                                if (light == 1) {
                                                    sendToEsp("0")
                                                    light = 0
                                                } else {

                                                    sendToEsp("1")
                                                    light = 1
                                                }


                                            },
                                            enabled = ready && !busy
                                        ), contentAlignment = Alignment.Center

                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)

                                            .clip(shape = CircleShape)
                                            .background(Color(0xffAD82FF).copy(alpha = 0.102f))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xffAD82FF).copy(alpha = 0.33f),
                                                shape = CircleShape
                                            )
                                            .padding(3.dp), contentAlignment = Alignment.Center

                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.power),
                                            contentDescription = null,
                                        )
                                    }

                                }

                            }
                        }


                    }
                }
            }
        }
    }

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun startScan() {
        if (!hasPermissions()) return

        val bt = adapter

        if (bt?.isEnabled != true) {
            status = "Trun on bloototh"
            return
        }

        val scanner = bt.bluetoothLeScanner

        if (scanner == null) {
            status = "BLE Scanner not available"
            return
        }

        gatt?.close()
        gatt = null
        textCharacteristic = null
        ready = false
        busy = false
        lastRead = "-"

        scanning = true
        status = "Searching..."

        scanner.startScan(scanCallback)

        handler.postDelayed({
            if (scanning) {
                stopScan()
                status = "ESP32 nor found"
            }
        }, 10_000L)
    }

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

    private fun connectToEsp(device: BluetoothDevice) {
        status = "ESP32 Found..."

        gatt = device.connectGatt(
            this,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

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
                    status = "Connect;found servies..."
                }

                if (!connection.discoverServices()) {
                    runOnUiThread {
//                        status = "شروع کشف سرویس‌ها ناموفق بود"
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
                        status = "disconnect: $result"
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
                    status = "Bluetooth connected"
                } else {
                    status = "Cants found Characteristic "
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

@Composable
fun MainScreen(modifier: Modifier = Modifier) {


}