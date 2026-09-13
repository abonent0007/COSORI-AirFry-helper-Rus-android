package com.cosory.app.domain.model

enum class ProductGroup(val labelRu: String) {
    POULTRY("Птица"),
    MEAT("Мясо"),
    FISH("Рыба и морепродукты"),
    POTATO("Картофель"),
    VEGETABLES("Овощи"),
    MUSHROOMS("Грибы"),
    DOUGH("Тесто и выпечка"),
    DESSERTS("Десерты"),
    EGGS("Яйца и молочное"),
    FROZEN("Замороженные продукты"),
}

enum class CookingMode(val labelRu: String, val labelEn: String) {
    AIR_FRY("Аэрофритюр", "Air Fry"),
    ROAST("Запекание", "Roast"),
    BAKE("Выпечка", "Bake"),
    GRILL("Гриль", "Grill"),
    FROZEN("Заморозка", "Frozen"),
    REHEAT("Разогрев", "Reheat"),
    DEHYDRATE("Сушка", "Dry"),
    PROOF("Расстойка", "Proof"),
    PREHEAT("Преднагрев", "Preheat"),
    KEEP_WARM("Поддержание тепла", "Warm"),
}

enum class FinalLook(val labelRu: String) {
    GOLDEN_CRUST("Румяная корочка"),
    CRISPY("Хрустящая корочка"),
    JUICY_INSIDE("Сочный внутри"),
    DEEP_BAKED("Глубокая пропекаемость"),
    TENDER("Мягкий, нежный"),
    MELTED_CHEESE("Расплавленный сыр"),
}

data class Product(
    val id: String,
    val nameRu: String,
    val group: ProductGroup,
    val baseWeightG: Int,
    val baseTimeMin: Int,
    val baseTempC: Int,
    val mode: CookingMode,
    val timeExponent: Double = 0.4,
    val turnOver: Boolean = false,
    val shake: Boolean = false,
    val cheeseAtEndMin: Int = 0,
    val canBrowning: Boolean = false,
    val tips: List<String> = emptyList(),
)

data class ModeInfo(
    val mode: CookingMode,
    val fanSpeed: Int,
    val tempMinC: Int,
    val tempMaxC: Int,
    val defaultTempC: Int,
    val defaultTimeMin: Int,
    val description: String,
    val timeRangeLabel: String = "",
)
