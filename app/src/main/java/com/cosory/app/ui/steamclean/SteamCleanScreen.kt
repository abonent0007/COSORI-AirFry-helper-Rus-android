package com.cosory.app.ui.steamclean

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cosory.app.data.dto.SteamCleanDto
import com.cosory.app.domain.model.CookingMode
import com.cosory.app.ui.components.DeviceButtonChip
import com.cosory.app.ui.components.DeviceButtonText
import com.cosory.app.ui.components.GlowButton
import com.cosory.app.ui.components.RichBulletRow
import com.cosory.app.ui.components.TimerRing
import com.cosory.app.ui.components.formatTime
import com.cosory.app.ui.components.glowTextStyle
import com.cosory.app.ui.components.vibrate
import kotlinx.coroutines.delay

private data class SteamPhase(val label: String, val seconds: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteamCleanScreen(
    steamClean: SteamCleanDto,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val modeLabel = remember(steamClean.mode) {
        CookingMode.entries.firstOrNull { it.name == steamClean.mode }?.labelEn ?: "Air Fry"
    }
    val phases = remember(steamClean.timeMin, steamClean.dwellMin) {
        listOf(
            SteamPhase("Пропаривание", steamClean.timeMin * 60),
            SteamPhase("Выдержка — не открывать", steamClean.dwellMin * 60),
        )
    }

    var phaseIndex by remember { mutableIntStateOf(0) }
    val phase = phases[phaseIndex.coerceIn(0, phases.lastIndex)]
    var remaining by remember(phaseIndex) { mutableIntStateOf(phase.seconds) }
    var running by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(running, phaseIndex) {
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
                title = { Text("Паровая очистка") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(text = steamClean.intro, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DeviceButtonChip(
                                label = modeLabel,
                                textStyle = MaterialTheme.typography.titleSmall,
                                horizontalPadding = 12.dp,
                                verticalPadding = 6.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${steamClean.tempC} °C · ${steamClean.timeMin} мин",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Вода: ${steamClean.waterMl} — только в жаростойкой чашке.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = steamClean.vinegar,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "После цикла — выдержка ${steamClean.dwellMin} мин, корзина закрыта.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = phase.label, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        TimerRing(
                            fraction = if (phase.seconds > 0) remaining.toFloat() / phase.seconds else 0f,
                            diameter = 220.dp,
                            strokeWidth = 9.dp,
                        ) {
                            Text(
                                text = formatTime(remaining),
                                style = glowTextStyle(MaterialTheme.typography.displayMedium),
                            )
                        }
                        if (remaining == 0) {
                            Text(
                                text = "Этап завершён.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GlowButton(onClick = { running = !running }) {
                                Text(if (running) "Пауза" else "Старт")
                            }
                            OutlinedButton(
                                onClick = {
                                    running = false
                                    remaining = phase.seconds
                                },
                            ) {
                                Text("Сброс")
                            }
                        }
                        if (phaseIndex == 0) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    running = false
                                    phaseIndex = 1
                                },
                            ) {
                                Text("Дальше: выдержка")
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Порядок действий", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        steamClean.steps.forEachIndexed { index, step ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                DeviceButtonText(text = step, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Почему это работает", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        steamClean.physics.forEach { RichBulletRow(it) }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Безопасность",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(4.dp))
                        steamClean.warnings.forEach {
                            Text(
                                text = "• $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
