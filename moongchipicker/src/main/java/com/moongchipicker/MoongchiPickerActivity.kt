package com.moongchipicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.PERMISSION_MEDIA_ACCESS
import com.moongchipicker.util.registerPermissionRequestLauncher
import kotlinx.parcelize.Parcelize

sealed interface MoongchiPickerResult : Parcelable {
    @Parcelize
    class Success(val uriList: List<Uri>) : MoongchiPickerResult

    @Parcelize
    class Failure(val errorMsg: String?) : MoongchiPickerResult
}

class MoongchiPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moongchi_picker)

        val dialogInfo =
            intent.getParcelableExtra<MoongchiPickerDialog.DialogInfo>(MoongchiPickerDialog.DIALOG_INFO_KEY)
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
            onFailed = this::setFailureResult
        )

        val getMultipleContentLauncher = registerGetMultipleContentLauncher(
            onSuccess = this::setSuccessResult,
            onFailed = this::setFailureResult
        )

        val takePictureLauncher = registerTakePictureLauncher(
            onSuccess = {
                setSuccessResult(listOf(it))
            },
            onFailed = this::setFailureResult
        )

        val takeVideoLauncher = registerTakeVideoLauncher(
            onSuccess = {
                setSuccessResult(listOf(it))
            },
            onFailed = this::setFailureResult
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

        fun createIntent(context: Context, dialogInfo: MoongchiPickerDialog.DialogInfo) = Intent(
            context,
            MoongchiPickerActivity::class.java
        ).apply { putExtra(MoongchiPickerDialog.DIALOG_INFO_KEY, dialogInfo) }
    }
}