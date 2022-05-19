package com.moongchipicker.util

import android.content.Context
import android.net.Uri
import com.moongchipicker.PetMediaType

internal class MediaLoader(
    private val context : Context
) {
    suspend fun loadMediaFromExternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> context.loadImagesFromPublicExternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> context.loadVideosFromPublicExternalStorage(maxMediaSize)
        }
    }


    suspend fun loadMediaFromInternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> context.loadImagesFromInternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> context.loadVideosFromInternalStorage(maxMediaSize)
        }
    }

}