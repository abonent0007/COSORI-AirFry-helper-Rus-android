package com.cosory.app

import android.app.Application
import com.cosory.app.data.AssetDataSource
import com.cosory.app.data.CookingRepository
import com.cosory.app.data.Prefs

class CosoryApplication : Application() {

    val repository: CookingRepository by lazy { CookingRepository(AssetDataSource(this)) }

    val prefs: Prefs by lazy { Prefs(this) }
}
