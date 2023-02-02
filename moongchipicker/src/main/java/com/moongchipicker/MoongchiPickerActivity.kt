package com.moongchipicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.data.MediaType
import com.moongchipicker.data.MoongchiPickerParam
import com.moongchipicker.data.MoongchiPickerResult
import com.moongchipicker.util.*

internal class MoongchiPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialogInfo =
            intent.getParcelableExtra<MoongchiPickerParam>(MoongchiPickerDialog.DIALOG_INFO_KEY)
                ?: return

        val permissionLauncher = registerPermissionRequestLauncher(
            onPermissionGranted = { showMoongchiPicker(dialogInfo) },
            withDeniedPermissions = {
                Toast.makeText(
                    this,
                    "To enable this function, please allow requested permissions.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        permissionLauncher.launch(PERMISSION_MEDIA_ACCESS)

        val getContentLauncher = registerGetContentLauncher(
            onSuccess = {
                setSuccessResult(listOf(it))
            },
            onFailed = ::setFailureResult
        )

        val getMultipleContentLauncher = registerGetMultipleContentLauncher(
            onSuccess = ::setSuccessResult,
            onFailed = ::setFailureResult
        )

        val takePictureLauncher = registerTakePictureLauncher(
            onSuccess = {
                setSuccessResult(listOf(it))
            },
            onFailed = ::setFailureResult
        )

        val takeVideoLauncher = registerTakeVideoLauncher(
            onSuccess = {
                setSuccessResult(listOf(it))
            },
            onFailed = ::setFailureResult
        )


        onMoongchiPickerResult {
            when (it) {
                is MoongchiPickerDialog.DialogResult.Success -> {
                    setSuccessResult(it.mediaUriList)
                }
                is MoongchiPickerDialog.DialogResult.OpenGallery -> {
                    if (dialogInfo.maxSelectableMediaCount > 1) {
                        getMultipleContentLauncher.launch(dialogInfo.mediaType.mimeType)
                    } else {
                        getContentLauncher.launch(dialogInfo.mediaType.mimeType)
                    }
                }
                is MoongchiPickerDialog.DialogResult.OpenCamera -> {
                    when (dialogInfo.mediaType) {
                        MediaType.IMAGE -> takePictureLauncher.launch(Unit)
                        MediaType.VIDEO -> takeVideoLauncher.launch(Unit)
                    }
                }
                is MoongchiPickerDialog.DialogResult.Failure -> {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun setSuccessResult(uriList: List<Uri>) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(
                    KEY_MOONGCHIPICKER_RESULT,
                    MoongchiPickerResult.Success(uriList)
                )
            })
        finish()
    }

    private fun setFailureResult(throwable: Throwable) {
        setResult(
            RESULT_CANCELED,
            Intent().apply {
                putExtra(
                    KEY_MOONGCHIPICKER_RESULT,
                    MoongchiPickerResult.Failure(throwable.message)
                )
            })
        finish()
    }

    companion object {
        const val KEY_MOONGCHIPICKER_RESULT = "KEY_MOONGCHIPICKER_RESULT"

        internal fun createIntent(context: Context, dialogInfo: MoongchiPickerParam) =
            Intent(
                context,
                MoongchiPickerActivity::class.java
            ).apply { putExtra(MoongchiPickerDialog.DIALOG_INFO_KEY, dialogInfo) }
    }
}