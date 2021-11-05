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


object BitmapHelper {
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
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) ?: bitmap

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
     * URI부터 비트맵을 뽑아낸다.
     * 갤러리에서 고른 이미지나 카메라로 찍은 이미지들은
     * 그대로 ImageView를 통해 보여주기에는 해상도가 너무 높은경우가 많으므로
     * 적절히 해상도를 낮춰서 뽑는다.
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

        //다운샘플링시에 1/4 처럼 비율로 줄이니까 bitmapSize 가 뭐든 원본 비율은 유지됨
        if (bitmapSize != BitmapSize.NONE) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                //먼저 비트맵을 조사한다.
                option.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, option)

                //비트맵의 크기를 토대로 샘플링할 사이즈를 구한다.
                option.inSampleSize = calculateInSampleSize(
                    option.outHeight,
                    option.outWidth,
                    bitmapSize.height,
                    bitmapSize.width
                )
            }
        }

        // BitmapFactory.decodeStream 하면 inputStream 이 변형되므로, 옵션을 얻고난다음에는
        // 다시 inputStream 을 만들어야한다.
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

    /**
     * 타겟 너비와 높이를 기준으로 2의 거듭제곱 형태로 샘플 크기 값을 계산하는 메서드.
     * According to this inSampleSize just reduce the pixel count. It cant be used on whole numbers,
     * you cant map 2 pixel to 1.5 pixels, thats way it`s power of 2.
     * https://stackoverrun.com/ko/q/10820265
     */
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


    private fun rotateBitmapIfNeeded(contentResolver: ContentResolver, bitmap: Bitmap, uri: Uri): Bitmap {
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