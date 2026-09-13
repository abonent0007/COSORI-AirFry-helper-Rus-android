package com.cosory.app.ui.theme

import android.animation.ValueAnimator
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object Motion {
    const val NAV_ENTER_MS = 280
    const val NAV_EXIT_MS = 200
    const val APPEAR_MS = 220
    const val STAGGER_MS = 40
    const val PULSE_MS = 1600
    const val PRESS_MS = 120

    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun animationsEnabled(): Boolean = ValueAnimator.areAnimatorsEnabled()
}
