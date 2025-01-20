package com.customlibrary.imagedetaction.application

import android.content.Context
import com.customlibrary.imagedetaction.model.SDKConfig

object MySDK {

    private var isInitialized = false

    fun initialize(context: Context, config: SDKConfig) {
        if (isInitialized) {
            throw IllegalStateException("SDK is already initialized!")
        }
        Logger.setup(config.enableLogging)
        isInitialized = true
    }

    fun isInitialized(): Boolean {
        return isInitialized
    }
}
