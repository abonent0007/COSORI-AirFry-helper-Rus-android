package com.cosory.app.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosory.app.data.CookingRepository
import com.cosory.app.domain.CookingCalculator
import com.cosory.app.domain.model.CookingItem
import com.cosory.app.domain.model.CookingPlan
import com.cosory.app.domain.model.CookingRequest
import com.cosory.app.domain.model.FinalLook
import com.cosory.app.domain.model.Product
import com.cosory.app.domain.model.ProductGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    val groups: List<Pair<ProductGroup, List<Product>>> = emptyList(),
    val selected: Map<String, Int> = emptyMap(),
    val looks: Set<FinalLook> = emptySet(),
    val plan: CookingPlan? = null,
) {
    val selectedProducts: List<Product>
        get() = groups.flatMap { it.second }.filter { selected.containsKey(it.id) }

    val totalWeightG: Int
        get() = selected.values.sum()
}

class CalculatorViewModel(private val repository: CookingRepository) : ViewModel() {

    private val calculator = CookingCalculator(repository.modes)

    private val _state = MutableStateFlow(
        CalculatorUiState(groups = repository.groupedProducts.map { it.key to it.value }),
    )
    val state: StateFlow<CalculatorUiState> = _state

    fun toggleProduct(product: Product) = _state.update { current ->
        val selected = current.selected.toMutableMap()
        if (selected.containsKey(product.id)) {
            selected.remove(product.id)
        } else {
            selected[product.id] = product.baseWeightG
        }
        current.copy(selected = selected, plan = null)
    }

    fun setWeight(productId: String, grams: Int) = _state.update { current ->
        val selected = current.selected.toMutableMap()
        if (selected.containsKey(productId)) {
            selected[productId] = grams.coerceIn(MIN_WEIGHT, MAX_WEIGHT)
        }
        current.copy(selected = selected, plan = null)
    }

    fun toggleLook(look: FinalLook) = _state.update { current ->
        val looks = current.looks.toMutableSet()
        if (!looks.add(look)) looks.remove(look)
        current.copy(looks = looks, plan = null)
    }

    fun calculate() = _state.update { current ->
        val items = current.selectedProducts.map { CookingItem(it, current.selected.getValue(it.id)) }
        current.copy(
            plan = if (items.isEmpty()) null else calculator.calculate(CookingRequest(items, current.looks)),
        )
    }

    fun reset() = _state.update { it.copy(selected = emptyMap(), looks = emptySet(), plan = null) }

    companion object {
        const val MIN_WEIGHT = 50
        const val MAX_WEIGHT = 3000

        fun factory(repository: CookingRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CalculatorViewModel(repository) }
        }
    }
}
