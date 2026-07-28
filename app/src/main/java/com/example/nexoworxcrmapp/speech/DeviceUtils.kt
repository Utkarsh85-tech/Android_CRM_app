package com.example.nexoworxcrmapp.speech

import android.os.Build

object DeviceUtils {
    /** True for Android Studio emulator / most AVDs. */
    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk", ignoreCase = true)
            || Build.MODEL.contains("Emulator", ignoreCase = true)
            || Build.MODEL.contains("Android SDK built for x86", ignoreCase = true)
            || Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
            || Build.BRAND.startsWith("generic", ignoreCase = true)
            || Build.DEVICE.startsWith("generic", ignoreCase = true)
            || Build.PRODUCT.contains("sdk", ignoreCase = true)
    }
}
