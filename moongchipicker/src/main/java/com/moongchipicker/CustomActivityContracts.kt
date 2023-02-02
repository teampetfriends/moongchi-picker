package com.moongchipicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toFile
import com.moongchipicker.util.createImageFilePrivate
import com.moongchipicker.util.createVideoFilePrivate
import com.moongchipicker.util.getContentUriFromFile

internal class CustomTakePicture : ActivityResultContract<Unit, Uri?>() {
    private var uri: Uri? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        this.uri = kotlin.runCatching {
            val file = context.createImageFilePrivate()
            context.getContentUriFromFile(file)
        }.getOrNull()
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_OUTPUT, this.uri)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) {
            this.uri
        } else {
            uri?.toFile()?.delete()
            null
        }
    }
}

internal class CustomTakeVideo : ActivityResultContract<Unit, Uri?>() {
    private var uri: Uri? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        this.uri = kotlin.runCatching {
            val file = context.createVideoFilePrivate()
            context.getContentUriFromFile(file)
        }.getOrNull()
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_OUTPUT, this.uri)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) {
            this.uri
        } else {
            uri?.toFile()?.delete()
            null
        }
    }
}