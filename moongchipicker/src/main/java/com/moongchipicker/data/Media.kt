package com.moongchipicker.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.moongchipicker.PetMediaType
import com.moongchipicker.util.BitmapHelper

internal data class Media(
    val uri: Uri,
    val mediaType : PetMediaType
) {
    fun getBitmap(context: Context): Bitmap? {
        return when(mediaType){
            PetMediaType.IMAGE-> BitmapHelper.getBitmapFromUri(uri, context.contentResolver, BitmapHelper.BitmapSize.SMALL)
            PetMediaType.VIDEO -> BitmapHelper.getBitmapFromVideo(context, uri)
        }
    }
    companion object {
        fun empty() = Media(Uri.EMPTY, PetMediaType.IMAGE)
    }
}
