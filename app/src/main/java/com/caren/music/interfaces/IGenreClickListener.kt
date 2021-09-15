package com.caren.music.interfaces

import android.view.View
import com.caren.music.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}