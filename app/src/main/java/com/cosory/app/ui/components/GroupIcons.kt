package com.cosory.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.EggAlt
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.KebabDining
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.cosory.app.domain.model.FinalLook
import com.cosory.app.domain.model.ProductGroup

val ProductGroup.groupIcon: ImageVector
    get() = when (this) {
        ProductGroup.POULTRY -> Icons.Outlined.EggAlt
        ProductGroup.MEAT -> Icons.Outlined.KebabDining
        ProductGroup.FISH -> Icons.Outlined.SetMeal
        ProductGroup.POTATO -> Icons.Outlined.Fastfood
        ProductGroup.VEGETABLES -> Icons.Outlined.Eco
        ProductGroup.MUSHROOMS -> Icons.Outlined.Forest
        ProductGroup.DOUGH -> Icons.Outlined.BakeryDining
        ProductGroup.DESSERTS -> Icons.Outlined.Cake
        ProductGroup.EGGS -> Icons.Outlined.Egg
        ProductGroup.FROZEN -> Icons.Outlined.AcUnit
    }

val FinalLook.lookIcon: ImageVector
    get() = when (this) {
        FinalLook.GOLDEN_CRUST -> Icons.Outlined.LocalFireDepartment
        FinalLook.CRISPY -> Icons.Outlined.Whatshot
        FinalLook.JUICY_INSIDE -> Icons.Outlined.WaterDrop
        FinalLook.DEEP_BAKED -> Icons.Outlined.Thermostat
        FinalLook.TENDER -> Icons.Outlined.Spa
        FinalLook.MELTED_CHEESE -> Icons.Outlined.LocalPizza
    }
