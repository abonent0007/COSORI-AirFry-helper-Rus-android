package com.cosory.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cosory.app.CosoryApplication
import com.cosory.app.ui.calculator.CalculatorScreen
import com.cosory.app.ui.calculator.CalculatorViewModel
import com.cosory.app.ui.preheat.PreheatScreen
import com.cosory.app.ui.reference.ReferenceScreen
import com.cosory.app.ui.result.ResultScreen
import com.cosory.app.ui.steamclean.SteamCleanScreen
import com.cosory.app.ui.theme.Motion
import com.cosory.app.ui.timer.TimerScreen
import com.cosory.app.ui.welcome.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val CALCULATOR = "calculator"
    const val RESULT = "result"
    const val PREHEAT = "preheat"
    const val TIMER = "timer"
    const val REFERENCE = "reference"
    const val STEAM_CLEAN = "steam_clean"
}

@Composable
fun CosoryNavHost(
    startAtWelcome: Boolean,
    onWelcomeConfirmed: () -> Unit,
) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as CosoryApplication
    val viewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.factory(app.repository))
    val state by viewModel.state.collectAsState()
    val reference = remember { app.repository.reference }
    val enterMs = if (Motion.animationsEnabled()) Motion.NAV_ENTER_MS else 0
    val exitMs = if (Motion.animationsEnabled()) Motion.NAV_EXIT_MS else 0

    NavHost(
        navController = navController,
        startDestination = if (startAtWelcome) Routes.WELCOME else Routes.CALCULATOR,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 4 },
                animationSpec = tween(enterMs, easing = Motion.EmphasizedDecelerate),
            ) + fadeIn(tween(enterMs))
        },
        exitTransition = {
            fadeOut(tween(exitMs, easing = Motion.EmphasizedAccelerate)) +
                slideOutHorizontally(
                    targetOffsetX = { -it / 8 },
                    animationSpec = tween(exitMs, easing = Motion.EmphasizedAccelerate),
                )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(enterMs, easing = Motion.EmphasizedDecelerate),
            ) + fadeIn(tween(enterMs))
        },
        popExitTransition = {
            fadeOut(tween(exitMs, easing = Motion.EmphasizedAccelerate)) +
                slideOutHorizontally(
                    targetOffsetX = { it / 8 },
                    animationSpec = tween(exitMs, easing = Motion.EmphasizedAccelerate),
                )
        },
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                reference = reference,
                onContinue = {
                    onWelcomeConfirmed()
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CALCULATOR) {
            CalculatorScreen(
                state = state,
                onToggleProduct = viewModel::toggleProduct,
                onSetWeight = viewModel::setWeight,
                onToggleLook = viewModel::toggleLook,
                onCalculate = {
                    viewModel.calculate()
                    navController.navigate(Routes.RESULT)
                },
                onOpenReference = { navController.navigate(Routes.REFERENCE) },
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                plan = state.plan,
                onBack = { navController.popBackStack() },
                onStartTimer = {
                    if (state.plan?.preheat != null) {
                        navController.navigate(Routes.PREHEAT)
                    } else {
                        navController.navigate(Routes.TIMER)
                    }
                },
            )
        }
        composable(Routes.PREHEAT) {
            val preheat = state.plan?.preheat
            if (preheat != null) {
                PreheatScreen(
                    preheat = preheat,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Routes.TIMER) },
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.TIMER) {
                        popUpTo(Routes.RESULT)
                    }
                }
            }
        }
        composable(Routes.TIMER) {
            TimerScreen(
                plan = state.plan,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.REFERENCE) {
            ReferenceScreen(
                reference = reference,
                onBack = { navController.popBackStack() },
                onOpenSteamClean = { navController.navigate(Routes.STEAM_CLEAN) },
            )
        }
        composable(Routes.STEAM_CLEAN) {
            SteamCleanScreen(
                steamClean = reference.steamClean,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
