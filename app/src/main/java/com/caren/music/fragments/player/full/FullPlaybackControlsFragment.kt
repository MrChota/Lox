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
package com.caren.music.fragments.player.full

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import code.name.monkey.appthemehelper.util.ColorUtil
import com.caren.music.R
import com.caren.music.databinding.FragmentFullPlayerControlsBinding
import com.caren.music.db.PlaylistEntity
import com.caren.music.db.SongEntity
import com.caren.music.db.toSongEntity
import com.caren.music.extensions.applyColor
import com.caren.music.extensions.hide
import com.caren.music.extensions.show
import com.caren.music.fragments.LibraryViewModel
import com.caren.music.fragments.ReloadType
import com.caren.music.fragments.base.AbsPlayerControlsFragment
import com.caren.music.fragments.base.goToAlbum
import com.caren.music.fragments.base.goToArtist
import com.caren.music.helper.MusicPlayerRemote
import com.caren.music.helper.MusicProgressViewUpdateHelper
import com.caren.music.helper.PlayPauseButtonOnClickHandler
import com.caren.music.misc.SimpleOnSeekbarChangeListener
import com.caren.music.model.Song
import com.caren.music.service.MusicService
import com.caren.music.util.MusicUtil
import com.caren.music.util.PreferenceUtil
import com.caren.music.util.RetroUtil
import com.caren.music.util.color.MediaNotificationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Created by hemanths on 20/09/17.
 */

class FullPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_full_player_controls),
    PopupMenu.OnMenuItemClickListener {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    private val libraryViewModel: LibraryViewModel by sharedViewModel()
    private var _binding: FragmentFullPlayerControlsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFullPlayerControlsBinding.bind(view)

        setUpMusicControllers()
        binding.songTotalTime.setTextColor(Color.WHITE)
        binding.songCurrentProgress.setTextColor(Color.WHITE)
        binding.title.isSelected = true
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = color.primaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(color.primaryTextColor, 0.3f)

        val tintList = ColorStateList.valueOf(color.primaryTextColor)
        binding.playerMenu.imageTintList = tintList
        binding.songFavourite.imageTintList = tintList
        volumeFragment?.setTintableColor(color.primaryTextColor)
        binding.progressSlider.applyColor(color.primaryTextColor)
        binding.title.setTextColor(color.primaryTextColor)
        binding.text.setTextColor(color.secondaryTextColor)
        binding.songInfo.setTextColor(color.secondaryTextColor)
        binding.songCurrentProgress.setTextColor(color.secondaryTextColor)
        binding.songTotalTime.setTextColor(color.secondaryTextColor)

        binding.playPauseButton.backgroundTintList = tintList
        binding.playPauseButton.imageTintList = ColorStateList.valueOf(color.backgroundColor)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName
        updateIsFavorite()
        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
        binding.playPauseButton.post {
            binding.playPauseButton.pivotX = (binding.playPauseButton.width / 2).toFloat()
            binding.playPauseButton.pivotY = (binding.playPauseButton.height / 2).toFloat()
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
        setupFavourite()
        setupMenu()
    }

    private fun setupMenu() {
        binding.playerMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.menu_player)
            popupMenu.menu.findItem(R.id.action_toggle_favorite).isVisible = false
            popupMenu.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return (parentFragment as FullPlayerFragment).onMenuItemClick(item!!)
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        binding.nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        binding.previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor() {
        binding.nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        binding.previousButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    override fun setUpProgressSlider() {
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

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun setUpShuffleButton() {
        binding.shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> binding.shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> binding.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        binding.repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    override fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    override fun onFavoriteStateChanged() {
        updateIsFavorite(animate = true)
    }

    fun updateIsFavorite(animate: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist: PlaylistEntity? = libraryViewModel.favoritePlaylist()
            if (playlist != null) {
                val song: SongEntity =
                    MusicPlayerRemote.currentSong.toSongEntity(playlist.playListId)
                val isFavorite: Boolean = libraryViewModel.isFavoriteSong(song).isNotEmpty()
                withContext(Dispatchers.Main) {
                    val icon = if (animate) {
                        if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
                    } else {
                        if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                    }
                    val drawable: Drawable? = RetroUtil.getTintedVectorDrawable(
                        requireContext(),
                        icon,
                        Color.WHITE
                    )
                    binding.songFavourite.apply {
                        setImageDrawable(drawable)
                        getDrawable().also {
                            if (it is AnimatedVectorDrawable) {
                                it.start()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun toggleFavorite(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist: PlaylistEntity? = libraryViewModel.favoritePlaylist()
            if (playlist != null) {
                val songEntity = song.toSongEntity(playlist.playListId)
                val isFavorite = libraryViewModel.isFavoriteSong(songEntity).isNotEmpty()
                if (isFavorite) {
                    libraryViewModel.removeSongFromPlaylist(songEntity)
                } else {
                    libraryViewModel.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
                }
            }
            libraryViewModel.forceReload(ReloadType.Playlists)
            requireContext().sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
