/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.caren.music.fragments.player.flat

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.caren.music.R
import com.caren.music.databinding.FragmentFlatPlayerBinding
import com.caren.music.fragments.base.AbsPlayerFragment
import com.caren.music.fragments.player.PlayerAlbumCoverFragment
import com.caren.music.helper.MusicPlayerRemote
import com.caren.music.model.Song
import com.caren.music.util.PreferenceUtil
import com.caren.music.util.ViewUtil
import com.caren.music.util.color.MediaNotificationProcessor
import com.caren.music.views.DrawableGradient

class FlatPlayerFragment : AbsPlayerFragment(R.layout.fragment_flat_player) {
    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var valueAnimator: ValueAnimator? = null
    private lateinit var controlsFragment: FlatPlaybackControlsFragment
    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private var _binding: FragmentFlatPlayerBinding? = null
    private val binding get() = _binding!!


    private fun setUpSubFragments() {
        controlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as FlatPlaybackControlsFragment
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_player)
        binding.playerToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.playerToolbar.setOnMenuItemClickListener(this)
        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
            requireActivity()
        )
    }

    private fun colorize(i: Int) {
        if (valueAnimator != null) {
            valueAnimator?.cancel()
        }

        valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), android.R.color.transparent, i)
        valueAnimator?.addUpdateListener { animation ->
            val drawable = DrawableGradient(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(animation.animatedValue as Int, android.R.color.transparent), 0
            )
            binding.colorGradientBackground.background = drawable
        }
        valueAnimator?.setDuration(ViewUtil.RETRO_MUSIC_ANIM_TIME.toLong())?.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFlatPlayerBinding.bind(view)
        setUpPlayerToolbar()
        setUpSubFragments()
    }

    override fun onShow() {
        controlsFragment.show()
    }

    override fun onHide() {
        controlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        val isLight = ColorUtil.isColorLight(paletteColor)
        return if (PreferenceUtil.isAdaptiveColor)
            MaterialValueHelper.getPrimaryTextColor(requireContext(), isLight)
        else
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal)
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        controlsFragment.setColor(color)
        libraryViewModel.updateColor(color.backgroundColor)
        val isLight = ColorUtil.isColorLight(color.backgroundColor)
        val iconColor = if (PreferenceUtil.isAdaptiveColor)
            MaterialValueHelper.getPrimaryTextColor(requireContext(), isLight)
        else
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal)
        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            iconColor,
            requireActivity()
        )
        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color.backgroundColor)
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
