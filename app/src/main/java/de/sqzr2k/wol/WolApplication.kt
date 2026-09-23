package de.sqzr2k.wol

import android.app.Application

class WolApplication : Application() {
    val container by lazy { AppContainer(this) }
}
