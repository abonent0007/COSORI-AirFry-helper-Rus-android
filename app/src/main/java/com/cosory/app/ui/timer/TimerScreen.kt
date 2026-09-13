package com.cosory.app.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.cosory.app.domain.model.CookingPlan
import com.cosory.app.ui.components.BulletRow
import com.cosory.app.ui.components.DeviceButtonChip
import com.cosory.app.ui.components.GlowButton
import com.cosory.app.ui.components.TimerRing
import com.cosory.app.ui.components.formatTime
import com.cosory.app.ui.components.glowTextStyle
import com.cosory.app.ui.components.pulseGlow
import com.cosory.app.ui.components.vibrate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    plan: CookingPlan?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Таймер") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        if (plan == null || plan.phases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("План не найден. Вернитесь в калькулятор.")
            }
            return@Scaffold
        }

        var phaseIndex by remember { mutableIntStateOf(0) }
        val safePhaseIndex = phaseIndex.coerceIn(0, plan.phases.lastIndex)
        val phase = plan.phases[safePhaseIndex]
        var remaining by remember(safePhaseIndex) { mutableIntStateOf(phase.timeMin * 60) }
        var running by remember { mutableStateOf(false) }
        val context = LocalContext.current

        LaunchedEffect(running, safePhaseIndex) {
            while (running && remaining > 0) {
                delay(1000L)
                remaining -= 1
            }
            if (running && remaining <= 0) {
                running = false
                vibrate(context)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Фаза ${phase.index} из ${phase.totalPhases}",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeviceButtonChip(
                    label = phase.mode.labelEn,
                    modifier = Modifier.pulseGlow(enabled = running, cornerRadius = 8.dp),
                    textStyle = MaterialTheme.typography.titleSmall,
                    horizontalPadding = 12.dp,
                    verticalPadding = 6.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = phase.mode.labelRu,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = "${phase.tempC} °C",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )

            Spacer(Modifier.height(20.dp))
            val totalSeconds = phase.timeMin * 60
            val fraction = if (totalSeconds > 0) remaining.toFloat() / totalSeconds else 0f
            TimerRing(
                fraction = fraction,
                diameter = 280.dp,
                strokeWidth = 10.dp,
            ) {
                Text(
                    text = formatTime(remaining),
                    style = glowTextStyle(MaterialTheme.typography.displayLarge),
                )
            }
            if (remaining == 0) {
                Text(
                    text = "Время вышло! Проверьте готовность.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowButton(onClick = { running = !running }) {
                    Text(if (running) "Пауза" else "Старт")
                }
                OutlinedButton(
                    onClick = {
                        running = false
                        remaining = phase.timeMin * 60
                    },
                ) {
                    Text("Сброс")
                }
            }

            if (phaseIndex < plan.phases.lastIndex) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { phaseIndex += 1 }) {
                    Text("Следующая фаза")
                }
            }

            if (phase.reminders.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Напоминания фазы", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    phase.reminders.forEach { reminder ->
                        BulletRow("На ${reminder.atMin}-й мин — ${reminder.text}")
                    }
                }
            }
        }
    }
}
