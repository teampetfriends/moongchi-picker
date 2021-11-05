package com.moongchipicker

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.registerPermissionRequestLauncher
import com.moongchipicker.util.toDebugString

interface MoongchiPickerListener {
    fun onSubmitMedia(contentUris: List<Uri>)
    fun onFailed(t: Throwable)

    /**
     * 사용자가 갤러리를 통해 선택한 미디어의 갯수가
     * [MoongchiPicker] 에서 지정한 갯수보다 클때 불린다.
     */
    fun onSelectedMediaCountOverLimit(limit: Int) {

    }
}


/**
 * this class must created on [AppCompatActivity.onCreate]
 */
class MoongchiPicker(
    activity: AppCompatActivity,
    isAutoPermission: Boolean = false,
    mediaType: PetMediaType,
    isAllowMultiple: Boolean = false,
    maxImageCountBuilder: () -> Int = { 1 },
    moongchiPickerListener: MoongchiPickerListener
) {

    private val request: () -> Unit

    init {

        val moongchiPickerDialogListener = MoongchiPickerDelegate(activity).registerMediaPickRequest(
            mediaType,
            isAllowMultiple,
            maxImageCountBuilder,
            moongchiPickerListener
        )

        fun createMoongchiPickerDialog(): MoongchiPickerDialog {
            return MoongchiPickerDialog.newInstance(
                mediaType,
                moongchiPickerDialogListener,
                maxImageCountBuilder()
            )
        }

        fun getMoongchiPickerWithAutoPermissionLauncher(): () -> Unit {
            val mediaPermissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val launcher = activity.registerPermissionRequestLauncher(
                permissionsToRequest = mediaPermissions,
                onPermissionGranted = {
                    createMoongchiPickerDialog().show(activity.supportFragmentManager, null)
                },
                withDeniedPermissions = {
                    moongchiPickerListener.onFailed(Throwable("permission denied : ${it.toDebugString()}"))
                }
                )
            return { launcher.launch() }
        }

        request = if (isAutoPermission) {
            getMoongchiPickerWithAutoPermissionLauncher()
        } else {
            { createMoongchiPickerDialog().show(activity.supportFragmentManager, null) }
        }
    }


    fun show() {
        request()
    }


}