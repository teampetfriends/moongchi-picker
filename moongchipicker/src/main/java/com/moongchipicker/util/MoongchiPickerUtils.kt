package com.moongchipicker.util

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.moongchipicker.CustomTakePicture
import com.moongchipicker.CustomTakeVideo
import com.moongchipicker.MoongchiPickerDialog
import com.moongchipicker.MoongchiPickerDialog.Companion.REQUEST_MOONGCHI_PICKER_DIALOG
import com.moongchipicker.data.MediaType
import com.moongchipicker.data.MoongchiPickerParam

internal fun AppCompatActivity.showMoongchiPicker(moongchiPickerParam: MoongchiPickerParam) {
    MoongchiPickerDialog().apply {
        arguments = bundleOf(
            MoongchiPickerDialog.DIALOG_INFO_KEY to MoongchiPickerParam(
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
) =
    registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { contentUri: List<Uri>? ->
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
