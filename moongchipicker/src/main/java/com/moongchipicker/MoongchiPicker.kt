package com.moongchipicker

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.moongchipicker.util.GetPictureFailedException
import java.io.Serializable

enum class MediaType(val mimeType : String) : Serializable {
    IMAGE("image/*"), VIDEO("video/*")
}

internal const val REQUEST_MOONGCHI_PICKER = 456
internal const val REQUEST_MOONGCHI_PICKER_DIALOG = "REQUEST_MOONGCHI_PICKER_DIALOG"

internal fun AppCompatActivity.showMoongchiPicker(dialogInfo: MoongchiPickerDialog.DialogInfo) {
    MoongchiPickerDialog().apply {
        arguments = bundleOf(
            MoongchiPickerDialog.DIALOG_INFO_KEY to MoongchiPickerDialog.DialogInfo(
                MediaType.IMAGE,
                5
            )
        )
    }.show(supportFragmentManager, null)
}

internal fun AppCompatActivity.onMoongchiPickerResult(callback: (MoongchiPickerDialog.DialogResult) -> Unit) {
    supportFragmentManager.setFragmentResultListener(
        REQUEST_MOONGCHI_PICKER_DIALOG,
        this
    ) { _, result -> MoongchiPickerDialog.parseDialogResult(result)?.let(callback) }
}

internal fun AppCompatActivity.registerGetContentLauncher(
    onSuccess: (Uri) -> Unit,
    onFailed: (Throwable) -> Unit
) = registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri: Uri? ->
    if (contentUri == null) {
        onFailed(GetPictureFailedException())
    } else {
        onSuccess(contentUri)
    }
}

internal fun AppCompatActivity.registerGetMultipleContentLauncher(
    onSuccess: (List<Uri>) -> Unit,
    onFailed: (Throwable) -> Unit
) = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { contentUri: List<Uri>? ->
    if (contentUri == null) {
        onFailed(GetPictureFailedException())
    } else {
        onSuccess(contentUri)
    }
}

internal fun ComponentActivity.registerTakePictureLauncher(
    onSuccess: (fileUri: Uri) -> Unit,
    onFailed: (Throwable) -> Unit
): ActivityResultLauncher<Unit> {
    return registerForActivityResult(CustomTakePicture()) { uri ->
        if (uri == null) {
            onFailed(NullPointerException("TakePicture result in null"))
        } else {
            onSuccess(uri)
        }
    }
}

internal fun ComponentActivity.registerTakeVideoLauncher(
    onSuccess: (fileUri: Uri) -> Unit,
    onFailed: (Throwable) -> Unit
): ActivityResultLauncher<Unit> {
    return registerForActivityResult(CustomTakeVideo()) { uri ->
        if (uri == null) {
            onFailed(NullPointerException("TakeVideo result in null"))
        } else {
            onSuccess(uri)
        }
    }
}
