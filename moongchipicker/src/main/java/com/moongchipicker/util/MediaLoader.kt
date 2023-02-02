package com.moongchipicker.util

import android.content.Context
import android.net.Uri
import com.moongchipicker.data.MediaType

internal class MediaLoader(
    private val context : Context
) {
    suspend fun loadMediaFromExternalStorage(
        mediaType: MediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            MediaType.IMAGE -> context.loadImagesFromPublicExternalStorage(maxMediaSize)
            MediaType.VIDEO -> context.loadVideosFromPublicExternalStorage(maxMediaSize)
        }
    }


    suspend fun loadMediaFromInternalStorage(
        mediaType: MediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            MediaType.IMAGE -> context.loadImagesFromInternalStorage(maxMediaSize)
            MediaType.VIDEO -> context.loadVideosFromInternalStorage(maxMediaSize)
        }
    }

}