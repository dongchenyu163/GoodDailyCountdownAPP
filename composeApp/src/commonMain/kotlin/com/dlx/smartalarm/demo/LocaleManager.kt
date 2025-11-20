package com.dlx.smartalarm.demo

import dev.icerock.moko.resources.desc.StringDesc

object LocaleManager {
    fun setLocale(language: String) {
        StringDesc.localeType = if (language.isNotBlank()) {
            StringDesc.LocaleType.Custom(language)
        } else {
            StringDesc.LocaleType.System
        }
    }
}
