package com.cosory.app.data

import com.cosory.app.data.dto.ModeDto
import com.cosory.app.data.dto.ProductDto
import com.cosory.app.data.dto.ReferenceDto
import com.cosory.app.domain.model.CookingMode
import com.cosory.app.domain.model.ModeInfo
import com.cosory.app.domain.model.Product
import com.cosory.app.domain.model.ProductGroup

class CookingRepository(source: AssetDataSource) {

    private val productCatalog by lazy { source.loadProducts() }
    private val modeCatalog by lazy { source.loadModes() }

    val reference: ReferenceDto by lazy { source.loadReference() }

    val products: List<Product> by lazy { productCatalog.products.map { it.toDomain() } }

    val modes: Map<CookingMode, ModeInfo> by lazy {
        modeCatalog.modes.mapNotNull { it.toDomain() }.associateBy { it.mode }
    }

    val groupedProducts: Map<ProductGroup, List<Product>> by lazy {
        ProductGroup.entries
            .associateWith { group -> products.filter { it.group == group } }
            .filterValues { it.isNotEmpty() }
    }
}

private fun ProductDto.toDomain(): Product = Product(
    id = id,
    nameRu = nameRu,
    group = ProductGroup.entries.firstOrNull { it.name == group } ?: ProductGroup.VEGETABLES,
    baseWeightG = baseWeightG,
    baseTimeMin = baseTimeMin,
    baseTempC = baseTempC,
    mode = CookingMode.entries.firstOrNull { it.name == mode } ?: CookingMode.AIR_FRY,
    timeExponent = timeExponent,
    turnOver = turnOver,
    shake = shake,
    cheeseAtEndMin = cheeseAtEndMin,
    canBrowning = canBrowning,
    tips = tips,
)

private fun ModeDto.toDomain(): ModeInfo? {
    val cookingMode = CookingMode.entries.firstOrNull { it.name == id } ?: return null
    return ModeInfo(
        mode = cookingMode,
        fanSpeed = fanSpeed,
        tempMinC = tempMinC,
        tempMaxC = tempMaxC,
        defaultTempC = defaultTempC,
        defaultTimeMin = defaultTimeMin,
        description = description,
        timeRangeLabel = timeRangeLabel,
    )
}
