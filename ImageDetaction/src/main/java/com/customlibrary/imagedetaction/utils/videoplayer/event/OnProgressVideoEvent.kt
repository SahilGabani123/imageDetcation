package com.customlibrary.imagedetaction.utils.videoplayer.event

interface OnProgressVideoEvent {

    fun updateProgress(time: Float, max: Long, scale: Long)
}
