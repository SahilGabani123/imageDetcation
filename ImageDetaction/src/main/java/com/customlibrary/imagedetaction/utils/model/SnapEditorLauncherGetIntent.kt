package com.customlibrary.imagedetaction.utils.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable

data class SnapEditorLauncherGetIntent(
    val context: Context,
    val path: String,
    val mimeType: String,
    val playBackSpeed: Float,
    val isShorts: Boolean = false,
    val isChallenge: Boolean = false,
    val isStory: Boolean = false,
    val isClip: Boolean = false,
    val isFromPreview: Boolean = false,
    val tagName: String? = null
)

data class TextAttributes(
    val text: String,
    val color: Int,
    val size: Float,
    val alignment: String,
    val background: Drawable?,
    val fontId: Int,
    val shadowLayerRadius: Float,
    val shadowLayerDx: Float,
    val shadowLayerDy: Float,
    val shadowLayerColor: Int,
)

data class FontDetails(
    var fontId: Int? = null,
    var fontName: String? = null,
)