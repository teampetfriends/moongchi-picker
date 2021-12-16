package com.moongchipicker.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.LruCache
import androidx.exifinterface.media.ExifInterface
import java.lang.Exception


internal object BitmapHelper {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private var memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.byteCount / 1024
        }
    }

    enum class BitmapSize(val width: Int, val height: Int) {
        VERY_SMALL(64, 64), SMALL(128, 128), MIDDLE(512, 512), NONE(0, 0)
    }

    fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degree)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            ?: bitmap

    }

    fun resizeBitmap(bitmap: Bitmap, bitmapSize: BitmapSize): Bitmap? {
        val sampleSize =
            calculateInSampleSize(bitmap.height, bitmap.width, bitmapSize.height, bitmap.width)
        return Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / sampleSize,
            bitmap.height / sampleSize,
            true
        )
    }


    fun getBitmapFromVideo(context: Context, uri: Uri): Bitmap? {
        getBitmapFromCache(uri)?.let {
            return it
        }

        val retriever = MediaMetadataRetriever()
        val bitmap = retriever.runCatching {
            setDataSource(context, uri)
            getFrameAtTime(-1)
        }.onFailure {
            Logger.d("Failed To get video thumbnail : ${uri.path}")
        }.getOrNull()

        retriever.release()

        return ThumbnailUtils.extractThumbnail(
            bitmap,
            BitmapSize.SMALL.width,
            BitmapSize.SMALL.height,
            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        )?.also {
            putBitmapToCache(uri, it)
        }

    }

    /**
     * get bitmap from uri.
     * @param bitmapSize images from gallery or camera app has high resolution. so we can down-sample the image when get bitmap from uri.
     */
    fun getBitmapFromUri(
        uri: Uri,
        contentResolver: ContentResolver,
        bitmapSize: BitmapSize = BitmapSize.NONE
    ): Bitmap? {
        getBitmapFromCache(uri)?.let {
            return it
        }

        val option: BitmapFactory.Options = BitmapFactory.Options();

        if (bitmapSize != BitmapSize.NONE) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                option.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, option)

                option.inSampleSize = calculateInSampleSize(
                    option.outHeight,
                    option.outWidth,
                    bitmapSize.height,
                    bitmapSize.width
                )
            }
        }

        val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
            option.inJustDecodeBounds = false
            BitmapFactory.decodeStream(inputStream, null, option)?.let {
                rotateBitmapIfNeeded(contentResolver, it, uri)
            }
        }

        return bitmap?.also {
            putBitmapToCache(uri, it)
        }
    }


    private fun calculateInSampleSize(
        rawHeight: Int,
        rawWidth: Int,
        reqHeight: Int,
        reqWidth: Int
    ): Int {

        var inSampleSize = 1

        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            val halfHeight: Int = rawHeight / 2
            val halfWidth: Int = rawWidth / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


    private fun rotateBitmapIfNeeded(
        contentResolver: ContentResolver,
        bitmap: Bitmap,
        uri: Uri
    ): Bitmap {
        val attr = contentResolver.openInputStream(uri)?.use { inputStream ->
            kotlin.runCatching {
                ExifInterface(inputStream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrNull() ?: return bitmap
        }

        return when (attr) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270F)
            else -> bitmap
        }
    }


    private fun putBitmapToCache(uri: Uri, bitmap: Bitmap) {
        memoryCache.put(uri.path, bitmap)
    }

    private fun getBitmapFromCache(uri: Uri): Bitmap? {
        return memoryCache.get(uri.path)
    }


}