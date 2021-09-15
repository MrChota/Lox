package com.caren.music.interfaces

import com.caren.music.model.Album
import com.caren.music.model.Artist
import com.caren.music.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}