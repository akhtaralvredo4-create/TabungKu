
package com.tabungku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private val Green = Color(0xFF239B65)
private val Dark = Color(0xFF20352A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    val target = 300000L

    var amount by remember { mutableStateOf("") }

    var start by remember {
        mutableLongStateOf(prefs.getLong("start", System.currentTimeMillis()))
    }

    var history by remember {
        mutableStateOf(
            prefs.getString("history", "") ?: ""
        )
    }

    var celebrating by remember { mutableStateOf(false) }
    var completedAmount by remember { mutableLongStateOf(0L) }

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    val days = ((System.currentTimeMillis() - start) /
        86400000L).toInt().coerceAtLeast(0)

    val progress = (total.toFloat() / target).coerceIn(0f, 1f)

    LaunchedEffect(total) {
        if (total >= target && !celebrating) {
            completedAmount = total
            celebrating = true
            delay(5000)

            val record = "${System.currentTimeMillis()}|$completedAmount"
            history = listOf(history, record)
                .filter { it.isNotBlank() }
                .joinToString("\n")

            total = 0L
            start = System.currentTimeMillis()
            celebrating = false

            prefs.edit()
                .putLong("total", 0L)
                .putLong("start", start)
                .putString("history", history)
                .apply()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(primary = Green)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6FAF7))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "TabungKu 💰",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Dark
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE5F5EB)
                )
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL TABUNGAN")
                    Text(
                        formatter.format(total),
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green
                    )
                    Text("🐷", fontSize = 85.sp)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("${(progress * 100).toInt()}%")
                    Text("Target: ${formatter.format(target)}")
                }
            }

            Card {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Tabungan", fontSize = 20.sp)

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it.filter(Char::isDigit)
                        },
                        label = { Text("Nominal Rupiah") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val value = amount.toLongOrNull()
                            if (value != null && value > 0 &&
                                total <= Long.MAX_VALUE - value
                            ) {
                                total += value
                                prefs.edit().putLong("total", total).apply()
                                amount = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tambah Uang")
                    }
                }
            }

            Card {
                Column(Modifier.padding(18.dp)) {
                    Text("⏰ Pengingat")
                    Text("Setiap hari pukul 07.00 pagi")
                }
            }

            Card {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📅 Periode Menabung")
                    Text("Hari ke ${(days + 1).coerceAtMost(30)} dari 30")

                    LinearProgressIndicator(
                        progress = { (days / 30f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📚 Riwayat Tabungan", fontSize = 20.sp)

                    val records = history.split("\n")
                        .filter { it.isNotBlank() }
                        .reversed()

                    if (records.isEmpty()) {
                        Text("Belum ada riwayat tabungan.")
                    } else {
                        records.forEach { record ->
                            val parts = record.split("|")
                            if (parts.size == 2) {
                                Text(
                                    "🎉 ${formatter.format(parts[1].toLongOrNull() ?: 0L)}"
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        if (celebrating) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val colors = listOf(
                        Color.Red, Color.Blue,
                        Color.Yellow, Green, Color.Magenta
                    )
                    repeat(90) { i ->
                        val x = (i * 97f) % size.width
                        val y = (i * 137f) % size.height
                        drawCircle(
                            colors[i % colors.size],
                            radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }

                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("🎉 TARGET TERCAPAI!") },
                    text = {
                        Text(
                            "Hebat! Kamu berhasil menabung ${formatter.format(completedAmount)}!"
                        )
                    },
                    confirmButton = {}
                )
            }
        }
    }
}
