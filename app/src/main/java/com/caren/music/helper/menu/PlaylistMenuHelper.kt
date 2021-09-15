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
package com.caren.music.helper.menu

import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import com.caren.music.R
import com.caren.music.db.PlaylistWithSongs
import com.caren.music.db.toSongs
import com.caren.music.dialogs.AddToPlaylistDialog
import com.caren.music.dialogs.DeletePlaylistDialog
import com.caren.music.dialogs.RenamePlaylistDialog
import com.caren.music.dialogs.SavePlaylistDialog
import com.caren.music.helper.MusicPlayerRemote
import com.caren.music.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get

object PlaylistMenuHelper : KoinComponent {

    fun handleMenuClick(
        activity: FragmentActivity,
        playlistWithSongs: PlaylistWithSongs,
        item: MenuItem
    ): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                MusicPlayerRemote.openQueue(playlistWithSongs.songs.toSongs(), 0, true)
                return true
            }
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(playlistWithSongs.songs.toSongs())
                return true
            }
            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, playlistWithSongs.songs.toSongs())
                            .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(playlistWithSongs.songs.toSongs())
                return true
            }
            R.id.action_rename_playlist -> {
                RenamePlaylistDialog.create(playlistWithSongs.playlistEntity)
                    .show(activity.supportFragmentManager, "RENAME_PLAYLIST")
                return true
            }
            R.id.action_delete_playlist -> {
                DeletePlaylistDialog.create(playlistWithSongs.playlistEntity)
                    .show(activity.supportFragmentManager, "DELETE_PLAYLIST")
                return true
            }
            R.id.action_save_playlist -> {
                SavePlaylistDialog.create(playlistWithSongs)
                    .show(activity.supportFragmentManager, "SavePlaylist")
                return true
            }
        }
        return false
    }
}
