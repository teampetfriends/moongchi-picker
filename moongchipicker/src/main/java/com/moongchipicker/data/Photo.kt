package com.moongchipicker.data

import android.graphics.Bitmap
import android.net.Uri

internal data class Photo(
    val uri: Uri,
    val bitmap: Bitmap
) {
    companion object {
        fun empty() = Photo(Uri.EMPTY, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    }
}
