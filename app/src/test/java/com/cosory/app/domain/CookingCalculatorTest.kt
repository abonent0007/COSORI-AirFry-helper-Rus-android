package com.cosory.app.domain

import com.cosory.app.domain.model.CookingItem
import com.cosory.app.domain.model.CookingMode
import com.cosory.app.domain.model.CookingRequest
import com.cosory.app.domain.model.FinalLook
import com.cosory.app.domain.model.ModeInfo
import com.cosory.app.domain.model.Product
import com.cosory.app.domain.model.ProductGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CookingCalculatorTest {

    private val chicken = Product(
        id = "chicken_breast",
        nameRu = "Куриное филе",
        group = ProductGroup.POULTRY,
        baseWeightG = 200,
        baseTimeMin = 15,
        baseTempC = 195,
        mode = CookingMode.AIR_FRY,
        timeExponent = 0.3,
        turnOver = true,
        canBrowning = true,
    )

    private val potatoes = Product(
        id = "potatoes_wedges",
        nameRu = "Картофель дольками",
        group = ProductGroup.POTATO,
        baseWeightG = 500,
        baseTimeMin = 16,
        baseTempC = 200,
        mode = CookingMode.AIR_FRY,
        timeExponent = 0.35,
        shake = true,
        canBrowning = true,
    )

    private val brownie = Product(
        id = "brownie",
        nameRu = "Брауни",
        group = ProductGroup.DESSERTS,
        baseWeightG = 400,
        baseTimeMin = 15,
        baseTempC = 160,
        mode = CookingMode.BAKE,
        timeExponent = 0.3,
    )

    private val nuggets = Product(
        id = "nuggets",
        nameRu = "Наггетсы",
        group = ProductGroup.FROZEN,
        baseWeightG = 400,
        baseTimeMin = 10,
        baseTempC = 195,
        mode = CookingMode.FROZEN,
        timeExponent = 0.25,
        turnOver = true,
    )

    private val salmon = Product(
        id = "salmon",
        nameRu = "Лосось",
        group = ProductGroup.FISH,
        baseWeightG = 250,
        baseTimeMin = 9,
        baseTempC = 195,
        mode = CookingMode.AIR_FRY,
        timeExponent = 0.3,
        turnOver = true,
        canBrowning = true,
    )

    private val sausages = Product(
        id = "sausages",
        nameRu = "Сосиски",
        group = ProductGroup.MEAT,
        baseWeightG = 400,
        baseTimeMin = 9,
        baseTempC = 210,
        mode = CookingMode.AIR_FRY,
        timeExponent = 0.25,
        turnOver = true,
        canBrowning = true,
    )

    private val modes = mapOf(
        CookingMode.AIR_FRY to ModeInfo(CookingMode.AIR_FRY, 5, 150, 230, 195, 10, ""),
        CookingMode.BAKE to ModeInfo(CookingMode.BAKE, 3, 125, 205, 165, 20, ""),
        CookingMode.FROZEN to ModeInfo(CookingMode.FROZEN, 5, 150, 230, 200, 14, ""),
        CookingMode.GRILL to ModeInfo(CookingMode.GRILL, 5, 220, 230, 230, 5, ""),
        CookingMode.ROAST to ModeInfo(CookingMode.ROAST, 5, 175, 230, 220, 12, ""),
        CookingMode.DEHYDRATE to ModeInfo(CookingMode.DEHYDRATE, 1, 35, 95, 55, 240, ""),
    )

    private val calculator = CookingCalculator(modes)

    @Test
    fun `single product uses base mode temp and time`() {
        val plan = calculator.calculate(CookingRequest(listOf(CookingItem(chicken, 200))))

        assertEquals(1, plan.phases.size)
        assertEquals(CookingMode.AIR_FRY, plan.phases[0].mode)
        assertEquals(195, plan.phases[0].tempC)
        assertEquals(15, plan.phases[0].timeMin)
        assertNotNull(plan.preheat)
        assertEquals(4, plan.preheat?.timeMin)
        assertEquals(19, plan.totalTimeMin)
        assertEquals(15, plan.cookingTimeMin)
    }

    @Test
    fun `time scales with weight`() {
        val plan = calculator.calculate(CookingRequest(listOf(CookingItem(chicken, 400))))

        assertEquals(18, plan.phases[0].timeMin)
    }

    @Test
    fun `compatible products cook in one phase with max time and load factor`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(chicken, 200), CookingItem(potatoes, 500))),
        )

        assertEquals(1, plan.phases.size)
        assertEquals(16, plan.phases[0].timeMin)
        assertEquals(200, plan.phases[0].tempC)
        assertTrue(plan.phases[0].reminders.any { it.atMin == 8 && it.text.contains("Перевернуть") })
        assertTrue(plan.phases[0].reminders.any { it.atMin == 8 && it.text.contains("Встряхнуть") })
        assertTrue(plan.phases[0].reminders.none { it.text.startsWith("Добавить:") })
        assertTrue(plan.rationale.any { it.contains("Картофель дольками") && it.contains("Ориентир") })
    }

    @Test
    fun `conflicting modes produce sequential phases`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(brownie, 400), CookingItem(nuggets, 400))),
        )

        assertEquals(2, plan.phases.size)
        assertEquals(CookingMode.BAKE, plan.phases[0].mode)
        assertEquals(15, plan.phases[0].timeMin)
        assertEquals(CookingMode.FROZEN, plan.phases[1].mode)
        assertEquals(10, plan.phases[1].timeMin)
        assertEquals(29, plan.totalTimeMin)
        assertTrue(plan.warnings.any { it.contains("разных режимов") })
        assertTrue(plan.rationale.any { it.contains("несколько приёмов") })
    }

    @Test
    fun `golden crust adds grill browning phase at device temperature`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(chicken, 200)), setOf(FinalLook.GOLDEN_CRUST)),
        )

        assertEquals(2, plan.phases.size)
        val browning = plan.phases[1]
        assertEquals(CookingMode.GRILL, browning.mode)
        assertEquals(230, browning.tempC)
        assertEquals(2, browning.timeMin)
        assertTrue(browning.isBrowning)
        assertEquals(21, plan.totalTimeMin)
        assertTrue(plan.rationale.any { it.contains("Финал") })
    }

    @Test
    fun `juicy inside lowers temp and extends time slightly`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(chicken, 200)), setOf(FinalLook.JUICY_INSIDE)),
        )

        assertEquals(190, plan.phases[0].tempC)
        assertEquals(16, plan.phases[0].timeMin)
    }

    @Test
    fun `crispy frozen product extends time without grill phase`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(nuggets, 400)), setOf(FinalLook.CRISPY)),
        )

        assertEquals(1, plan.phases.size)
        assertEquals(11, plan.phases[0].timeMin)
    }

    @Test
    fun `crispy meat gets grill phase`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(chicken, 200)), setOf(FinalLook.CRISPY)),
        )

        assertEquals(2, plan.phases.size)
        assertEquals(230, plan.phases[1].tempC)
        assertEquals(3, plan.phases[1].timeMin)
    }

    @Test
    fun `short product gets add-later reminder and rationale`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(salmon, 250), CookingItem(potatoes, 500))),
        )

        assertTrue(
            plan.phases[0].reminders.any { it.text.startsWith("Добавить:") && it.text.contains("Лосось") },
        )
        assertTrue(plan.rationale.any { it.contains("Лосось") && it.contains("позже") })
    }

    @Test
    fun `cheese look adds cheese reminder`() {
        val plan = calculator.calculate(
            CookingRequest(
                listOf(CookingItem(chicken, 200)),
                setOf(FinalLook.GOLDEN_CRUST, FinalLook.MELTED_CHEESE),
            ),
        )

        assertTrue(plan.phases[0].reminders.any { it.text.contains("сыр") })
    }

    @Test
    fun `phases carry instruction and numbering`() {
        val plan = calculator.calculate(
            CookingRequest(
                listOf(CookingItem(chicken, 200), CookingItem(potatoes, 500), CookingItem(brownie, 600)),
                setOf(FinalLook.GOLDEN_CRUST),
            ),
        )

        assertEquals(3, plan.phases.size)
        plan.phases.forEach { phase ->
            assertTrue(phase.instruction.isNotEmpty())
            assertEquals(3, phase.totalPhases)
            assertTrue(phase.index in 1..3)
        }
    }

    @Test
    fun `no preheat for dry modes`() {
        val plan = calculator.calculate(
            CookingRequest(listOf(CookingItem(nuggets.copy(mode = CookingMode.DEHYDRATE), 400))),
        )

        assertNull(plan.preheat)
    }

    @Test
    fun `sausages stay fast and single dish rationale`() {
        val plan = calculator.calculate(CookingRequest(listOf(CookingItem(sausages, 400))))

        assertTrue(plan.phases[0].timeMin <= 10)
        assertTrue(plan.rationale.any { it.contains("Обычно это блюдо") })
    }

    @Test
    fun `look modifiers do not stack multiplicatively`() {
        val plan = calculator.calculate(
            CookingRequest(
                listOf(CookingItem(chicken, 200)),
                setOf(FinalLook.JUICY_INSIDE, FinalLook.TENDER, FinalLook.DEEP_BAKED),
            ),
        )

        assertEquals(17, plan.phases[0].timeMin)
        assertEquals(180, plan.phases[0].tempC)
    }

    @Test
    fun `all looks keep temperatures within official mode ranges`() {
        val allLooks = setOf(
            FinalLook.GOLDEN_CRUST,
            FinalLook.CRISPY,
            FinalLook.JUICY_INSIDE,
            FinalLook.DEEP_BAKED,
            FinalLook.TENDER,
            FinalLook.MELTED_CHEESE,
        )
        val products = listOf(chicken, potatoes, brownie, nuggets, salmon, sausages)

        products.forEach { product ->
            val plan = calculator.calculate(CookingRequest(listOf(CookingItem(product, 500)), allLooks))
            plan.phases.forEach { phase ->
                val info = modes.getValue(phase.mode)
                assertTrue(
                    "${product.id}: ${phase.mode} ${phase.tempC} not in ${info.tempMinC}-${info.tempMaxC}",
                    phase.tempC in info.tempMinC..info.tempMaxC,
                )
            }
        }
    }
}
