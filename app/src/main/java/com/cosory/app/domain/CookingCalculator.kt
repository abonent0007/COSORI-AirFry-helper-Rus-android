package com.cosory.app.domain

import com.cosory.app.domain.model.CookingItem
import com.cosory.app.domain.model.CookingMode
import com.cosory.app.domain.model.CookingPhase
import com.cosory.app.domain.model.CookingPlan
import com.cosory.app.domain.model.CookingRequest
import com.cosory.app.domain.model.FinalLook
import com.cosory.app.domain.model.ModeInfo
import com.cosory.app.domain.model.PreheatInfo
import com.cosory.app.domain.model.Product
import com.cosory.app.domain.model.ProductEstimate
import com.cosory.app.domain.model.Reminder
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class CookingCalculator(
    private val modes: Map<CookingMode, ModeInfo> = emptyMap(),
) {

    fun calculate(request: CookingRequest): CookingPlan {
        require(request.items.isNotEmpty()) { "Выберите хотя бы один продукт" }

        val adjusted = request.items.map { adjust(it, request.looks) }
        val clusters = cluster(adjusted)

        val cookPhases = clusters.map { buildCookPhase(it, request.looks) }
        val browning = buildBrowningPhase(adjusted, request.looks)
        val rawPhases = if (browning != null) cookPhases + browning else cookPhases

        val total = rawPhases.size
        val phases = rawPhases.mapIndexed { i, phase ->
            phase.copy(index = i + 1, totalPhases = total).withInstruction()
        }

        val preheat = if (adjusted.any { CookingRules.needsPreheat(it.product.mode) }) PreheatInfo() else null
        val totalTime = (preheat?.timeMin ?: 0) + phases.sumOf { it.timeMin }

        return CookingPlan(
            preheat = preheat,
            phases = phases,
            totalTimeMin = totalTime,
            perProduct = adjusted.map {
                ProductEstimate(
                    nameRu = it.product.nameRu,
                    weightG = it.weightG,
                    mode = it.product.mode,
                    tempC = it.tempC,
                    timeMin = it.timeMin,
                )
            },
            warnings = warnings(adjusted, clusters.size),
            tips = tips(adjusted),
            rationale = buildRationale(adjusted, clusters, phases),
        )
    }

    private fun adjust(item: CookingItem, looks: Set<FinalLook>): Adjusted {
        val product = item.product
        val weight = item.weightG.coerceIn(CookingRules.MIN_WEIGHT_G, CookingRules.MAX_WEIGHT_G)

        var temp = product.baseTempC
        var timeFactor = 1.0

        if (FinalLook.JUICY_INSIDE in looks) {
            temp += CookingRules.JUICY_TEMP_DELTA
            timeFactor = maxOf(timeFactor, CookingRules.JUICY_TIME_FACTOR)
        }
        if (FinalLook.DEEP_BAKED in looks) {
            timeFactor = maxOf(timeFactor, CookingRules.DEEP_TIME_FACTOR)
        }
        if (FinalLook.TENDER in looks) {
            temp += CookingRules.TENDER_TEMP_DELTA
            timeFactor = maxOf(timeFactor, CookingRules.TENDER_TIME_FACTOR)
        }
        timeFactor = timeFactor.coerceAtMost(CookingRules.MAX_TIME_FACTOR)

        var time = product.baseTimeMin * (weight.toDouble() / product.baseWeightG).pow(product.timeExponent)
        time *= timeFactor
        if (FinalLook.CRISPY in looks && product.mode == CookingMode.FROZEN) {
            time += CookingRules.CRISPY_FROZEN_EXTRA_MIN
        }

        time = time.coerceIn(CookingRules.MIN_TIME_MIN.toDouble(), CookingRules.MAX_TIME_MIN.toDouble())

        return Adjusted(
            product = product,
            weightG = weight,
            rawTimeMin = time,
            timeMin = time.roundToInt(),
            tempC = clampTemp(product.mode, temp),
        )
    }

    private fun cluster(items: List<Adjusted>): List<List<Adjusted>> {
        val sorted = items.sortedByDescending { it.rawTimeMin }
        val clusters = mutableListOf<MutableList<Adjusted>>()
        for (item in sorted) {
            val target = clusters.firstOrNull { cluster ->
                val anchor = cluster.first()
                CookingRules.family(anchor.product.mode) == CookingRules.family(item.product.mode) &&
                    abs(anchor.tempC - item.tempC) <= CookingRules.TEMP_TOLERANCE
            }
            if (target != null) target += item else clusters += mutableListOf(item)
        }
        return clusters
    }

    private fun buildCookPhase(cluster: List<Adjusted>, looks: Set<FinalLook>): CookingPhase {
        val anchor = cluster.first()
        val totalWeight = cluster.sumOf { it.weightG }
        val rawPhaseTime = anchor.rawTimeMin * loadFactor(totalWeight)
        val phaseTime = rawPhaseTime.roundToInt().coerceAtLeast(CookingRules.MIN_TIME_MIN)
        val half = (phaseTime / 2).coerceAtLeast(1)

        val reminders = mutableListOf<Reminder>()

        cluster.forEach { item ->
            if (item.product.turnOver) {
                reminders += Reminder(half, "Перевернуть: ${item.product.nameRu}")
            }
            if (item.product.shake) {
                reminders += Reminder(half, "Встряхнуть корзину: ${item.product.nameRu}")
            }
            if (item.rawTimeMin < anchor.rawTimeMin * CookingRules.ADD_LATER_RATIO) {
                val addAt = (phaseTime - item.timeMin).coerceAtLeast(1)
                reminders += Reminder(
                    addAt,
                    "Добавить: ${item.product.nameRu} (${item.weightG} г) — ему нужно ~${item.timeMin} мин, " +
                        "т. е. за ${phaseTime - addAt} мин до конца",
                )
            }
            if (item.product.cheeseAtEndMin > 0) {
                reminders += Reminder(
                    (phaseTime - item.product.cheeseAtEndMin).coerceAtLeast(1),
                    "Добавить сыр: ${item.product.nameRu} — за ${item.product.cheeseAtEndMin} мин до конца",
                )
            } else if (FinalLook.MELTED_CHEESE in looks && item.product.canBrowning) {
                reminders += Reminder(
                    (phaseTime - CookingRules.CHEESE_AT_END_MIN).coerceAtLeast(1),
                    "Посыпать сыром: ${item.product.nameRu} — за ${CookingRules.CHEESE_AT_END_MIN} мин до конца",
                )
            }
        }

        if (phaseTime > CookingRules.CHECK_EARLY_MIN + 2) {
            reminders += Reminder(
                phaseTime - CookingRules.CHECK_EARLY_MIN,
                "Проверьте готовность — аэрогриль готовит быстрее духовки",
            )
        }

        return CookingPhase(
            index = 0,
            totalPhases = 0,
            mode = anchor.product.mode,
            tempC = anchor.tempC,
            timeMin = phaseTime,
            fanSpeed = modes[anchor.product.mode]?.fanSpeed ?: 0,
            productNames = cluster.map { "${it.product.nameRu} — ${it.weightG} г" },
            instruction = "",
            reminders = reminders.sortedBy { it.atMin }.distinctBy { it.text },
        )
    }

    private fun buildBrowningPhase(items: List<Adjusted>, looks: Set<FinalLook>): CookingPhase? {
        val wantsCrust = FinalLook.GOLDEN_CRUST in looks || FinalLook.CRISPY in looks
        if (!wantsCrust) return null

        val candidates = items.filter { it.product.canBrowning }
        if (candidates.isEmpty()) return null

        val crispy = FinalLook.CRISPY in looks
        val time = if (crispy) CookingRules.BROWNING_TIME_CRISPY_MIN else CookingRules.BROWNING_TIME_MIN
        val temp = clampTemp(CookingMode.GRILL, CookingRules.BROWNING_TEMP_C)

        return CookingPhase(
            index = 0,
            totalPhases = 0,
            mode = CookingMode.GRILL,
            tempC = temp,
            timeMin = time,
            fanSpeed = modes[CookingMode.GRILL]?.fanSpeed ?: 0,
            productNames = candidates.map { it.product.nameRu },
            instruction = "",
            reminders = listOf(
                Reminder(
                    (time - 1).coerceAtLeast(1),
                    "Следите за корочкой: если румянец уже достаточный — остановите раньше",
                ),
            ),
            isBrowning = true,
        )
    }

    private fun buildRationale(
        items: List<Adjusted>,
        clusters: List<List<Adjusted>>,
        phases: List<CookingPhase>,
    ): List<String> = buildList {
        val cookPhases = phases.filter { !it.isBrowning }
        val cookTime = cookPhases.sumOf { it.timeMin }
        val longest = items.maxBy { it.rawTimeMin }

        if (items.size == 1) {
            add("Обычно это блюдо готовится ~$cookTime мин (без прогрева).")
        } else {
            add("Обычно такая комбинация готовится ~$cookTime мин (без прогрева).")
            add("Ориентир — самый долгий продукт: «${longest.product.nameRu}» ~${longest.timeMin} мин.")
        }

        val loadExtra = cookTime - clusters.sumOf { it.first().rawTimeMin.roundToInt() }
        if (loadExtra >= 1) {
            add("Поправка на загрузку корзины (${items.sumOf { it.weightG }} г): +$loadExtra мин.")
        }

        items
            .filter { it.rawTimeMin < longest.rawTimeMin * CookingRules.ADD_LATER_RATIO }
            .sortedBy { it.rawTimeMin }
            .forEach { add("«${it.product.nameRu}» добавите позже — ему нужно ~${it.timeMin} мин.") }

        if (cookPhases.size > 1) {
            val sequence = cookPhases.joinToString(", затем ") { it.mode.labelEn }
            add("Готовим в несколько приёмов: сначала $sequence.")
        }

        cookPhases.firstOrNull()?.let { main ->
            val info = modes[main.mode]
            if (info != null && info.timeRangeLabel.isNotEmpty()) {
                add(
                    "Диапазон режима ${main.mode.labelEn}: " +
                        "${info.tempMinC}–${info.tempMaxC} °C, ${info.timeRangeLabel}.",
                )
            }
        }

        phases.firstOrNull { it.isBrowning }?.let { browning ->
            add("Финал: ${browning.mode.labelEn} ${browning.tempC} °C, ${browning.timeMin} мин — румяная корочка.")
        }
    }

    private fun loadFactor(totalWeightG: Int): Double {
        val extra = (totalWeightG - 500).coerceAtLeast(0) / 1000.0 * CookingRules.LOAD_EXTRA_PER_KG
        return (1.0 + extra).coerceAtMost(CookingRules.MAX_LOAD_FACTOR)
    }

    private fun clampTemp(mode: CookingMode, temp: Int): Int {
        val info = modes[mode] ?: return temp.coerceAtLeast(CookingRules.MIN_TEMP_C)
        return temp.coerceIn(info.tempMinC, info.tempMaxC)
    }

    private fun warnings(items: List<Adjusted>, clusterCount: Int): List<String> = buildList {
        val total = items.sumOf { it.weightG }
        if (total > CookingRules.COMFORT_WEIGHT_G) {
            add(
                "Общий вес $total г — корзину лучше заполнять не более чем на 2/3. " +
                    "Возможно, придётся готовить партиями.",
            )
        }
        if (clusterCount > 1) {
            add("Продукты требуют разных режимов — готовьте последовательно, как описано в фазах.")
        }
        if (items.any { it.product.mode == CookingMode.FROZEN }) {
            add("Замороженные продукты не размораживайте заранее — готовьте прямо из морозилки.")
        }
    }

    private fun tips(items: List<Adjusted>): List<String> = buildList {
        add("Выкладывайте продукты в один слой и не заполняйте корзину выше 2/3.")
        add("Используйте сетчатый противень (crisper plate) — он даёт лучшую циркуляцию воздуха.")
        items.flatMapTo(this) { it.product.tips }
    }.distinct()

    private fun CookingPhase.withInstruction(): CookingPhase {
        val modeText = "${mode.labelEn} (${mode.labelRu})"
        val text = if (isBrowning) {
            "Фаза $index из $totalPhases — подрумянивание. Режим $modeText, $tempC°C, $timeMin мин. " +
                "Откройте корзину, проверьте корочку и при необходимости остановите раньше."
        } else {
            "Фаза $index из $totalPhases — приготовление. Режим $modeText, $tempC°C, $timeMin мин. " +
                "Загрузите: ${productNames.joinToString("; ")}."
        }
        return copy(instruction = text)
    }

    private data class Adjusted(
        val product: Product,
        val weightG: Int,
        val rawTimeMin: Double,
        val timeMin: Int,
        val tempC: Int,
    )
}
