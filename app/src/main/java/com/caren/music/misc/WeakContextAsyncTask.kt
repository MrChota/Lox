/*com.caren.music
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

package code.name.monkey.retromusic.misc

import android.content.Context
import android.os.AsyncTask

import java.lang.ref.WeakReference

abstract class WeakContextAsyncTask<Params, Progress, Result>(context: Context) :
    AsyncTask<Params, Progress, Result>() {
    private val contextWeakReference: WeakReference<Context> = WeakReference(context)

    protected val context: Context?
        get() = contextWeakReference.get()

}