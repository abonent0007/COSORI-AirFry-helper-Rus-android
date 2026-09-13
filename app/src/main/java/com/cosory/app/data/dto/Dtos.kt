package com.cosory.app.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCatalogDto(
    val version: Int = 1,
    val products: List<ProductDto> = emptyList(),
)

@Serializable
data class ProductDto(
    val id: String,
    val nameRu: String,
    val group: String,
    val baseWeightG: Int,
    val baseTimeMin: Int,
    val baseTempC: Int,
    val mode: String,
    val timeExponent: Double = 0.4,
    val turnOver: Boolean = false,
    val shake: Boolean = false,
    val cheeseAtEndMin: Int = 0,
    val canBrowning: Boolean = false,
    val tips: List<String> = emptyList(),
)

@Serializable
data class ModeCatalogDto(
    val modes: List<ModeDto> = emptyList(),
)

@Serializable
data class ModeDto(
    val id: String,
    val fanSpeed: Int = 0,
    val tempMinC: Int = 40,
    val tempMaxC: Int = 230,
    val defaultTempC: Int = 180,
    val defaultTimeMin: Int = 15,
    val timeRangeLabel: String = "",
    val description: String = "",
)

@Serializable
data class ReferenceDto(
    val welcomeTitle: String = "",
    val welcome: List<ReferenceSectionDto> = emptyList(),
    val buttons: List<ButtonRowDto> = emptyList(),
    val firstSteps: List<String> = emptyList(),
    val safety: List<String> = emptyList(),
    val care: List<String> = emptyList(),
    val steamClean: SteamCleanDto = SteamCleanDto(),
    val troubleshooting: List<String> = emptyList(),
    val errorCodes: List<ErrorCodeDto> = emptyList(),
    val tips: List<String> = emptyList(),
)

@Serializable
data class SteamCleanDto(
    val title: String = "",
    val intro: String = "",
    val waterMl: String = "",
    val vinegar: String = "",
    val tempC: Int = 160,
    val timeMin: Int = 12,
    val dwellMin: Int = 8,
    val mode: String = "AIR_FRY",
    val steps: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val physics: List<String> = emptyList(),
)

@Serializable
data class ReferenceSectionDto(
    val title: String,
    val items: List<String> = emptyList(),
)

@Serializable
data class ButtonRowDto(
    val en: String,
    val ru: String,
    val purpose: String,
    val fan: String = "",
)

@Serializable
data class ErrorCodeDto(
    val code: String,
    val meaning: String,
)
