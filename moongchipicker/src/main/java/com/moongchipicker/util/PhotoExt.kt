package com.moongchipicker.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min


/**
 * this method should called on [AppCompatActivity.onCreate]
 * because this method create file from context and use [AppCompatActivity.registerForActivityResult]
 */
internal fun ComponentActivity.registerTakePictureLauncher(
    onSuccess: (fileUri: Uri) -> Unit,
    onFailed: (Throwable) -> Unit
): StatefulActivityResultLauncher<Uri> {
    val contentUri = getContentUriFromFile(createImageFilePrivate())

    return registerForActivityResult(ActivityResultContracts.TakePicture()) { isPictureSaved ->
        if (isPictureSaved) {
            onSuccess(contentUri)
        } else {
            onFailed(TakePictureFailedException())
        }
    }.toStatefulActivityResultLauncher(contentUri)
}


internal fun ComponentActivity.registerTakeVideoLauncher(
    onSuccess: (fileUri: Uri) -> Unit,
    onFailed: (Throwable) -> Unit
): StatefulActivityResultLauncher<Uri> {
    val contentUri = getContentUriFromFile(createVideoFilePrivate())

    return registerForActivityResult(TakeVideoContract()) { isSucceed ->
        if (isSucceed) {
            onSuccess(contentUri)
        } else {
            onFailed(TakeVideoFailedException())
        }
    }.toStatefulActivityResultLauncher(contentUri)
}

internal fun Context.getContentUriFromFile(file: File): Uri =
    FileProvider.getUriForFile(this, applicationContext.packageName + ".com.moongchipicker.fileprovider", file)


internal fun Context.createImageFilePrivate(
    prefix: String = ""
): File {
    val imageFileFromExternalStorage = createImageFileToPrivateExternalStorage(prefix)
    return if (isExternalStorageWritable() && imageFileFromExternalStorage != null) {
        imageFileFromExternalStorage
    } else {
        createImageFileToInternalStorage(prefix)
    }
}

internal fun Context.createVideoFilePrivate(
    prefix: String = ""
): File {
    val videoFileFromExternalStorage = createVideoFileToPrivateExternalStorage(prefix)
    return if (isExternalStorageWritable() && videoFileFromExternalStorage != null) {
        videoFileFromExternalStorage
    } else {
        createVideoFileToInternalStorage(prefix)
    }
}

/**
 * @return uri is sorted ascending order base on modified date
 */
internal suspend fun Context.loadVideosFromInternalStorage(maxFileCount: Int): List<Uri> {
    return loadFilesFromInternalStorage(".mp4", maxFileCount)
}

/**
 * @return uri is sorted ascending order base on modified date
 */
internal suspend fun Context.loadImagesFromInternalStorage(maxFileCount: Int): List<Uri> {
    return loadFilesFromInternalStorage(".jpg", maxFileCount)
}


/**
 * @param format : ex ) ".jpg", ".png"
 * @return uri is sorted ascending order base on modified date
 */
private suspend fun Context.loadFilesFromInternalStorage(
    format: String,
    maxFileCount: Int
): List<Uri> {
    return withContext(Dispatchers.IO) {
        val files =
            filesDir.listFiles()?.toList()?.apply { subList(0, min(size, maxFileCount)) }.toSafe()
        files.filter { it.canRead() && it.isFile && it.name.endsWith(format) }
            .sortedBy {
                it.lastModified()
            }.map { file ->
                Uri.fromFile(file)
            }
    }
}

/**
 * @return uri is sorted ascending order base on modified date
 */
internal suspend fun Context.loadVideosFromExternalStorage(maxFileCount: Int): List<Uri> {
    return withContext(Dispatchers.IO) {
        val collection = sdkAndUp(Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID
        )

        contentResolver.query(
            collection,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_MODIFIED
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

            val uris = mutableListOf<Uri>()
            while (cursor.moveToNext() && uris.size < maxFileCount) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                uris.add(contentUri)
            }
            uris
        } ?: listOf<Uri>()
    }
}

/**
 * @return uri is sorted ascending order base on modified date
 */
internal suspend fun Context.loadImagesFromExternalStorage(maxFileCount: Int): List<Uri> {
    return withContext(Dispatchers.IO) {
        val collection = sdkAndUp(Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID
        )

        contentResolver.query(
            collection,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_MODIFIED
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            val uris = mutableListOf<Uri>()
            while (cursor.moveToNext() && uris.size < maxFileCount) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                uris.add(contentUri)
            }
            uris
        } ?: listOf<Uri>()
    }
}


private fun Context.createImageFileToInternalStorage(
    prefix: String = ""
): File {
    val storageDir: File = filesDir

    //createTempFile 은 알아서 겹치지않는 이름을 가진 파일을 생성한다.
    return File.createTempFile(
        "JPEG_${prefix}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}


private fun Context.createImageFileToPrivateExternalStorage(
    prefix: String = ""
): File? {
    val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null

    return File.createTempFile(
        "JPEG_${prefix}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}

private fun Context.createVideoFileToInternalStorage(
    prefix: String = ""
): File {
    val storageDir: File = filesDir

    //createTempFile 은 알아서 겹치지않는 이름을 가진 파일을 생성한다.
    return File.createTempFile(
        "MPEG_${prefix}_", /* prefix */
        ".mp4", /* suffix */
        storageDir /* directory */
    )
}

private fun Context.createVideoFileToPrivateExternalStorage(
    prefix: String = ""
): File? {
    val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: return null

    return File.createTempFile(
        "MPEG_${prefix}_", /* prefix */
        ".mp4", /* suffix */
        storageDir /* directory */
    )
}


private fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

private fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

