package com.cosory.app.ui.preheat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cosory.app.domain.model.PreheatInfo
import com.cosory.app.ui.components.BulletRow
import com.cosory.app.ui.components.DeviceButtonChip
import com.cosory.app.ui.components.GlowButton
import com.cosory.app.ui.components.TimerRing
import com.cosory.app.ui.components.formatTime
import com.cosory.app.ui.components.glowTextStyle
import com.cosory.app.ui.components.vibrate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreheatScreen(
    preheat: PreheatInfo,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var remaining by remember { mutableIntStateOf(preheat.timeMin * 60) }
    var running by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000L)
            remaining -= 1
        }
        if (running && remaining <= 0) {
            running = false
            vibrate(context)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Прогрев") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Сначала прогрейте аэрогриль",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DeviceButtonChip(
                            label = "Preheat",
                            textStyle = MaterialTheme.typography.titleMedium,
                            horizontalPadding = 16.dp,
                            verticalPadding = 8.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${preheat.tempC} °C · ${preheat.timeMin} мин",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Нажмите кнопку, дождитесь звукового сигнала и только потом закладывайте продукты.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(8.dp))
                    BulletRow("Корзина должна быть пустой.")
                    BulletRow("Без прогрева продукты готовятся неравномерно и могут получиться сухими.")
                    BulletRow("Если готовите выпечку, прогрев особенно важен.")
                }
            }

            Spacer(Modifier.height(20.dp))
            val totalSeconds = preheat.timeMin * 60
            val fraction = if (totalSeconds > 0) remaining.toFloat() / totalSeconds else 0f
            TimerRing(
                fraction = fraction,
                diameter = 240.dp,
                strokeWidth = 9.dp,
            ) {
                Text(
                    text = formatTime(remaining),
                    style = glowTextStyle(MaterialTheme.typography.displayMedium),
                )
            }
            if (remaining == 0) {
                Text(
                    text = "Прогрев завершён — закладывайте продукты.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowButton(onClick = { running = !running }) {
                    Text(if (running) "Пауза" else "Запустить отсчёт")
                }
                OutlinedButton(
                    onClick = {
                        running = false
                        remaining = preheat.timeMin * 60
                    },
                ) {
                    Text("Сброс")
                }
            }

            Spacer(Modifier.weight(1f))
            GlowButton(
                onClick = onContinue,
                pulsing = true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Я прогрел(а) — продолжить")
            }
        }
    }
}
