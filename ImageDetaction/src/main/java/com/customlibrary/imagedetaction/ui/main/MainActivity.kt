package com.customlibrary.imagedetaction.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Editable
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.customlibrary.imagedetaction.R
import com.customlibrary.imagedetaction.databinding.ActivityMainBinding
import com.customlibrary.imagedetaction.ui.main.rx.RxBus
import com.customlibrary.imagedetaction.ui.main.rx.RxEvent
import com.customlibrary.imagedetaction.ui.main.stickers.StickerBottomSheet
import com.customlibrary.imagedetaction.ui.main.view.FontPickerAdapter
import com.customlibrary.imagedetaction.utils.MIME_TYPE_IMAGE_JPEG
import com.customlibrary.imagedetaction.utils.MIME_TYPE_VIDEO_MP4
import com.customlibrary.imagedetaction.utils.hideKeyboard
import com.customlibrary.imagedetaction.utils.model.FontDetails
import com.customlibrary.imagedetaction.utils.model.TextAttributes
import com.customlibrary.imagedetaction.utils.videoplayer.event.OnProgressVideoEvent
import com.customlibrary.imagedetaction.utils.videoplayer.event.OnVideoEditedEvent
import com.customlibrary.imagedetaction.utils.videoplayer.utils.TrimVideoUtils
import com.customlibrary.imagedetaction.utils.Constant
import com.customlibrary.imagedetaction.utils.Constant.FIXED_10_FLOAT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_10_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_3_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_4_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_5_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_6_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_7_INT
import com.customlibrary.imagedetaction.utils.Constant.FIXED_80_INT
import com.rtugeek.android.colorseekbar.ColorSeekBar
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

class MainActivity : AppCompatActivity() , OnVideoEditedEvent {

    companion object {
        var colorCodeTextView: Int = -65512
        const val DEFOULT_TEXT_COLOR_CODE = -65512
        const val CROP_REQUEST = 200
        const val INTENT_VIDEO_PATH = "path"
        private const val SHOW_PROGRESS = 2
        private const val STICKER = "STICKER"
        private const val EMOJI = "EMOJI"
        private const val CROP = "CROP"
        private const val TEXT = "TEXT"
        private const val PEN = "PEN"
        private const val BLUR = "ERASE"
        private const val TEXT_ALIGN_CENTER = "TEXT_ALIGN_CENTER"
        private const val TEXT_ALIGN_RIGHT = "TEXT_ALIGN_RIGHT"
        private const val TEXT_ALIGN_LEFT = "TEXT_ALIGN_LEFT"
        private const val EMOJIS_DRAWING = 1
        private const val LINE_DRAWING = 0
        private var lastTextSize = 40.0f
        private var shadowLayerRadius: Float = 0f
        private var shadowLayerDx: Float = 0f
        private var shadowLayerDy: Float = 0f
        private var shadowLayerColor: Int = 0
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        const val COLOR_0xFFFFFF = 0xFFFFFF
        private const val TAG = "MainActivity"
        fun getIntent(
            context: Context, path: String
        ): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_VIDEO_PATH, path)
            return intent
        }
    }

    private lateinit var binding: ActivityMainBinding
    private var selectedItem: String? = null
    private lateinit var stickerBottomSheet: StickerBottomSheet
    private val drawnEmojis = mutableListOf<Pair<Float, Float>>()
    private val emojiViews = mutableListOf<ImageView>()
    private var shapeDrawable: GradientDrawable? = null
    private var textAlignPosition: String = TEXT_ALIGN_CENTER
    private var fontId: Int = R.font.american_typewriter
    private var fontList: ArrayList<FontDetails> = arrayListOf()
    private var lastShadowColor = 0
    private var count = 0
    private var listOfRemoveCount: ArrayList<Int> = arrayListOf()
    private var listOfDrawingView: ArrayList<Int> = arrayListOf()
    private var currentTextView: TextView? = null
    private var isBackGroundAdd = false
    private var ivStrikethroughClicked = false
    private var strikethroughApplied = false
    private var backgroundAdded = false
    private var selectedItemAddText: TextAttributes? = null
    private val texts = mutableListOf<TextAttributes>()
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var isDragging: Boolean = false
    private var lastX = -1f
    private var lastY = -1f
    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f
    private var lastMode = NONE

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var mOnVideoEditedListener: OnVideoEditedEvent? = null
    lateinit var mPlayer: ExoPlayer
    lateinit var audioPlayer: ExoPlayer
    private lateinit var mSrc: Uri
    private var mResetSeekBar = false
    private var originalVideoWidth: Int = 0
    private var originalVideoHeight: Int = 0
    private var videoPlayerWidth: Int = 0
    private var videoPlayerHeight: Int = 0
    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoEvent> = ArrayList()
    private var mStartPosition = 0L
    private var mEndPosition = 0L
    private var mDuration: Long = 0L
    private var isVideoPrepared = false
    private var mTimeVideo = 0L
    private var savedDrawing: Bitmap? = null
    private var mimeType = MIME_TYPE_IMAGE_JPEG
    private var isClickEmojis: Boolean = false
    private var isClickText: Boolean = false
    private var isClickPen: Boolean = false
    private var emojisIndex: Int? = 0
    private val mMessageHandler = MessageHandler(this)
    private var mediaUrl : String = ""
    private var cropVideoPath = ""
    private var outputMediaUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stickerBottomSheet = StickerBottomSheet()
        initializeFontList()
        listenToViewEvent()
        mediaUrl = intent.getStringExtra(INTENT_VIDEO_PATH) ?: return
        setupMediaIfNeeded(mediaUrl)
    }



    private fun setupMediaIfNeeded(path: String) {
        binding.ivEraser.isVisible = mimeType == MIME_TYPE_IMAGE_JPEG
        binding.deleteAppCompatImageView.isVisible = mimeType == MIME_TYPE_VIDEO_MP4

        if (mimeType == MIME_TYPE_VIDEO_MP4) {
            mListeners = ArrayList()
            mListeners.add(object : OnProgressVideoEvent {
                override fun updateProgress(time: Float, max: Long, scale: Long) {
                    updateVideoProgress(time.toLong())
                }
            })
            binding.imagePreview.visibility = View.GONE
            binding.layout.visibility = View.VISIBLE
            binding.videoLoader.apply {
                setVideoBackgroundColor(resources.getColor(R.color.white))
                setOnTrimVideoListener(this@MainActivity)
                setVideoURI(Uri.parse(path))
                setMinDuration(1)
                Handler(Looper.getMainLooper()).postDelayed({
                    onClickVideoPlayPause()
                }, Constant.FIXED_500_MILLISECOND)
            }
            val videoSize = getVideoDimensions(path)
            if (videoSize != null) {
                val deviceHeight = resources.displayMetrics.heightPixels
                val deviceWitdh = resources.displayMetrics.widthPixels
                videoHeight = if (videoSize.height > deviceHeight) deviceHeight else videoSize.height
                videoWidth = if (videoSize.width > deviceWitdh) deviceWitdh else videoSize.width
                binding.flContainer.layoutParams?.apply {
                    this.height = videoHeight
                    this.width = videoWidth
                }
            } else {
                Timber.tag(TAG).e("Failed to retrieve video dimisensions.")
            }
        } else if (mimeType == MIME_TYPE_IMAGE_JPEG) {
            binding.videoContainer.visibility = View.GONE
            binding.layout.visibility = View.GONE
            binding.imagePreview.post {
                Glide.with(this).load(path).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        finish()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        binding.imagePreview.setBackgroundColor(Color.BLACK)
                        imageWidth = resource.intrinsicWidth
                        imageHeight = resource.intrinsicHeight
                        binding.flContainer.layoutParams?.apply {
                            this.height = imageHeight
                            this.width = imageWidth
                        }
                        return false
                    }
                })
                    .into(binding.imagePreview)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun listenToViewEvent() {
        binding.ivAddText.setOnClickListener {
            selectedItem = TEXT
            binding.llText.isVisible = false
            shapeDrawable = null
            colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
            textAlignPosition = TEXT_ALIGN_CENTER
            fontId = R.font.american_typewriter
            lastTextSize = 40.0f
            shadowLayerRadius = 0f
            shadowLayerDx = 0f
            shadowLayerDy = 0f
            lastShadowColor = 0
            val newTextAttributes = TextAttributes(
                text = " ",
                color = colorCodeTextView,
                size = lastTextSize,
                alignment = TEXT_ALIGN_CENTER,
                background = shapeDrawable,
                fontId = fontId,
                shadowLayerRadius = shadowLayerRadius,
                shadowLayerDx = shadowLayerDx,
                shadowLayerDy = shadowLayerDy,
                shadowLayerColor = lastShadowColor,

                )
            openAddTextPopupWindow(newTextAttributes)
            if (mimeType == MIME_TYPE_VIDEO_MP4) {
                moveDrawViewToTop()
            }
        }

        binding.ivEdit.setOnClickListener {
            if (!isClickPen) {
                binding.llText.isVisible = false
                isClickPen = true
                isClickEmojis = false
                selectedItem = PEN
                binding.llEdit.isVisible = false
                binding.ivSelectedPen.isVisible = true
                binding.llTextEdit.isVisible = true
                binding.addEmojis.isVisible = true
                binding.colorSeekBar.isVisible = true
                binding.toolsLayout.isVisible = true
                binding.ivSelectedText.isVisible = false
                binding.drawView.enableDrawing()
                binding.emojisCardView.isVisible = false
                if (mimeType == MIME_TYPE_VIDEO_MP4) {
                    binding.relativeLayout.visibility = View.INVISIBLE
                    binding.deleteAppCompatImageView.visibility = View.INVISIBLE
                    binding.layout.alpha = 0.0f
                    binding.layout.setBackgroundColor(resources.getColor(R.color.color_transparent, null))
                    moveDrawViewToTop()
                } else {
                    binding.relativeLayout.visibility = View.INVISIBLE
                    binding.deleteAppCompatImageView.visibility = View.INVISIBLE
                }
            }
        }

        binding.addEmojis.setOnClickListener {
            if (!isClickEmojis) {
                setDrawable(Constant.FIXED_8_INT)
                isClickEmojis = true
                selectedItem = EMOJI
                binding.llEdit.isVisible = false
                binding.ivSelectedPen.isVisible = true
                binding.llTextEdit.isVisible = true
                binding.toolsLayout.isVisible = true
                binding.ivSelectedText.isVisible = false
                binding.emojisCardView.isVisible = true
                binding.colorSeekBar.isVisible = false
                binding.addEmojis.isVisible = false
                binding.addGridHex.isVisible = true
                binding.drawView.disableDrawing()
                binding.emojiContainer.isVisible = true
            }
        }
        binding.ivSelectedPen.setOnClickListener {
            if (isClickPen) {
                binding.llEdit.isVisible = true
                binding.llTextEdit.isVisible = false
                binding.toolsLayout.isVisible = false
                emojisIndex = null
                isClickText = false
                isClickPen = false
                binding.drawView.disableDrawing()

                if (mimeType != MIME_TYPE_VIDEO_MP4) {
                    binding.deleteAppCompatImageView.isVisible = false
                    binding.relativeLayout.isVisible = true
                } else {
                    binding.relativeLayout.isVisible = true
                    binding.deleteAppCompatImageView.isVisible = true
                    binding.layout.alpha = 1.0f
                    moveDrawViewToTop()
                }
            }
        }
        binding.buttonUndo.setOnClickListener {
            if (!listOfDrawingView.isNullOrEmpty()) {
                when (listOfDrawingView.last()) {
                    EMOJIS_DRAWING -> {
                        if (!listOfRemoveCount.isNullOrEmpty()) {
                            for (i in 0 until listOfRemoveCount[listOfRemoveCount.size - 1]) {
                                undoLastEmoji()
                            }
                            listOfRemoveCount.removeLast()
                        }
                    }

                    LINE_DRAWING -> {
                        binding.drawView.undo()
                    }

                    else -> {
                    }
                }
                listOfDrawingView.removeLast()
            }
        }

        binding.ivAddSticker.setOnClickListener {
            binding.llText.isVisible = false
            selectedItem = STICKER
            emojisIndex = null
            openBottomSheet()
            if (mimeType == MIME_TYPE_VIDEO_MP4) {
                moveDrawViewToTop()
            }
        }
        binding.colorSeekBar.setOnColorChangeListener { _, color ->
            binding.drawView.setColor(color)
        }
        handelEmojisClick()
        handelOnTouchListener()
        binding.addGridHex.setOnClickListener {
            binding.colorSeekBar.isVisible = true
            binding.addGridHex.isVisible = false
            binding.addEmojis.isVisible = true
            binding.emojisCardView.isVisible = false
            selectedItem = PEN
            emojisIndex = null
            isClickEmojis = false
            binding.drawView.enableDrawing()
        }

        binding.ivEraser.setOnClickListener {
            binding.llText.isVisible = false
            selectedItem = BLUR
            binding.llEdit.isVisible = false
            binding.llEraser.isVisible = true
            binding.relativeLayout.isVisible = false
        }

        binding.ivSelectedEraser.setOnClickListener {
            selectedItem = null
            binding.llEdit.isVisible = true
            binding.llEraser.isVisible = false
            binding.relativeLayout.isVisible = true
        }

        RxBus.listen(RxEvent.ItemClick::class.java).subscribe { event ->
            val bitmap = BitmapFactory.decodeResource(resources, event.itemId)

            val imageView = ImageView(this).apply {
                setImageBitmap(bitmap)
            }

            val imageViewParams = FrameLayout.LayoutParams(
                Constant.FIXED_150_INT,
                Constant.FIXED_150_INT
            ).apply {
                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
            }
            binding.emojiContainer.addView(imageView, imageViewParams)

            imageView.setOnTouchListener { v, events ->
                val views = v as ImageView
                views.bringToFront()
                viewTransformation(views, events)
                true
            }
            stickerBottomSheet.dismiss()
        }


        binding.ivCrop.setOnClickListener {
            binding.llText.isVisible = false
            if (mimeType == MIME_TYPE_IMAGE_JPEG) {
                selectedItem = CROP
                emojisIndex = null
                drawnEmojis.clear()
                val file = File(mediaUrl)
                val uri: Uri = Uri.fromFile(file)
                startCrop(uri)
            } else {
                val savedFile = saveVideoToCacheDir(this, "Edited_video_${System.currentTimeMillis()}")
                cropVideoPath = savedFile.path
//                startActivityForResult(VideoCropActivity.createIntent(this, mediaUrl, savedFile.path), CROP_REQUEST)
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))

        val options = UCrop.Options().apply {
            setCompressionQuality(Constant.FIXED_70_INT)
            setFreeStyleCropEnabled(true)
        }
        UCrop.of(uri, destinationUri).withOptions(options).withAspectRatio(1f, 1f).start(this)
    }

    fun saveVideoToCacheDir(context: Context, fileName: String): File {
        val cacheDir = context.cacheDir
        val specificCacheDir = File(cacheDir, "temp_Video")

        // Create the cache directory if it does not exist
        if (!specificCacheDir.exists()) {
            specificCacheDir.mkdirs()
        }

        // Create the video file
        return File(specificCacheDir, "$fileName.mp4")
    }

    private fun openBottomSheet() {
        stickerBottomSheet.show(supportFragmentManager, StickerBottomSheet.TAG)
    }

    private fun handelEmojisClick() {
        binding.smileEmoji.setOnClickListener {
            binding.smileEmoji.background = ContextCompat.getDrawable(this, R.drawable.rounded_button_selected)
            emojisIndex = 0
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.heartEmoji.setOnClickListener {
            emojisIndex = 1
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.happyEmoji.setOnClickListener {
            emojisIndex = 2
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.fireEmoji.setOnClickListener {
            emojisIndex = FIXED_3_INT
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.kissEmoji.setOnClickListener {
            emojisIndex = FIXED_4_INT
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.ghostEmoji.setOnClickListener {
            emojisIndex = FIXED_5_INT
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.cryEmoji.setOnClickListener {
            emojisIndex = FIXED_6_INT
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.hundredEmoji.setOnClickListener {
            emojisIndex = FIXED_7_INT
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }
    }
    private fun undoLastEmoji() {
        if (emojiViews.isNotEmpty()) {
            val lastEmojiView = emojiViews.removeAt(emojiViews.size - 1)
            binding.emojiContainer.removeView(lastEmojiView)
            if (emojiViews.isEmpty()) {
                lastX = -1f
                lastY = -1f
            } else {
                val lastEmoji = emojiViews.last()
                lastX = lastEmoji.left.toFloat()
                lastY = lastEmoji.top.toFloat()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handelOnTouchListener() {
        if (mimeType != MIME_TYPE_IMAGE_JPEG) {
            binding.emojiContainer.setOnTouchListener(commonTouchListener)
        }
        binding.imagePreview.setOnTouchListener(commonTouchListener)
        binding.smileEmoji.setOnTouchListener(touchListener)
        binding.heartEmoji.setOnTouchListener(touchListener)
        binding.happyEmoji.setOnTouchListener(touchListener)
        binding.fireEmoji.setOnTouchListener(touchListener)
        binding.kissEmoji.setOnTouchListener(touchListener)
        binding.ghostEmoji.setOnTouchListener(touchListener)
        binding.cryEmoji.setOnTouchListener(touchListener)
        binding.hundredEmoji.setOnTouchListener(touchListener)
    }

    private val commonTouchListener = View.OnTouchListener { _, event ->
        val x = event.x
        val y = event.y
        val emojiDrawThreshold = FIXED_80_INT // Minimum distance between emojis
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (selectedItem == EMOJI) {
                    val distance = kotlin.math.sqrt((x - lastX).pow(2) + (y - lastY).pow(2))
                    if (lastX == -1f || lastY == -1f || distance > emojiDrawThreshold) {
                        drawEmojiOnView(x, y, binding.emojiContainer)
                        lastX = x
                        lastY = y
                    }
                } else if (selectedItem == BLUR) {
                    applyBlurEfect(x.toInt() - Constant.FIXED_50_INT, y.toInt() - Constant.FIXED_50_INT)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selectedItem == EMOJI) {
                    listOfRemoveCount.add(count)
                    listOfDrawingView.add(EMOJIS_DRAWING)
                    // Reset the last position after lifting the finger
                    lastX = -1f
                    lastY = -1f
                }
            }
        }
        true
    }

    // Function to draw emoji on the given container at the specified coordinates
    private fun drawEmojiOnView(x: Float, y: Float, container: FrameLayout) {
        if (emojisIndex != null) {
            val emoji = when (emojisIndex) {
                0 -> R.drawable.smile
                1 -> R.drawable.heart
                2 -> R.drawable.laugh
                FIXED_3_INT -> R.drawable.burn
                FIXED_4_INT -> R.drawable.kiss
                FIXED_5_INT -> R.drawable.ghost
                FIXED_6_INT -> R.drawable.sad
                FIXED_7_INT -> R.drawable.hundred
                else -> R.drawable.smile
            }

            val imageView = ImageView(this).apply {
                setImageResource(emoji)
            }
            val imageViewParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = x.toInt() - Constant.FIXED_50_INT
                topMargin = y.toInt() - Constant.FIXED_50_INT // Adjust this value as needed
            }
            container.addView(imageView, imageViewParams)
            count++
            // Add the ImageView to the list
            emojiViews.add(imageView)
        }
    }


    private fun applyBlurEfect(x: Int, y: Int) {
        binding.imagePreview.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(binding.imagePreview.drawingCache)
        binding.imagePreview.isDrawingCacheEnabled = false
        val blurRadius = Constant.FIXED_25_FLOAT
        val rsContext = RenderScript.create(this)
        val startX = maxOf(0, x)
        val startY = maxOf(0, y)
        val width = minOf(bitmap.width - startX, Constant.FIXED_75_INT)
        val height = minOf(bitmap.height - startY, Constant.FIXED_75_INT)
        if (width > 0 && height > 0) {
            val regionBitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height)
            val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val blurInput = Allocation.createFromBitmap(rsContext, regionBitmap)
            val blurOutput = Allocation.createTyped(rsContext, blurInput.type)
            val blurScript = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
            blurScript.setRadius(blurRadius)
            blurScript.setInput(blurInput)
            blurScript.forEach(blurOutput)
            blurOutput.copyTo(blurredBitmap)
            val imageView = ImageView(this).apply {
                setImageBitmap(blurredBitmap)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = startX
                    topMargin = startY
                }
            }
            binding.emojiContainer.addView(imageView)
        }
        rsContext.finish()
    }

    private fun moveDrawViewToTop() {
        savedDrawing = binding.drawView.saveDrawing()
        binding.videoLoader.post {
            val playerViewWidth = binding.videoLoader.width
            val playerViewHeight = binding.videoLoader.height
            val layoutParams = FrameLayout.LayoutParams(playerViewWidth, playerViewHeight).apply {
                // Center the view
                gravity = Gravity.CENTER
            }

            // Apply the new layout parameters with margin
            binding.drawView.layoutParams = layoutParams
            binding.emojiContainer.layoutParams = layoutParams
            videoHeight = playerViewHeight
            videoWidth = playerViewWidth
        }
    }

    private fun openAddTextPopupWindow(attributes: TextAttributes) {
        binding.llEdit.visibility = View.GONE
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val addTextPopupWindowRootView: View = inflater.inflate(R.layout.add_text_popup_window, null)
        val addTextEditText = addTextPopupWindowRootView.findViewById<View>(R.id.add_text_edit_text) as EditText
        addTextEditText.requestFocus()
        colorCodeTextView = attributes.color
        textAlignPosition = attributes.alignment
        fontId = attributes.fontId
        shadowLayerRadius = attributes.shadowLayerRadius
        shadowLayerDx = attributes.shadowLayerDx
        shadowLayerDy = attributes.shadowLayerDy
        lastShadowColor = attributes.shadowLayerColor
        shapeDrawable = attributes.background as GradientDrawable?

        addTextEditText.setText(attributes.text)
        addTextEditText.setTextColor(colorCodeTextView)
        addTextEditText.background = attributes.background
        addTextEditText.typeface = ResourcesCompat.getFont(this@MainActivity, fontId)
        // Apply shadow layer for strikethrough effect
        addTextEditText.setShadowLayer(
            shadowLayerRadius,
            shadowLayerDx,
            shadowLayerDy,
            lastShadowColor
        )
        val addTextDoneTextView = addTextPopupWindowRootView.findViewById<View>(R.id.ivSelectedText) as ImageView
        val colorPicker = addTextPopupWindowRootView.findViewById<View>(R.id.colorSeekBarInPopPop) as ColorSeekBar
        val textSizePicker = addTextPopupWindowRootView.findViewById<View>(R.id.textSizePicker) as SeekBar
        val textCenter = addTextPopupWindowRootView.findViewById<View>(R.id.textCenter) as ImageView
        val ivAddBackGround = addTextPopupWindowRootView.findViewById<View>(R.id.ivAddBackGround) as ImageView
        val ivStrikethrough = addTextPopupWindowRootView.findViewById<View>(R.id.ivStrikethrough) as ImageView
        val etLinearLayout = addTextPopupWindowRootView.findViewById<View>(R.id.llEditTextView) as LinearLayout
        val fontPickerRecyclerView = addTextPopupWindowRootView.findViewById<View>(
            R.id.font_picker_recycler_view
        ) as RecyclerView
        val layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        fontPickerRecyclerView.layoutManager = layoutManager
        fontPickerRecyclerView.setHasFixedSize(true)
        val fontPickerAdapter = FontPickerAdapter(this@MainActivity, fontList)
        fontPickerAdapter.setOnFontPickerClickListener(object : FontPickerAdapter.OnFontPickerClickListener {
            override fun onFontPickerClickListener(fontCode: Int) {
                fontId = fontCode
                val typeface = ResourcesCompat.getFont(this@MainActivity, fontCode)
                addTextEditText.typeface = typeface
            }
        })
        fontPickerRecyclerView.adapter = fontPickerAdapter
        val minFontSize = 10
        val maxFontSize = 80
        val defaultFontSize = 30

        when (textAlignPosition) {
            TEXT_ALIGN_RIGHT -> {
                etLinearLayout.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                addTextEditText.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }

            TEXT_ALIGN_LEFT -> {
                etLinearLayout.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                addTextEditText.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }

            else -> {
                etLinearLayout.gravity = Gravity.CENTER
                addTextEditText.gravity = Gravity.CENTER
            }
        }

        textSizePicker.max = maxFontSize - minFontSize
        textSizePicker.progress = defaultFontSize - minFontSize
        lastTextSize = (defaultFontSize - minFontSize).toFloat()
        addTextEditText.textSize = defaultFontSize.toFloat()
        textSizePicker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = minFontSize + progress
                // Set the new font size to the TextView
                addTextEditText.textSize = fontSize.toFloat()
                lastTextSize = fontSize.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        lastTextSize = attributes.size
        addTextEditText.textSize = lastTextSize

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (firstVisiblePosition != RecyclerView.NO_POSITION) {
                        val viewAtPosition = layoutManager.findViewByPosition(firstVisiblePosition)
                        if (viewAtPosition != null) {
                            if (fontList[firstVisiblePosition].fontId != null) {
                                fontList[firstVisiblePosition].fontId?.let {
                                    fontId = it
                                }
                                val typeface = ResourcesCompat.getFont(this@MainActivity, fontId)
                                addTextEditText.typeface = typeface
                                fontPickerAdapter.setSelectedPosition(firstVisiblePosition)
                            }
                        }
                    }
                }
            }
        }

        fontPickerRecyclerView.addOnScrollListener(scrollListener)
        colorPicker.color = DEFOULT_TEXT_COLOR_CODE
        colorPicker.setOnColorChangeListener { _, color ->
            val newColorCode: String = decimalToHex(color)
            if (isBackGroundAdd) {
                if (shapeDrawable == null) {
                    createShapeDrawable() // Initialize shapeDrawable if it's null
                }
                shapeDrawable?.setColor(color) // Safe call using ?. to avoid null pointer exception
            } else if (ivStrikethroughClicked) {
                lastShadowColor = color
                addTextEditText.setShadowLayer(15.0f, 0f, 0f, color)
            } else {
                colorCodeTextView = color
                addTextEditText.setTextColor(Color.parseColor(newColorCode))
            }

            val textContent: String = if (addTextEditText.text.toString().startsWith(
                    " "
                )
            ) {
                addTextEditText.text.toString()
                    .drop(1)
            } else {
                addTextEditText.text.toString() // remove first letter space
            }
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
        }

        val pop = PopupWindow(this@MainActivity)
        pop.contentView = addTextPopupWindowRootView
        pop.width = LinearLayout.LayoutParams.MATCH_PARENT
        pop.height = LinearLayout.LayoutParams.MATCH_PARENT
        pop.isFocusable = true
        pop.setBackgroundDrawable(null)
        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        addTextDoneTextView.setOnClickListener { view ->
            hideKeyboard(view)

            val newText = addTextEditText.text.toString()
            if (newText.isNotBlank()) {
                val newTextProperties: TextAttributes?

                if (selectedItemAddText != null) {
                    selectedItemAddText = selectedItemAddText?.copy(
                        text = newText,
                        color = colorCodeTextView,
                        size = lastTextSize,
                        alignment = textAlignPosition,
                        background = shapeDrawable,
                        fontId = fontId
                    )
                    newTextProperties = selectedItemAddText
                } else {
                    // Add a new text entry
                    newTextProperties = TextAttributes(
                        text = newText,
                        color = colorCodeTextView,
                        size = lastTextSize,
                        alignment = textAlignPosition,
                        background = shapeDrawable,
                        fontId = fontId,
                        shadowLayerRadius = shadowLayerRadius,
                        shadowLayerDx = shadowLayerDx,
                        shadowLayerDy = shadowLayerDy,
                        shadowLayerColor = lastShadowColor,
                    )
                    texts.add(newTextProperties)
                }
                newTextProperties?.let {
                    addTextOnImage(newText, it)
                } ?: Log.d("SnapEditorActivity", "Text properties are null, unable to add text.")
            }
            selectedItem = null
            addTextEditText.text.clear()
            pop.dismiss()
        }
        textCenter.setOnClickListener {
            val textContent: String = if (addTextEditText.text.toString().startsWith(
                    " "
                )
            ) {
                addTextEditText.text.toString()
                    .drop(1)
            } else {
                addTextEditText.text.toString() // remove first letter space
            }
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
            when (etLinearLayout.gravity) {
                Gravity.CENTER -> {
                    etLinearLayout.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    addTextEditText.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    textAlignPosition = TEXT_ALIGN_RIGHT
                }

                Gravity.END or Gravity.CENTER_VERTICAL -> {
                    etLinearLayout.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    addTextEditText.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    textAlignPosition = TEXT_ALIGN_LEFT
                }

                else -> {
                    etLinearLayout.gravity = Gravity.CENTER
                    addTextEditText.gravity = Gravity.CENTER
                    textAlignPosition = TEXT_ALIGN_CENTER
                }
            }
        }
        ivAddBackGround.setOnClickListener {
            val textContent: String = if (addTextEditText.text.toString().startsWith(
                    " "
                )
            ) {
                addTextEditText.text.toString()
                    .drop(1)
            } else {
                addTextEditText.text.toString() // remove first letter space
            }
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent)
            isBackGroundAdd = !isBackGroundAdd
            ivStrikethroughClicked = false
            ivStrikethrough.setImageResource(R.drawable.ic_strikethrough)
            if (isBackGroundAdd) {
                ivAddBackGround.setImageResource(R.drawable.fil_icon)
                // Initialize shapeDrawable if it is null
                if (shapeDrawable == null) {
                    createShapeDrawable()
                }
                // Apply the drawable if it's initialized
                addTextEditText.background = shapeDrawable
            } else {
                shapeDrawable = null
                ivAddBackGround.setImageResource(R.drawable.bg_add_text)
                addTextEditText.background = null // Clear background
            }
        }

        ivStrikethrough.setOnClickListener {
            val textContent: String = if (addTextEditText.text.toString().startsWith(
                    " "
                )
            ) {
                addTextEditText.text.toString()
                    .drop(1)
            } else {
                addTextEditText.text.toString() // remove first letter space
            }
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
            ivStrikethroughClicked = !ivStrikethroughClicked
            isBackGroundAdd = false
            ivAddBackGround.setImageResource(R.drawable.bg_add_text)
            strikethroughApplied = true
            if (ivStrikethroughClicked) {
                ivStrikethrough.setImageResource(R.drawable.select_stroke)
                addTextEditText.setShadowLayer(15.0f, 0f, 0f, R.color.textColor_red)
                shadowLayerRadius = 15.0f
                shadowLayerDx = 0f
                shadowLayerDy = 0f
                lastShadowColor = R.color.textColor_red
            } else {
                ivStrikethrough.setImageResource(R.drawable.ic_strikethrough)
                addTextEditText.setShadowLayer(0f, 0f, 0f, 0)
                shadowLayerRadius = 0f
                shadowLayerDx = 0f
                shadowLayerDy = 0f
                shadowLayerColor = 0
            }
        }

        pop.setOnDismissListener {
            binding.llEdit.visibility = View.VISIBLE
        }
    }
    fun onClickVideoPlayPause() {
        if (mPlayer.isPlaying) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mPlayer.pause()
            binding.tvPlay.setImageResource(R.drawable.ic_video_play)
            binding.handlerTop.visibility = View.VISIBLE
        } else {
            if (mResetSeekBar) {
                mResetSeekBar = false
                mPlayer.seekTo(mStartPosition)
                binding.handlerTop.visibility = View.VISIBLE
                setProgressBarPosition(0)
            }
            mResetSeekBar = false
            binding.handlerTop.visibility = View.VISIBLE
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mPlayer.play()
            binding.tvPlay.setImageResource(R.drawable.baseline_pause_circle_outline_24)
        }
    }

    private fun addTextOnImage(content: String, attributes: TextAttributes) {
        val textContent = if (content.startsWith(
                " "
            )
        ) {
            attributes.text.drop(1)
        } else {
            attributes.text // remove first letter space
        }
        val textView = createConfiguredTextView(
            textContent = textContent,
            attributes = TextAttributes(
                text = textContent,
                color = colorCodeTextView,
                size = lastTextSize,
                alignment = textAlignPosition,
                background = shapeDrawable,
                fontId = fontId,
                shadowLayerRadius = shadowLayerRadius,
                shadowLayerDx = shadowLayerDx,
                shadowLayerDy = shadowLayerDy,
                shadowLayerColor = lastShadowColor
            ),
            backgroundAdded = backgroundAdded,
            strikethroughApplied = strikethroughApplied,
            lastShadowColor = lastShadowColor
        )

        val textViewParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = when (textAlignPosition) {
                TEXT_ALIGN_LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
                TEXT_ALIGN_RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
                else -> Gravity.CENTER or Gravity.CENTER_VERTICAL
            }
        }
        binding.emojiContainer.addView(textView, textViewParams)
        currentTextView = textView
        ivStrikethroughClicked = false
        isBackGroundAdd = false
        setTextViewTouchListener(textView)
    }

    private fun createConfiguredTextView(
        textContent: String,
        attributes: TextAttributes,
        backgroundAdded: Boolean,
        strikethroughApplied: Boolean,
        lastShadowColor: Int
    ): TextView {
        return TextView(this).apply {
            text = textContent
            textSize = attributes.size
            setTextColor(attributes.color)
            textAlignment = when (attributes.alignment) {
                TEXT_ALIGN_LEFT -> View.TEXT_ALIGNMENT_TEXT_START
                TEXT_ALIGN_RIGHT -> View.TEXT_ALIGNMENT_TEXT_END
                else -> View.TEXT_ALIGNMENT_CENTER
            }
            background = attributes.background
            typeface = ResourcesCompat.getFont(this@MainActivity, attributes.fontId)

            if (backgroundAdded) {
                background = attributes.background
            }

            if (strikethroughApplied) {
                setShadowLayer(15.0f, 0f, 0f, lastShadowColor)
            }

            setOnClickListener {
                binding.emojiContainer.removeView(this)
                openAddTextPopupWindow(attributes)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTextViewTouchListener(textView: TextView) {
        textView.setOnTouchListener { v, event ->
            val view = v as TextView
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Capture starting point
                    startX = event.x
                    startY = event.y
                    isDragging = false
                    binding.deleteTv.visibility = View.GONE
                    view.bringToFront()
                    viewTransformation(view, event)

                    true // Return true to indicate we are handling the touch event
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate the movement distance
                    val deltaX = abs(event.x - startX)
                    val deltaY = abs(event.y - startY)

                    if (deltaX > FIXED_10_INT || deltaY > FIXED_10_INT) {
                        isDragging = true
                        view.bringToFront()
                        v.x += event.x - startX
                        v.y += event.y - startY

                        viewTransformation(view, event)
                    }
                    true // Return true to indicate we are handling the touch event
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        v.performClick()
                    }
                    binding.deleteTv.visibility = View.GONE
                    view.bringToFront()
                    viewTransformation(view, event)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                binding.deleteTv.visibility = View.VISIBLE
                binding.tvDone.visibility = View.GONE
                binding.llOption.visibility = View.GONE
                binding.deleteAppCompatImageView.visibility = View.GONE

                binding.tvDone.isVisible = false
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY

                start.set(event.x, event.y)
                isOutSide = false
                mode = DRAG
                lastMode = DRAG
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > FIXED_10_FLOAT) {
                    midPoint(mid, event)
                    mode = ZOOM
                    lastMode = ZOOM
                }

                lastEvent = FloatArray(FIXED_4_INT)
                lastEvent?.set(0, event.getX(0))
                lastEvent?.set(1, event.getX(1))
                lastEvent?.set(2, event.getY(0))
                lastEvent?.set(FIXED_3_INT, event.getY(1))
                d = rotation(event)
            }

            MotionEvent.ACTION_UP -> {
                if (isViewOverlapping(view, binding.deleteTv)) {
                    // Remove the text view from the layout
                    colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
                    fontId = R.font.american_typewriter
                    binding.emojiContainer.removeView(view)
                } else {
                    val viewY = view.y
                    if (mimeType == MIME_TYPE_IMAGE_JPEG) {
                        if (viewY > imageHeight - FIXED_80_INT) {
                            colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
                            fontId = R.font.american_typewriter
                            binding.emojiContainer.removeView(view)

                        } else if (viewY < 0) {
                            colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
                            fontId = R.font.american_typewriter
                            binding.emojiContainer.removeView(view)

                        }
                    } else {
                        if (viewY > videoHeight - FIXED_80_INT) {
                            colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
                            fontId = R.font.american_typewriter
                            binding.emojiContainer.removeView(view)

                        } else if (viewY < 0) {
                            colorCodeTextView = DEFOULT_TEXT_COLOR_CODE
                            fontId = R.font.american_typewriter
                            binding.emojiContainer.removeView(view)
                        }
                    }
                }
                binding.deleteTv.visibility = View.GONE
                binding.layout.visibility = View.VISIBLE
                binding.tvDone.visibility = View.VISIBLE
                binding.llOption.visibility = View.VISIBLE
                if (mimeType == MIME_TYPE_VIDEO_MP4) {
                    binding.deleteAppCompatImageView.visibility = View.VISIBLE
                    binding.timeLineFrame.visibility = View.VISIBLE
                    binding.tvPlay.visibility = View.VISIBLE
                } else {
                    binding.timeLineFrame.visibility = View.GONE
                    binding.tvPlay.visibility = View.GONE
                    binding.tvDone.isVisible = true
                    binding.deleteAppCompatImageView.visibility = View.GONE
                }
                isZoomAndRotate = false
            }

            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false
                        view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(0).start()
                    }
                    if (mode == ZOOM && event.pointerCount == 2) {
                        val newDist1 = spacing(event)
                        if (newDist1 > FIXED_10_FLOAT) {
                            val scale = newDist1 / oldDist * view.scaleX
                            view.scaleX = scale
                            view.scaleY = scale
                        }
                        lastEvent?.let {
                            val newRot = rotation(event)
                            view.rotation += (newRot - d)
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    val touchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Get the touch coordinates relative to the screen
                val touchX = event.rawX
                val touchY = event.rawY

                // Determine which emoji is being touched
                val emojiViews = listOf(
                    binding.smileEmoji to R.drawable.ic_smile,
                    binding.heartEmoji to R.drawable.ic_heart_emoji,
                    binding.happyEmoji to R.drawable.ic_laugh,
                    binding.fireEmoji to R.drawable.ic_burn,
                    binding.kissEmoji to R.drawable.ic_kiss,
                    binding.ghostEmoji to R.drawable.ic_ghost,
                    binding.cryEmoji to R.drawable.ic_sad,
                    binding.hundredEmoji to R.drawable.ic_hundred
                )

                var touchedView: View? = null
                var emojiDrawableResId = 0

                emojiViews.forEach { (view, drawableResId) ->
                    if (isPointInsideView(touchX, touchY, view)) {
                        emojiDrawableResId = drawableResId
                        touchedView = view
                    }
                }

                // Update the highlight view position, size, and drawable if an emoji is touched
                touchedView?.let { updateHighlightView(touchY, emojiDrawableResId, it) }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Hide the highlight view when touch is released
                binding.highlightView.visibility = View.GONE
            }
        }
        true
    }

    private fun updateHighlightView(touchY: Float, emojiDrawableResId: Int, emojiView: View) {
        val highlightView = binding.highlightView
        val layoutParams = highlightView.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = (touchY - Constant.FIXED_160_INT).toInt()
        highlightView.layoutParams = layoutParams
        binding.highlightViewEmoji.setImageResource(emojiDrawableResId)
        highlightView.visibility = View.VISIBLE
        resetEmojiBackgrounds()
        emojiView.background = ContextCompat.getDrawable(emojiView.context, R.drawable.rounded_button_selected)

        // Update emojisIndex based on the selected emoji view
        when (emojiView.id) {
            R.id.smileEmoji -> emojisIndex = 0
            R.id.heartEmoji -> emojisIndex = 1
            R.id.happyEmoji -> emojisIndex = 2
            R.id.fireEmoji -> emojisIndex = FIXED_3_INT
            R.id.kissEmoji -> emojisIndex = FIXED_4_INT
            R.id.ghostEmoji -> emojisIndex = Constant.FIXED_5_INT
            R.id.cryEmoji -> emojisIndex = Constant.FIXED_6_INT
            R.id.hundredEmoji -> emojisIndex = Constant.FIXED_7_INT
        }
    }

    private fun resetEmojiBackgrounds() {
        val emojiViews = listOf(
            binding.smileEmoji,
            binding.heartEmoji,
            binding.happyEmoji,
            binding.fireEmoji,
            binding.kissEmoji,
            binding.ghostEmoji,
            binding.cryEmoji,
            binding.hundredEmoji
        )

        emojiViews.forEach { emojiView ->
            emojiView.background = null
        }
    }

    // Function to check if a point is inside a view
    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        return (x >= viewX && x <= viewX + view.width && y >= viewY && y <= viewY + view.height)
    }


    private fun initializeFontList() {
        fontList.add(FontDetails(R.font.helvetica, "Classic"))
        fontList.add(FontDetails(R.font.qochy_demo, "Elegance"))
        fontList.add(FontDetails(R.font.retro_side, "Retro"))
        fontList.add(FontDetails(R.font.olivera, "Vintage"))
        fontList.add(FontDetails(R.font.american_typewriter, "AmericanTypewriter"))
        fontList.add(FontDetails(R.font.avenir_heavy, "Avenir-Heavy"))
        fontList.add(FontDetails(R.font.chalkboard_regular, "ChalkboardSE-Regular"))
        fontList.add(FontDetails(R.font.arial_mt, "ArialMT"))
        fontList.add(FontDetails(R.font.bangla_sangam_mn, "BanglaSangamMN"))
        fontList.add(FontDetails(R.font.liberator, "LIBERATOR"))
        fontList.add(FontDetails(R.font.muncie, "MUNCIE"))
        fontList.add(FontDetails(R.font.abraham_lincoln, "Abraham lincoln"))
        fontList.add(FontDetails(R.font.airship_27_regular, "AIRSHIP 27"))
        fontList.add(FontDetails(R.font.arvil_sans, "ARVIL"))
        fontList.add(FontDetails(R.font.bender_lnline, "BENDER"))
        fontList.add(FontDetails(R.font.blanch_condensed, "BLANCH"))
        fontList.add(FontDetails(R.font.cubano_regular_webfont, "CUBANO"))
        fontList.add(FontDetails(R.font.franchise_bold, "FRANCHISE"))
        fontList.add(FontDetails(R.font.geared_slab, "Geared Slab"))
        fontList.add(FontDetails(R.font.governor, "GOVERNOR"))
        fontList.add(FontDetails(R.font.haymaker, "HAYMAKER"))
        fontList.add(FontDetails(R.font.homestead_regular, "HOMESTEAD"))
        fontList.add(FontDetails(R.font.maven_pro_light_200, "Maven Pro Light"))
        fontList.add(FontDetails(R.font.mensch, "MENSCH"))
        fontList.add(FontDetails(R.font.sullivan_regular, "SULLIVAN"))
        fontList.add(FontDetails(R.font.tommaso, "TOMMASO"))
        fontList.add(FontDetails(R.font.valencia_regular, "VALENCIA"))
        fontList.add(FontDetails(R.font.vevey, "VEVEY"))
    }


    private fun decimalToHex(decimalColor: Int): String {
        // Convert decimal color to hexadecimal and strip the '0x' prefix
        val hexColor = Integer.toHexString(decimalColor and COLOR_0xFFFFFF).uppercase()
        // Ensure the hex color is 6 characters long
        val paddedHexColor = hexColor.padStart(6, '0')
        // Add the '#' prefix
        return "#$paddedHexColor"
    }




    private fun createShapeDrawable() {
        shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            cornerRadius = 10 * resources.displayMetrics.density
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setPadding(
                    (10 * resources.displayMetrics.density).toInt(),
                    (4 * resources.displayMetrics.density).toInt(),
                    (10 * resources.displayMetrics.density).toInt(),
                    (5 * resources.displayMetrics.density).toInt()
                )
            }
        }
    }

    private fun isViewOverlapping(view: View, otherView: View): Boolean {
        val viewRect = Rect()
        val otherRect = Rect()
        view.getHitRect(viewRect)
        otherView.getHitRect(otherRect)
        return Rect.intersects(viewRect, otherRect)
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    fun setVideoBackgroundColor(@ColorInt color: Int) = with(binding) {
        binding.videoContainer.setBackgroundColor(color)
        layout.setBackgroundColor(color)
    }

    fun setOnTrimVideoListener(onVideoEditedListener: OnVideoEditedEvent): MainActivity {
        mOnVideoEditedListener = onVideoEditedListener
        return this
    }

    fun setVideoURI(videoURI: Uri): MainActivity {
        mSrc = videoURI
        mPlayer = ExoPlayer.Builder(this).build()

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(
            dataSourceFactory
        ).createMediaSource(MediaItem.fromUri(videoURI))

        mPlayer.setMediaSource(videoSource)
        mPlayer.prepare()
        mPlayer.playWhenReady = true // Start playback automatically

        binding.videoLoader.also {
            it.player = mPlayer
            it.useController = false
        }
        mPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                mOnVideoEditedListener?.onError("Something went wrong reason : ${error.localizedMessage}")
            }

            @SuppressLint("UnsafeOptInUsageError")
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > videoSize.height) {
                    binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                } else {
                    binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    mPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                }

                onVideoPrepared(mPlayer)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    // Video has ended, seek back to the start
                    mResetSeekBar = true
                    mPlayer.seekTo(mStartPosition)
                    binding.handlerTop.visibility = View.VISIBLE
                    setProgressBarPosition(0)
                    binding.tvPlay.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                    // You might want to start playing the video again here if needed
                    mPlayer.play()
                }
            }
        })

        binding.videoLoader.requestFocus()
        binding.timeLineView.setVideo(mSrc)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this@MainActivity, mSrc)
        val metaDateWidth = mediaMetadataRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
        )?.toInt() ?: 0
        val metaDataHeight = mediaMetadataRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
        )?.toInt() ?: 0

        // If the rotation is 90 or 270 the width and height will be transposed.
        when (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt()) {
            Constant.FIXED_90_INT, Constant.FIXED_270_INT -> {
                originalVideoWidth = metaDataHeight
                originalVideoHeight = metaDateWidth
            }

            else -> {
                originalVideoWidth = metaDateWidth
                originalVideoHeight = metaDataHeight
            }
        }

        return this
    }
    fun setMinDuration(minDuration: Int): MainActivity {
        mMinDuration = minDuration * Constant.FIXED_1000_INT
        return this
    }

    fun getDurations(): Pair<Int, Int> {
        val startSeconds = (mStartPosition / Constant.FIXED_1000_INT).toInt()
        val endSeconds = (mEndPosition / Constant.FIXED_1000_INT).toInt()
        return Pair(startSeconds, endSeconds)
    }

    private fun setProgressBarPosition(position: Long) {
        if (mDuration > 0) binding.handlerTop.progress = (Constant.FIXED_1000_MILLISECOND * position / mDuration).toInt()
    }

    private fun updateVideoProgress(time: Long) {
        if (time <= mStartPosition && time <= mEndPosition) {
            binding.handlerTop.visibility = View.GONE
        } else {
            binding.handlerTop.visibility = View.VISIBLE
        }
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mPlayer.pause()
            mResetSeekBar = true
            return
        }
        setProgressBarPosition(time)
    }

    private fun onVideoPrepared(mp: ExoPlayer) {
        if (isVideoPrepared) return
        isVideoPrepared = true
        val videoWidth = mp.videoSize.width
        val videoHeight = mp.videoSize.height
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = binding.layoutSurfaceView.width
        val screenHeight = binding.layoutSurfaceView.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = binding.videoLoader.layoutParams

        if (videoProportion > screenProportion) {
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            lp.height = screenHeight
        }
        videoPlayerWidth = lp.width
        videoPlayerHeight = lp.height
        binding.videoLoader.layoutParams = lp

        mDuration = mPlayer.duration
        setSeekBarPosition()
        setTimeFrames()
    }

    private fun setSeekBarPosition() {
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * Constant.FIXED_100_INT / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * Constant.FIXED_100_INT / mDuration))
            }

            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * Constant.FIXED_100_INT / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * Constant.FIXED_100_INT / mDuration))
            }

            else -> {
                mStartPosition = 0L
                mEndPosition = mDuration
            }
        }
        mPlayer.seekTo(mStartPosition)
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
    }

    private fun setTimeFrames() {
        val seconds = resources.getString(R.string.short_seconds)
        binding.textTimeSelection.text = String.format(
            Locale.ENGLISH,
            "%s %s - %s %s",
            TrimVideoUtils.stringForTime(mStartPosition),
            seconds,
            TrimVideoUtils.stringForTime(mEndPosition),
            seconds
        )
    }


    fun getVideoDimensions(videoPath: String): Size? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoPath)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()

            if (width != null && height != null) {
                Size(width, height)
            } else {
                null
            }
        } catch (e: IOException) {
            Timber.tag("SnapEditorActivity").d("Error:->$e")
            null
        } finally {
            retriever.release()
        }
    }

    override fun getResult(uri: Uri) {

    }

    override fun onError(message: String) {
    }

    override fun onProgress(percentage: Int) {
    }

    private class MessageHandler(view: MainActivity) : Handler(Looper.getMainLooper()) {
        private val mView: WeakReference<MainActivity> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get() ?: return
            view.notifyProgressUpdate(true)
            if (view.binding.videoLoader.player?.isPlaying == true) sendEmptyMessageDelayed(0, Constant.FIXED_10_MILLISECOND)
        }
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0L) return
        val position = mPlayer.currentPosition // binding.videoLoader.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position.toFloat(), mDuration, (position * Constant.FIXED_100_INT / mDuration))
            }
        } else {
            mListeners[0].updateProgress(
                position.toFloat(),
                mDuration,
                (position * Constant.FIXED_100_INT / mDuration)
            )
        }
    }

    private fun setDrawable(indexOfEmojis: Int) {
        val emojiButtons = listOf(
            binding.smileEmoji,
            binding.heartEmoji,
            binding.happyEmoji,
            binding.fireEmoji,
            binding.kissEmoji,
            binding.ghostEmoji,
            binding.cryEmoji,
            binding.hundredEmoji
        )
        emojiButtons.forEach { it.background = null }
        if (indexOfEmojis in emojiButtons.indices) {
            emojiButtons[indexOfEmojis].background = ContextCompat.getDrawable(this, R.drawable.rounded_button_selected)
        } else {
            binding.smileEmoji.background = null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            data?.let {
                val resultUri = UCrop.getOutput(it)
                resultUri?.let { uri ->
                    binding.imagePreview.setImageURI(uri)
                    selectedItem = null
                    saveImageToLocalStorage(uri)
                    setupMediaIfNeeded(uri.toString())
                }
            }
        } else if (requestCode == CROP_REQUEST && resultCode == RESULT_OK) {
            outputMediaUrl = cropVideoPath
            setupMediaIfNeeded(outputMediaUrl)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            data?.let {
                val cropError = UCrop.getError(it)
                cropError?.let { error ->
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveImageToLocalStorage(imageUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val dir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES)
            val demoDir = File(dir, "demo")
            if (!demoDir.exists()) {
                demoDir.mkdir()
            }
            val file = File(demoDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            mediaUrl = file.path
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}