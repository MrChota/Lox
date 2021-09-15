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
package com.caren.music.fragments.artists

import androidx.lifecycle.*
import com.caren.music.interfaces.IMusicServiceEventListener
import com.caren.music.model.Artist
import com.caren.music.network.Result
import com.caren.music.network.model.LastFmArtist
import com.caren.music.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AlbumArtistDetailsViewModel(
    private val realRepository: RealRepository,
    private val artistName: String
) : ViewModel(), IMusicServiceEventListener {
    private val artistDetails = MutableLiveData<Artist>()

    init {
        fetchAlbumArtist()
    }

    private fun fetchAlbumArtist() {
        viewModelScope.launch(IO) {
            artistDetails.postValue(realRepository.albumArtistByName(artistName))
        }
    }

    fun getArtist(): LiveData<Artist> = artistDetails

    fun getArtistInfo(
        name: String,
        lang: String?,
        cache: String?
    ): LiveData<Result<LastFmArtist>> = liveData(IO) {
        emit(Result.Loading)
        val info = realRepository.artistInfo(name, lang, cache)
        emit(info)
    }

    override fun onMediaStoreChanged() {
       fetchAlbumArtist()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onFavoriteStateChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}
