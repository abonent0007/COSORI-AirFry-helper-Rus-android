package com.cosory.app.domain.model

data class CookingItem(
    val product: Product,
    val weightG: Int,
)

data class CookingRequest(
    val items: List<CookingItem>,
    val looks: Set<FinalLook> = emptySet(),
)

data class Reminder(
    val atMin: Int,
    val text: String,
)

data class CookingPhase(
    val index: Int,
    val totalPhases: Int,
    val mode: CookingMode,
    val tempC: Int,
    val timeMin: Int,
    val fanSpeed: Int,
    val productNames: List<String>,
    val instruction: String,
    val reminders: List<Reminder>,
    val isBrowning: Boolean = false,
)

data class ProductEstimate(
    val nameRu: String,
    val weightG: Int,
    val mode: CookingMode,
    val tempC: Int,
    val timeMin: Int,
)

data class PreheatInfo(
    val tempC: Int = 205,
    val timeMin: Int = 4,
)

data class CookingPlan(
    val preheat: PreheatInfo?,
    val phases: List<CookingPhase>,
    val totalTimeMin: Int,
    val perProduct: List<ProductEstimate>,
    val warnings: List<String>,
    val tips: List<String>,
    val rationale: List<String> = emptyList(),
) {
    val cookingTimeMin: Int
        get() = phases.sumOf { it.timeMin }
}
