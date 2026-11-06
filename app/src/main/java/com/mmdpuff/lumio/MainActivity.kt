package com.mmdpuff.lumio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mmdpuff.lumio.ui.theme.ColorBox
import com.mmdpuff.lumio.ui.theme.FontRobotoMono
import com.mmdpuff.lumio.ui.theme.LUMIOTheme

data class MyColor(
    val name: String,
    val color: Color
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

                                        .background(color = Color(0xff6CE3B1))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ESP32 ONLINE",
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
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
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
                    Text("Bluetooth connected", fontSize = 10.sp, color = Color(0xff9398AD))

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
                        ), contentAlignment = Alignment.Center

                ) {
                    Text(
                        "DISCONNECT",
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
                            Text(item.name, fontSize = 9.sp, color = Color(0xff9398AD))

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
                            tint = Color(0xff9398AD), modifier = Modifier.fillMaxSize()
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
                                letterSpacing = 0.6.sp, color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))

                            Text(
                                "· GPIO 2",
                                fontFamily = FontRobotoMono,
                                fontSize = 9.sp,
                                letterSpacing = 0.6.sp, color = Color(0xff666E87)
                            )
                        }

                    }
                }

                Box(
                    Modifier
                        .size(50.dp)

                        .clip(shape = CircleShape)
                        .background(Color(0xff171927))
                        .border(
                            width = 1.dp,
                            color = Color(0xffAD82FF).copy(alpha = 0.1961f),
                            shape = CircleShape
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