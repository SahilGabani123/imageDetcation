package com.customlibrary.imagedetaction.utils.videoplayer.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.View
import com.customlibrary.imagedetaction.R
import com.customlibrary.imagedetaction.utils.videoplayer.utils.BackgroundExecutor
import com.customlibrary.imagedetaction.utils.videoplayer.utils.UiThreadExecutor
import java.io.IOException
import kotlin.math.ceil

class TimeLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mVideoUri: Uri? = null
    private var mHeightView: Int = 0
    private var mBitmapList: LongSparseArray<Bitmap>? = null
    companion object {
        const val N_1000 = 1000
        const val N_11 = 11
    }

    init {
        init()
    }

    private fun init() {
        mHeightView = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + mHeightView
        val h = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW) getBitmap(w)
    }
    private fun getVideoLength(mediaMetadataRetriever: MediaMetadataRetriever): Long {
        return (
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0L
            ) * N_1000
    }
    private fun getBitmap(viewWidth: Int) {
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val threshold = N_11
                    val thumbnailList = LongSparseArray<Bitmap>()
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mVideoUri)
                    val videoLengthInMs = getVideoLength(mediaMetadataRetriever)
                    val frameHeight = mHeightView
                    val initialBitmap = mediaMetadataRetriever.getFrameAtTime(
                        0,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    if (initialBitmap == null) {
                        return
                    }
                    val frameWidth = (
                        (
                            initialBitmap.width.toFloat() / initialBitmap.height.toFloat()
                            ) * frameHeight
                        ).toInt()
                    var numThumbs = ceil(viewWidth.toFloat() / frameWidth).toInt()
                    if (numThumbs < threshold) numThumbs = threshold
                    val cropWidth = viewWidth / threshold
                    val interval = videoLengthInMs / numThumbs

                    for (i in 0 until numThumbs) {
                        val bitmap = mediaMetadataRetriever.getFrameAtTime(
                            i * interval,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        if (bitmap != null) {
                            try {
                                val scaledBitmap = Bitmap.createScaledBitmap(
                                    bitmap,
                                    frameWidth,
                                    frameHeight,
                                    false
                                )
                                val croppedBitmap = Bitmap.createBitmap(
                                    scaledBitmap,
                                    0,
                                    0,
                                    cropWidth,
                                    scaledBitmap.height
                                )
                                thumbnailList.put(i.toLong(), croppedBitmap)
                            } catch (e: IOException) {
                                Log.e("MediaEditor","TimeLineView error:-$e")
                            }
                        }
                    }

                    mediaMetadataRetriever.release()
                    returnBitmaps(thumbnailList)
                } catch (e: IOException) {
                    Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(Thread.currentThread(), e)
                }
            }
        })
    }

    private fun returnBitmaps(thumbnailList: LongSparseArray<Bitmap>) {
        UiThreadExecutor.runTask("", {
            mBitmapList = thumbnailList
            invalidate()
        }, 0L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBitmapList != null) {
            canvas.save()
            var x = 0
            for (i in 0 until (mBitmapList?.size() ?: 0)) {
                val bitmap = mBitmapList?.get(i.toLong())
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
                    x += bitmap.width
                }
            }
            canvas.restore()
        }
    }

    fun setVideo(data: Uri) {
        mVideoUri = data
    }
}
