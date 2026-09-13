package com.cosory.app.domain

import com.cosory.app.domain.model.CookingMode

enum class ModeFamily {
    CRISP,
    OVEN,
    DRY,
    PROOF,
    WARM,
}

object CookingRules {
    const val MIN_WEIGHT_G = 50
    const val MAX_WEIGHT_G = 3000

    const val MIN_TIME_MIN = 3
    const val MAX_TIME_MIN = 90
    const val MIN_TEMP_C = 30

    const val TEMP_TOLERANCE = 15
    const val ADD_LATER_RATIO = 0.6
    const val COMFORT_WEIGHT_G = 1500

    const val PREHEAT_TEMP_C = 205
    const val PREHEAT_TIME_MIN = 4

    const val LOAD_EXTRA_PER_KG = 0.10
    const val MAX_LOAD_FACTOR = 1.25

    const val MAX_TIME_FACTOR = 1.15

    const val JUICY_TEMP_DELTA = -5
    const val JUICY_TIME_FACTOR = 1.05

    const val DEEP_TIME_FACTOR = 1.10

    const val TENDER_TEMP_DELTA = -10
    const val TENDER_TIME_FACTOR = 1.10

    const val CRISPY_FROZEN_EXTRA_MIN = 1

    const val BROWNING_TEMP_C = 230
    const val BROWNING_TIME_MIN = 2
    const val BROWNING_TIME_CRISPY_MIN = 3

    const val CHEESE_AT_END_MIN = 3
    const val CHECK_EARLY_MIN = 2

    fun family(mode: CookingMode): ModeFamily = when (mode) {
        CookingMode.AIR_FRY, CookingMode.GRILL, CookingMode.FROZEN -> ModeFamily.CRISP
        CookingMode.BAKE, CookingMode.ROAST, CookingMode.REHEAT, CookingMode.PREHEAT -> ModeFamily.OVEN
        CookingMode.DEHYDRATE -> ModeFamily.DRY
        CookingMode.PROOF -> ModeFamily.PROOF
        CookingMode.KEEP_WARM -> ModeFamily.WARM
    }

    fun needsPreheat(mode: CookingMode): Boolean = when (mode) {
        CookingMode.AIR_FRY, CookingMode.ROAST, CookingMode.BAKE, CookingMode.GRILL, CookingMode.FROZEN -> true
        else -> false
    }
}
