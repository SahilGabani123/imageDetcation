package com.customlibrary.imagedetaction.ui.main.stickers

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.customlibrary.imagedetaction.R
import com.customlibrary.imagedetaction.databinding.StickerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "StickerBottomSheet"
        const val N_4 = 4
    }

    private lateinit var binding: StickerBottomSheetBinding
    private lateinit var stickerBottomSheetAdapter: StickerBottomSheetAdapter
    private lateinit var listOfSticker: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = (view.parent as View)
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        listOfSticker = arrayListOf()

        listOfSticker.add(R.drawable.cuppy_smile_sticker)
        listOfSticker.add(R.drawable.avocado_sticker)
        listOfSticker.add(R.drawable.balloons_sticker)
        listOfSticker.add(R.drawable.bunting_sticker)
        listOfSticker.add(R.drawable.cake_sticker)
        listOfSticker.add(R.drawable.confetti_sticker)
        listOfSticker.add(R.drawable.happy_birthday_sticker)
        listOfSticker.add(R.drawable.heart_sticker)
        listOfSticker.add(R.drawable.hello_sticker)
        listOfSticker.add(R.drawable.milkshake_sticker)
        listOfSticker.add(R.drawable.moon_sticker)
        listOfSticker.add(R.drawable.panda_sticker)
        listOfSticker.add(R.drawable.pizza_sticker)
        listOfSticker.add(R.drawable.rainbow_sticker)
        listOfSticker.add(R.drawable.rob_sticker)
        listOfSticker.add(R.drawable.star_sticker)
        listOfSticker.add(R.drawable.sun_sticker)
        listOfSticker.add(R.drawable.wreath_sticker)
        listOfSticker.add(R.drawable.sticker_banana)
        listOfSticker.add(R.drawable.fried_egg_sticker)
        listOfSticker.add(R.drawable.pancake_sticker)
        listOfSticker.add(R.drawable.sandwich_sticker)
        listOfSticker.add(R.drawable.vegetable_sticker)
        listOfSticker.add(R.drawable.wine_sticker)
        listOfSticker.add(R.drawable.rocket_sticker)
        listOfSticker.add(R.drawable.victory_sticker)
        listOfSticker.add(R.drawable.glasses_sticker)
        listOfSticker.add(R.drawable.arrow_own_sticker)
        listOfSticker.add(R.drawable.award_sticker)
        listOfSticker.add(R.drawable.bravo_sticker)
        listOfSticker.add(R.drawable.butterfly_sticker)
        listOfSticker.add(R.drawable.good_morning_sticker)
        listOfSticker.add(R.drawable.sparrow_sticker)
        listOfSticker.add(R.drawable.cool_sticker)
        listOfSticker.add(R.drawable.donut_sticker)
        listOfSticker.add(R.drawable.campfire_sticker)
        listOfSticker.add(R.drawable.dream_big_sticker)
        listOfSticker.add(R.drawable.fried_chicken_sticker)
        listOfSticker.add(R.drawable.haha_sticker)
        listOfSticker.add(R.drawable.happy_face_sticker)
        listOfSticker.add(R.drawable.hot_sticker)
        listOfSticker.add(R.drawable.ice_cream_sticker)
        listOfSticker.add(R.drawable.lol_sticker)
        listOfSticker.add(R.drawable.popsicle_sticker)
        listOfSticker.add(R.drawable.bicycle_sticker)
        listOfSticker.add(R.drawable.bouquet_sticker)

        stickerBottomSheetAdapter = StickerBottomSheetAdapter()
        stickerBottomSheetAdapter.list = listOfSticker
        handelBindingData()
    }

    private fun handelBindingData() {
        binding.ivAddSticker.setOnClickListener {
            dismiss()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        val gridLayoutManager = GridLayoutManager(requireContext(), N_4)

        binding.rvSticker.layoutManager = gridLayoutManager
        binding.rvSticker.adapter = stickerBottomSheetAdapter
    }

    override fun onStart() {
        super.onStart()
        // Adjust BottomSheetDialogFragment to full screen
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.peekHeight = WindowManager.LayoutParams.MATCH_PARENT
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
