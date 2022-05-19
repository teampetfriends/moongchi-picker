package com.moongchipicker.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import com.moongchipicker.PetMediaType
import com.moongchipicker.util.BitmapHelper

internal data class Media(
    val uri: Uri,
    val mediaType : PetMediaType
) {
    @Throws
    fun getBitmap(context: Context): Bitmap? {
        return when(mediaType){
            PetMediaType.IMAGE-> BitmapHelper.getBitmapFromUri(uri, context.contentResolver, BitmapHelper.BitmapSize.SMALL)
            PetMediaType.VIDEO -> BitmapHelper.getBitmapFromVideo(context, uri)
        }
    }
    companion object {
        fun empty() = Media(Uri.EMPTY, PetMediaType.IMAGE)
        val diffUtil = object : DiffUtil.ItemCallback<Media>(){
            override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
                return oldItem.uri == newItem.uri
            }

        }
    }
}
