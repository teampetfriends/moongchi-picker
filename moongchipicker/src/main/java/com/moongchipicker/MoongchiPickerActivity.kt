package com.moongchipicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.registerPermissionRequestLauncher
import java.io.Serializable

sealed interface MoongchiPickerResult : Serializable {
    class Success(uriList: List<Uri>) : MoongchiPickerResult
    class Failure(throwable: Throwable) : MoongchiPickerResult
}

class MoongchiPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moongchi_picker)

        val dialogInfo =
            intent.getSerializableExtra(MoongchiPickerDialog.DIALOG_INFO_KEY) as? MoongchiPickerDialog.DialogInfo
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
                is MoongchiPickerDialog.DialogResult.GetContentFromGallery -> {
                    if (dialogInfo.maxSelectableMediaCount > 1) {
                        getMultipleContentLauncher.launch(dialogInfo.mediaType.mimeType)
                    } else {
                        getContentLauncher.launch(dialogInfo.mediaType.mimeType)
                    }
                }
                is MoongchiPickerDialog.DialogResult.TakePicture -> {
                    takePictureLauncher.launch(Unit)
                }
                is MoongchiPickerDialog.DialogResult.TakeVideo -> {
                    takeVideoLauncher.launch(Unit)
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
    }

    private fun setFailureResult(throwable: Throwable) {
        setResult(
            RESULT_CANCELED,
            Intent().apply {
                putExtra(
                    KEY_MOONGCHIPICKER_RESULT,
                    MoongchiPickerResult.Failure(throwable)
                )
            })
    }

    companion object {
        const val KEY_MOONGCHIPICKER_RESULT = "KEY_MOONGCHIPICKER_RESULT"
    }
}