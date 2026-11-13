package com.tabungku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

private val Green = Color(0xFF239B65)
private val LightGreen = Color(0xFFE5F5EB)
private val Dark = Color(0xFF20352A)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
if (android.os.Build.VERSION.SDK_INT >= 33) {
    if (checkSelfPermission(
            android.Manifest.permission.POST_NOTIFICATIONS
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            101
        )
    }
}
        createNotificationChannel(this)
        scheduleDailyAlarm(this)

        setContent {
            TabungKuApp()
        }
    }
}

@Composable
fun TabungKuApp() {

    val context = androidx.compose.ui.platform.LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("tabungku", 0)
    }

    var total by remember {
        mutableLongStateOf(prefs.getLong("total", 0L))
    }

    var target by remember {
        mutableLongStateOf(prefs.getLong("target", 300000L))
    }

    var amount by remember {
        mutableStateOf("")
    }

    var startDate by remember {
        mutableLongStateOf(
            prefs.getLong("start", 0L)
        )
    }

    var celebrated by remember {
        mutableStateOf(prefs.getBoolean("celebrated", false))
    }

    var showCelebration by remember {
        mutableStateOf(false)
    }

    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L

    if (startDate == 0L) {
        startDate = now
        prefs.edit().putLong("start", startDate).apply()
    }

    val daysPassed = ((now - startDate) / day).toInt()
    val finished = daysPassed >= 30

    LaunchedEffect(finished) {
        if (finished && !celebrated) {
            showCelebration = true
            celebrated = true
            prefs.edit().putBoolean("celebrated", true).apply()
        }
    }

    val formatter = remember {
        NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        )
    }

    val progress = if (target > 0) {
        (total.toFloat() / target).coerceIn(0f, 1f)
    } else 0f

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            background = Color(0xFFF6FAF7),
            surface = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6FAF7))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    "TabungKu 💰",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Dark
                )

                Text(
                    "Sedikit demi sedikit, lama-lama menjadi bukit!",
                    color = Color.Gray
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = LightGreen
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("TOTAL TABUNGAN", color = Dark)

                        Text(
                            formatter.format(total),
                            fontSize = 29.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )

                        Spacer(Modifier.height(12.dp))

                        Text("🐷", fontSize = 90.sp)

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Green,
                            trackColor = Color.White
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "${(progress * 100).toInt()}% dari target",
                            color = Dark
                        )

                        Text(
                            "Target: ${formatter.format(target)}",
                            color = Dark
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "Tambah Tabungan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it.filter { c -> c.isDigit() }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nominal (Rupiah)") },
                            leadingIcon = {
                                Icon(Icons.Default.Payments, null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val value = amount.toLongOrNull()

                                if (value != null &&
                                    value > 0 &&
                                    total <= Long.MAX_VALUE - value
                                ) {
                                    total += value
                                    prefs.edit()
                                        .putLong("total", total)
                                        .apply()
                                    amount = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green
                            )
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Uang")
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "⏰ Pengingat Menabung",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text("Alarm harian pukul 07.00 pagi")
                        Text(
                            "Pastikan notifikasi TabungKu diizinkan."
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📅 Perjalanan 30 Hari",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text(
                            if (finished) "Periode selesai! 🎉"
                            else "Hari ke ${daysPassed + 1} dari 30"
                        )

                        LinearProgressIndicator(
                            progress = {
                                (daysPassed / 30f).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = Green
                        )

                        if (finished) {
                            Text(
                                "Total berhasil ditabung: ${formatter.format(total)}",
                                color = Green,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = {
                                    startDate = System.currentTimeMillis()
                                    celebrated = false
                                    showCelebration = false
                                    prefs.edit()
                                        .putLong("start", startDate)
                                        .putBoolean("celebrated", false)
                                        .apply()
                                }
                            ) {
                                Text("Mulai Periode Baru")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            if (showCelebration) {
                ConfettiOverlay()

                AlertDialog(
                    onDismissRequest = {
                        showCelebration = false
                    },
                    title = { Text("🎉 Selamat!") },
                    text = {
                        Text(
                            "Kamu berhasil menabung selama 30 hari!\n\nTotal: ${formatter.format(total)}"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCelebration = false
                            }
                        ) {
                            Text("Hebat!")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ConfettiOverlay() {

    val transition = rememberInfiniteTransition(
        label = "confetti"
    )

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {

        val colors = listOf(
            Color.Red,
            Color.Blue,
            Color.Yellow,
            Color.Green,
            Color.Magenta
        )

        repeat(70) { i ->
            val x = (i * 97f) % size.width
            val y = (
                (progress * size.height * 1.5f) +
                (i * 31f)
            ) % size.height

            drawCircle(
                color = colors[i % colors.size],
                radius = 5f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
