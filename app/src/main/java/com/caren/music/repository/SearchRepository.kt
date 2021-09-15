/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.caren.music.repository

import android.content.Context
import com.caren.music.R
import com.caren.music.model.Album
import com.caren.music.model.Artist
import com.caren.music.model.Genre
import com.caren.music.model.Song

class RealSearchRepository(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val roomRepository: RoomRepository,
    private val genreRepository: GenreRepository,
) {
    fun searchAll(context: Context, query: String?, filters: List<Boolean>): MutableList<Any> {
        val results = mutableListOf<Any>()
        query?.let { searchString ->
            val isAll = !filters.contains(true)
            val songs: List<Song> = if (filters[0] || isAll) {
                songRepository.songs(searchString)
            } else {
                emptyList()
            }

            if (songs.isNotEmpty()) {
                results.add(context.resources.getString(R.string.songs))
                results.addAll(songs)
            }
            val artists: List<Artist> = if (filters[1] || isAll) {
                artistRepository.artists(searchString)
            } else {
                emptyList()
            }
            if (artists.isNotEmpty()) {
                results.add(context.resources.getString(R.string.artists))
                results.addAll(artists)
            }
            val albums: List<Album> = if (filters[2] || isAll) {
                albumRepository.albums(searchString)
            } else {
                emptyList()
            }
            if (albums.isNotEmpty()) {
                results.add(context.resources.getString(R.string.albums))
                results.addAll(albums)
            }
            val albumArtists: List<Artist> = if (filters[3] || isAll) {
                artistRepository.albumArtists(searchString)
            } else {
                emptyList()
            }
            if (albumArtists.isNotEmpty()) {
                results.add(context.resources.getString(R.string.album_artist))
                results.addAll(albumArtists)
            }
            val genres: List<Genre> = if (filters[4] || isAll) {
                genreRepository.genres().filter { genre ->
                    genre.name.lowercase()
                        .contains(searchString.lowercase())
                }
            } else {
                emptyList()
            }
            if (genres.isNotEmpty()) {
                results.add(context.resources.getString(R.string.genres))
                results.addAll(genres)
            }
            /* val playlist = roomRepository.playlists().filter { playlist ->
                 playlist.playlistName.toLowerCase(Locale.getDefault())
                     .contains(searchString.toLowerCase(Locale.getDefault()))
             }
             if (playlist.isNotEmpty()) {
                 results.add(context.getString(R.string.playlists))
                 results.addAll(playlist)
             }*/
        }
        return results
    }
}
