package com.caren.music.glide.playlistPreview

import com.caren.music.db.PlaylistEntity
import com.caren.music.db.PlaylistWithSongs
import com.caren.music.db.toSongs
import com.caren.music.model.Song

class PlaylistPreview(val playlistWithSongs: PlaylistWithSongs) {

    val playlistEntity: PlaylistEntity get() = playlistWithSongs.playlistEntity
    val songs: List<Song> get() = playlistWithSongs.songs.toSongs()

    override fun equals(other: Any?): Boolean {
        if (other is PlaylistPreview) {
            if (other.playlistEntity.playListId != playlistEntity.playListId) {
                return false
            }
            if (other.songs.size != songs.size) {
                return false
            }
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = playlistEntity.playListId.hashCode()
        result = 31 * result + playlistWithSongs.songs.size
        return result
    }
}