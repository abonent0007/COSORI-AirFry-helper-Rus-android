package com.cosory.app.data

import android.content.Context
import com.cosory.app.data.dto.ModeCatalogDto
import com.cosory.app.data.dto.ProductCatalogDto
import com.cosory.app.data.dto.ReferenceDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AssetDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadProducts(): ProductCatalogDto = load("data/products.json")

    fun loadModes(): ModeCatalogDto = load("data/modes.json")

    fun loadReference(): ReferenceDto = load("data/reference.json")

    private inline fun <reified T> load(path: String): T =
        context.assets.open(path).bufferedReader().use { reader ->
            json.decodeFromString<T>(reader.readText())
        }
}
