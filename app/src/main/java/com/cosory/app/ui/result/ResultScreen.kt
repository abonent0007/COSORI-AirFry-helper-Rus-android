package com.cosory.app.ui.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosory.app.domain.model.CookingPhase
import com.cosory.app.domain.model.CookingPlan
import com.cosory.app.domain.model.PreheatInfo
import com.cosory.app.domain.model.ProductEstimate
import com.cosory.app.ui.components.Badge
import com.cosory.app.ui.components.BulletRow
import com.cosory.app.ui.components.DeviceButtonChip
import com.cosory.app.ui.components.DeviceButtonText
import com.cosory.app.ui.components.GlowButton
import com.cosory.app.ui.components.RichBulletRow
import com.cosory.app.ui.components.ctaGlow
import com.cosory.app.ui.components.glowTextStyle
import com.cosory.app.ui.theme.Motion
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    plan: CookingPlan?,
    onBack: () -> Unit,
    onStartTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("План готовки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        if (plan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Сначала выберите продукты в калькуляторе")
            }
        } else {
            val phaseCount = plan.phases.size
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { StaggeredAppear(0) { SummaryCard(plan) } }
                plan.preheat?.let { preheat ->
                    item { StaggeredAppear(1) { PreheatCard(preheat) } }
                }
                itemsIndexed(plan.phases, key = { _, phase -> "${phase.index}_${phase.mode.name}" }) { index, phase ->
                    StaggeredAppear(2 + index) { PhaseCard(phase) }
                }
                if (plan.rationale.isNotEmpty()) {
                    item { StaggeredAppear(2 + phaseCount) { RationaleCard(plan.rationale) } }
                }
                item { StaggeredAppear(3 + phaseCount) { PerProductCard(plan.perProduct) } }
                if (plan.warnings.isNotEmpty()) {
                    item { StaggeredAppear(4 + phaseCount) { WarningsCard(plan.warnings) } }
                }
                item { StaggeredAppear(5 + phaseCount) { TipsCard(plan.tips) } }
                item {
                    StaggeredAppear(6 + phaseCount) {
                        GlowButton(
                            onClick = onStartTimer,
                            pulsing = true,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Начать готовку")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredAppear(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val delayMs = minOf(index, 6) * Motion.STAGGER_MS
        if (Motion.animationsEnabled() && delayMs > 0) {
            delay(delayMs.toLong())
        }
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.APPEAR_MS)) +
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(Motion.APPEAR_MS, easing = Motion.EmphasizedDecelerate),
            ),
    ) {
        content()
    }
}

@Composable
private fun SummaryCard(plan: CookingPlan, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Приготовление ~ ${plan.cookingTimeMin} мин",
                style = glowTextStyle(
                    base = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    alpha = 0.45f,
                    blur = 8f,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            if (plan.preheat != null) {
                Text(
                    text = "Плюс прогрев ${plan.preheat.tempC} °C · ${plan.preheat.timeMin} мин (кнопка Preheat)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            val weight = plan.perProduct.sumOf { it.weightG }
            Text(
                text = "Продуктов: ${plan.perProduct.size} · общий вес: $weight г",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PreheatCard(preheat: PreheatInfo, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Badge("ПОДГОТОВКА")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeviceButtonChip(
                    label = "Preheat",
                    textStyle = MaterialTheme.typography.titleSmall,
                    horizontalPadding = 12.dp,
                    verticalPadding = 6.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${preheat.tempC} °C · ${preheat.timeMin} мин",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Нажмите кнопку, дождитесь звукового сигнала и только потом закладывайте продукты.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PhaseCard(phase: CookingPhase, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Badge(if (phase.isBrowning) "ПОДРУМЯНИВАНИЕ" else "ФАЗА ${phase.index} ИЗ ${phase.totalPhases}")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeviceButtonChip(
                    label = phase.mode.labelEn,
                    modifier = Modifier.ctaGlow(cornerRadius = 8.dp),
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
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${phase.tempC} °C · ${phase.timeMin} мин",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (phase.fanSpeed > 0) {
                Text(
                    text = "Обдув: ${phase.fanSpeed}/5 (подбирается автоматически)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            DeviceButtonText(text = phase.instruction, style = MaterialTheme.typography.bodyMedium)
            if (phase.reminders.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                phase.reminders.forEach { reminder ->
                    BulletRow("На ${reminder.atMin}-й мин — ${reminder.text}")
                }
            }
        }
    }
}

@Composable
private fun RationaleCard(rationale: List<String>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Почему такое время", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            rationale.forEach { RichBulletRow(it) }
        }
    }
}

@Composable
private fun PerProductCard(estimates: List<ProductEstimate>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Оценка по продуктам", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val longest = estimates.maxByOrNull { it.timeMin }
            estimates.forEach { estimate ->
                val marker = if (estimate === longest && estimates.size > 1) " ← ориентир" else ""
                DeviceButtonText(
                    text = "${estimate.nameRu} — ${estimate.weightG} г · " +
                        "${estimate.mode.labelEn} ${estimate.tempC} °C · ~${estimate.timeMin} мин$marker",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun WarningsCard(warnings: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Внимание",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(4.dp))
            warnings.forEach {
                Text(
                    text = "• $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun TipsCard(tips: List<String>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Советы", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            tips.forEach { RichBulletRow(it) }
        }
    }
}
