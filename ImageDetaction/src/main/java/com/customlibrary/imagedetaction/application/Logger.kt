package com.customlibrary.imagedetaction.application

object Logger {

    private var isLoggingEnabled = false

    fun setup(enableLogging: Boolean) {
        isLoggingEnabled = enableLogging
    }

    fun log(message: String) {
        if (isLoggingEnabled) {
            println("SDK Log: $message")
        }
    }
}
