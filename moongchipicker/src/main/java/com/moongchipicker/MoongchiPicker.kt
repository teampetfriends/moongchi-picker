package com.moongchipicker

import android.Manifest
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.registerPermissionRequestLauncher
import com.moongchipicker.util.toDebugString

interface MoongchiPickerListener {
    fun onSubmitMedia(contentUris: List<Uri>)
    fun onFailed(t: Throwable)

    /**
     * called when user select media over limit from gallery
     * @see [MoongchiPicker]
     */
    fun onSelectedMediaCountOverLimit(limit: Int) {

    }
}


/**
 * this class must created on [AppCompatActivity.onCreate]
 * @param activity activity to register [ActivityResultContracts] that MoonchiPicker need
 * @param allowPermissionRequest allow to request permissions for reading and writing media
 * @param allowMultiple allow to pick multiple media from gallery
 * @param maxMediaCountBuilder builder for build max selection count for fetching media from gallery
 */
class MoongchiPicker(
    activity: AppCompatActivity,
    allowPermissionRequest: Boolean = false,
    mediaType: PetMediaType,
    allowMultiple: Boolean = false,
    maxMediaCountBuilder: () -> Int = { 1 },
    moongchiPickerListener: MoongchiPickerListener
) {

    private val request: () -> Unit

    init {

        val moongchiPickerDialogListener = MoongchiPickerDelegate(activity).registerMediaPickRequest(
            mediaType,
            allowMultiple,
            maxMediaCountBuilder,
            moongchiPickerListener
        )

        fun createMoongchiPickerDialog(): MoongchiPickerDialog {
            return MoongchiPickerDialog.newInstance(
                mediaType,
                moongchiPickerDialogListener,
                maxMediaCountBuilder()
            )
        }

        fun createShowMoochiPickerWithPermissionDelegate(): () -> Unit {
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

        request = if (allowPermissionRequest) {
            createShowMoochiPickerWithPermissionDelegate()
        } else {
            { createMoongchiPickerDialog().show(activity.supportFragmentManager, null) }
        }
    }


    fun show() {
        request()
    }


}