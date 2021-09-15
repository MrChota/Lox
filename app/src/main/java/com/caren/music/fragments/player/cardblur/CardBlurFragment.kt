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
package com.caren.music.fragments.player.cardblur

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.caren.music.NEW_BLUR_AMOUNT
import com.caren.music.R
import com.caren.music.databinding.FragmentCardBlurPlayerBinding
import com.caren.music.fragments.base.AbsPlayerFragment
import com.caren.music.fragments.player.PlayerAlbumCoverFragment
import com.caren.music.fragments.player.normal.PlayerFragment
import com.caren.music.glide.BlurTransformation
import com.caren.music.glide.GlideApp
import com.caren.music.glide.RetroGlideExtension
import com.caren.music.glide.RetroMusicColoredTarget
import com.caren.music.helper.MusicPlayerRemote
import com.caren.music.model.Song
import com.caren.music.util.color.MediaNotificationProcessor

class CardBlurFragment : AbsPlayerFragment(R.layout.fragment_card_blur_player),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor
    private lateinit var playbackControlsFragment: CardBlurPlaybackControlsFragment

    private var _binding: FragmentCardBlurPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return Color.WHITE
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)

        binding.playerToolbar.setTitleTextColor(Color.WHITE)
        binding.playerToolbar.setSubtitleTextColor(Color.WHITE)
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardBlurPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
    }

    private fun setUpSubFragments() {
        playbackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as CardBlurPlaybackControlsFragment
        (childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment?)?.setCallbacks(
            this
        )
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            setTitleTextColor(Color.WHITE)
            setSubtitleTextColor(Color.WHITE)
            ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)
        }.setOnMenuItemClickListener(this)
    }

    override fun onServiceConnected() {
        updateIsFavorite()
        updateBlur()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
        updateBlur()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.playerToolbar.apply {
            title = song.title
            subtitle = song.artistName
        }
    }

    private fun updateBlur() {
        val blurAmount = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getInt(NEW_BLUR_AMOUNT, 25)
        binding.colorBackground.clearColorFilter()
        GlideApp.with(requireActivity()).asBitmapPalette()
            .songCoverOptions(MusicPlayerRemote.currentSong)
            .load(RetroGlideExtension.getSongModel(MusicPlayerRemote.currentSong))
            .dontAnimate()
            .transform(
                BlurTransformation.Builder(requireContext()).blurRadius(blurAmount.toFloat())
                    .build()
            )
            .into(object : RetroMusicColoredTarget(binding.colorBackground) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    if (colors.backgroundColor == defaultFooterColor) {
                        binding.colorBackground.setColorFilter(colors.backgroundColor)
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == NEW_BLUR_AMOUNT) {
            updateBlur()
        }
    }

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}
