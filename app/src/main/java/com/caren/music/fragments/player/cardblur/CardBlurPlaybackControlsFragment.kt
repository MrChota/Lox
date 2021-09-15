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

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.TintHelper
import com.caren.music.R
import com.caren.music.databinding.FragmentCardBlurPlayerPlaybackControlsBinding
import com.caren.music.extensions.applyColor
import com.caren.music.extensions.hide
import com.caren.music.extensions.show
import com.caren.music.fragments.base.AbsPlayerControlsFragment
import com.caren.music.helper.MusicPlayerRemote
import com.caren.music.helper.MusicProgressViewUpdateHelper
import com.caren.music.helper.PlayPauseButtonOnClickHandler
import com.caren.music.misc.SimpleOnSeekbarChangeListener
import com.caren.music.service.MusicService
import com.caren.music.util.MusicUtil
import com.caren.music.util.PreferenceUtil
import com.caren.music.util.color.MediaNotificationProcessor

class CardBlurPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_card_blur_player_playback_controls) {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    private var _binding: FragmentCardBlurPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardBlurPlayerPlaybackControlsBinding.bind(view)
        setUpMusicControllers()
    }

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = Color.WHITE
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(Color.WHITE, 0.3f)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updateProgressTextColor()

        volumeFragment?.tintWhiteColor()
    }

    private fun setUpPlayPauseFab() {
        binding.mediaButton.playPauseButton.apply {
            TintHelper.setTintAuto(this, Color.WHITE, true)
            TintHelper.setTintAuto(this, Color.BLACK, false)
            setOnClickListener(PlayPauseButtonOnClickHandler())
        }
    }

    private fun updatePlayPauseDrawableState() {
        when {
            MusicPlayerRemote.isPlaying -> binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_pause)
            else -> binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun updateProgressTextColor() {
        val color = Color.WHITE
        binding.songTotalTime.setTextColor(color)
        binding.songCurrentProgress.setTextColor(color)
        binding.songInfo.setTextColor(color)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    private fun updateSong() {
        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(MusicPlayerRemote.currentSong)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        binding.mediaButton.nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        binding.mediaButton.previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor() {
        binding.mediaButton.nextButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.mediaButton.previousButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun setUpShuffleButton() {
        binding.mediaButton.shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> binding.mediaButton.shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> binding.mediaButton.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        binding.mediaButton.repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    override fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.mediaButton.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.mediaButton.repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.mediaButton.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.mediaButton.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.mediaButton.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.mediaButton.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    public override fun show() {
        binding.mediaButton.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.mediaButton.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun setUpProgressSlider() {
        binding.progressSlider.applyColor(Color.WHITE)
        binding.progressSlider.setOnSeekBarChangeListener(object : SimpleOnSeekbarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress)
                    onUpdateProgressViews(
                        MusicPlayerRemote.songProgressMillis,
                        MusicPlayerRemote.songDurationMillis
                    )
                }
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressSlider.max = total

        val animator = ObjectAnimator.ofInt(binding.progressSlider, "progress", progress)
        animator.duration = SLIDER_ANIMATION_TIME
        animator.interpolator = LinearInterpolator()
        animator.start()

        binding.songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        binding.songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
