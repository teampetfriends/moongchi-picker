package com.moongchipicker

import android.Manifest
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
 * @param maxSelectableMediaCountBuilder builder for build max selection count for fetching media from gallery
 * @param maxVisibleMediaCount MoongchiPickerDialog shows this amount of media items
 */
class MoongchiPicker(
    activity: ComponentActivity,
    fragmentManager: FragmentManager,
    allowPermissionRequest: Boolean = false,
    mediaType: PetMediaType,
    allowMultiple: Boolean = false,
    maxSelectableMediaCountBuilder: () -> Int = { 1 },
    maxVisibleMediaCount: Int = MoongchiPickerDialog.MAX_VISIBLE_MEDIA_COUNT,
    moongchiPickerListener: MoongchiPickerListener
) {

    private val request: () -> Unit

    init {

        val moongchiPickerDialogListener = MoongchiPickerDelegate(activity).registerMediaPickRequest(
            mediaType,
            allowMultiple,
            maxSelectableMediaCountBuilder,
            moongchiPickerListener
        )

        fun createMoongchiPickerDialog(): MoongchiPickerDialog {
            val maxSelectableMEdiaCount = if (allowMultiple) {
                maxSelectableMediaCountBuilder()
            } else {
                1
            }
            return MoongchiPickerDialog.newInstance(
                mediaType,
                moongchiPickerDialogListener,
                maxSelectableMEdiaCount,
                maxVisibleMediaCount
            )
        }

        fun createShowMoongchiPickerWithPermissionDelegate(): () -> Unit {
            val mediaPermissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val launcher = activity.registerPermissionRequestLauncher(
                permissionsToRequest = mediaPermissions,
                onPermissionGranted = {
                    createMoongchiPickerDialog().show(fragmentManager, null)
                },
                withDeniedPermissions = {
                    moongchiPickerListener.onFailed(Throwable("permission denied : ${it.toDebugString()}"))
                }
            )
            return { launcher.launch() }
        }

        request = if (allowPermissionRequest) {
            createShowMoongchiPickerWithPermissionDelegate()
        } else {
            { createMoongchiPickerDialog().show(fragmentManager, null) }
        }
    }


    fun show() {
        request()
    }
}

/**
 * this class must created on [AppCompatActivity.onCreate]
 * @param allowPermissionRequest allow to request permissions for reading and writing media
 * @param allowMultiple allow to pick multiple media from gallery
 * @param maxMediaCountBuilder builder for build max selection count for fetching media from gallery
 * @param maxVisibleMediaCount MoongchiPickerDialog shows this amount of media items
 */
fun AppCompatActivity.createMoongchiPicker(
    allowPermissionRequest: Boolean = false,
    mediaType: PetMediaType,
    allowMultiple: Boolean = false,
    maxMediaCountBuilder: () -> Int = { 1 },
    maxVisibleMediaCount: Int = MoongchiPickerDialog.MAX_VISIBLE_MEDIA_COUNT,
    moongchiPickerListener: MoongchiPickerListener
) = MoongchiPicker(this, supportFragmentManager, allowPermissionRequest, mediaType, allowMultiple, maxMediaCountBuilder, maxVisibleMediaCount, moongchiPickerListener)


/**
 * this class must created on [Fragment.onViewCreated]
 * @param allowPermissionRequest allow to request permissions for reading and writing media
 * @param allowMultiple allow to pick multiple media from gallery
 * @param maxMediaCountBuilder builder for build max selection count for fetching media from gallery
 * @param maxVisibleMediaCount MoongchiPickerDialog shows this amount of media items
 */
fun Fragment.createMoongchiPicker(
    allowPermissionRequest: Boolean = false,
    mediaType: PetMediaType,
    allowMultiple: Boolean = false,
    maxMediaCountBuilder: () -> Int = { 1 },
    maxVisibleMediaCount: Int = MoongchiPickerDialog.MAX_VISIBLE_MEDIA_COUNT,
    moongchiPickerListener: MoongchiPickerListener
) = MoongchiPicker(requireActivity(), childFragmentManager, allowPermissionRequest, mediaType, allowMultiple, maxMediaCountBuilder, maxVisibleMediaCount, moongchiPickerListener)