package com.caren.music.model.smartplaylist

import com.caren.music.App
import com.caren.music.R
import com.caren.music.model.Song
import kotlinx.parcelize.Parcelize
import org.koin.core.KoinComponent

@Parcelize
class HistoryPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.history),
    iconRes = R.drawable.ic_history
), KoinComponent {

    override fun songs(): List<Song> {
        return topPlayedRepository.recentlyPlayedTracks()
    }
}