package com.cosory.app.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosory.app.domain.model.FinalLook
import com.cosory.app.domain.model.Product
import com.cosory.app.ui.components.DeviceButtonText
import com.cosory.app.ui.components.GlowButton
import com.cosory.app.ui.components.GroupHeader
import com.cosory.app.ui.components.StepHeader
import com.cosory.app.ui.components.ctaGlow
import com.cosory.app.ui.components.groupIcon
import com.cosory.app.ui.components.lookIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onToggleProduct: (Product) -> Unit,
    onSetWeight: (String, Int) -> Unit,
    onToggleLook: (FinalLook) -> Unit,
    onCalculate: () -> Unit,
    onOpenReference: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Калькулятор") },
                actions = {
                    IconButton(onClick = onOpenReference) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Справочник")
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { StepHeader("1", "Выберите продукты") }
            state.groups.forEach { (group, products) ->
                item(key = "group_${group.name}") { GroupHeader(group.labelRu, icon = group.groupIcon) }
                items(products, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        checked = state.selected.containsKey(product.id),
                        onToggle = onToggleProduct,
                    )
                }
            }

            if (state.selected.isNotEmpty()) {
                item { StepHeader("2", "Укажите вес") }
                items(state.selectedProducts, key = { "weight_${it.id}" }) { product ->
                    WeightRow(
                        product = product,
                        grams = state.selected.getValue(product.id),
                        onSetWeight = onSetWeight,
                    )
                }
            }

            item { StepHeader("3", "Желаемый результат") }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    FinalLook.entries.forEach { look ->
                        val selected = look in state.looks
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleLook(look) },
                            modifier = if (selected) Modifier.ctaGlow(cornerRadius = 8.dp) else Modifier,
                            label = { Text(look.labelRu) },
                            leadingIcon = {
                                Icon(
                                    imageVector = look.lookIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            },
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Выбрано продуктов: ${state.selected.size}, общий вес: ${state.totalWeightG} г",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state.totalWeightG > 1500) {
                    Text(
                        text = "Большой вес: корзину заполняйте не выше 2/3, возможно, готовить партиями.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(8.dp))
                GlowButton(
                    onClick = onCalculate,
                    enabled = state.selected.isNotEmpty(),
                    pulsing = state.selected.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Рассчитать режимы и время")
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    checked: Boolean,
    onToggle: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle(product) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle(product) })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.nameRu, style = MaterialTheme.typography.bodyLarge)
            DeviceButtonText(
                text = "${product.mode.labelEn} · ${product.mode.labelRu}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeightRow(
    product: Product,
    grams: Int,
    onSetWeight: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = product.nameRu,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            IconButton(
                onClick = { onSetWeight(product.id, grams - 50) },
                enabled = grams > CalculatorViewModel.MIN_WEIGHT,
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Меньше")
            }
            Text(
                text = "$grams г",
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = { onSetWeight(product.id, grams + 50) },
                enabled = grams < CalculatorViewModel.MAX_WEIGHT,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Больше")
            }
        }
    }
}
