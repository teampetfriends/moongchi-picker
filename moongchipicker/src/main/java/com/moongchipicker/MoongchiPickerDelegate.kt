package com.moongchipicker

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.*
import java.io.Serializable
import kotlin.math.min

enum class PetMediaType : Serializable {
    IMAGE, VIDEO
}

/**
 * this class contains every detail of MoongchiPicker
 */
internal class MoongchiPickerDelegate(
    private val activity: AppCompatActivity
) {

    /**
     * this method should called on [AppCompatActivity.onCreate]
     * @return [MoongchiPickerDialogListener] has to be set on [MoongchiPickerDialog]
     */
    fun registerMediaPickRequest(
        mediaType: PetMediaType,
        isAllowMultiple: Boolean,
        maxMediaCountBuilder: () -> Int,
        moongchiPickerListener: MoongchiPickerListener
    ): MoongchiPickerDialogListener {
        val cameraRequest = registerCameraRequest(mediaType, moongchiPickerListener)
        val galleryRequest = registerPickFromGalleryRequest(
            mediaType,
            isAllowMultiple,
            maxMediaCountBuilder,
            moongchiPickerListener
        )

        return object : MoongchiPickerDialogListener {
            override fun onSubmitMedia(uris: List<Uri>) {
                moongchiPickerListener.onSubmitMedia(uris)
            }

            override fun onClickCamera() {
                cameraRequest.launch()
            }

            override fun onClickGallery() {
                galleryRequest.launch()
            }
        }
    }


    private fun registerCameraRequest(
        mediaType: PetMediaType,
        moongchiPickerListener: MoongchiPickerListener
    ): StatefulActivityResultLauncher<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> {
                registerTakePictureRequest(
                    onSuccess = { moongchiPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { moongchiPickerListener.onFailed(it) }
                )
            }
            PetMediaType.VIDEO -> {
                registerTakeVideoRequest(
                    onSuccess = { moongchiPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { moongchiPickerListener.onFailed(it) }
                )
            }
        }
    }

    private fun registerPickFromGalleryRequest(
        mediaType: PetMediaType,
        isMultipleImage: Boolean = false,
        maxImageCountBuilder: () -> Int,
        moongchiPickerListener: MoongchiPickerListener
    ): StatefulActivityResultLauncher<String> {
        return when (mediaType) {
            PetMediaType.IMAGE -> {
                if (isMultipleImage) {
                    createPickMultiplePictureFromGallery(
                        onSuccess = {
                            val maxImageCount = maxImageCountBuilder()
                            if (it.size > maxImageCount) {
                                moongchiPickerListener.onSelectedMediaCountOverLimit(
                                    maxImageCount
                                )
                            }
                            moongchiPickerListener.onSubmitMedia(
                                it.subList(0, min(it.size, maxImageCount))
                            )
                        },
                        onFailed = { moongchiPickerListener.onFailed(it) }
                    )
                } else {
                    createPickPictureFromGalleryRequest(
                        onSuccess = { moongchiPickerListener.onSubmitMedia(listOf(it)) },
                        onFailed = { moongchiPickerListener.onFailed(it) }
                    )
                }
            }
            PetMediaType.VIDEO -> {
                createPickVideoFromGallery(
                    onSuccess = { moongchiPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { moongchiPickerListener.onFailed(it) }
                )
            }
        }
    }


    private fun createPickPictureFromGalleryRequest(
        onSuccess: (Uri) -> Unit,
        onFailed: (Throwable) -> Unit
    ) =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri: Uri? ->
            if (contentUri == null) {
                onFailed(Throwable("[ActivityResultContracts.GetContent()] failed : result uri is null"))
            } else {
                onSuccess(contentUri)
            }

        }.toStatefulActivityResultLauncher("image/*")

    private fun createPickMultiplePictureFromGallery(
        onSuccess: (List<Uri>) -> Unit,
        onFailed: (Throwable) -> Unit
    ) =
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { contentUri: List<Uri>? ->
            if (contentUri == null) {
                onFailed(Throwable("[ActivityResultContracts.GetMultipleContents()] failed : result uri is null"))
            } else {
                onSuccess(contentUri)
            }
        }.toStatefulActivityResultLauncher("image/*")


    private fun createPickVideoFromGallery(
        onSuccess: (Uri) -> Unit,
        onFailed: (Throwable) -> Unit
    ) =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri: Uri? ->
            if (contentUri == null) {
                onFailed(Throwable("[ActivityResultContracts.GetContent()] failed : result uri is null"))
            } else {
                onSuccess(contentUri)
            }
        }.toStatefulActivityResultLauncher("video/*")


    private fun registerTakePictureRequest(
        onSuccess: (Uri) -> Unit,
        onFailed: (Throwable) -> Unit
    ) =
        activity.registerTakePictureLauncher(
            onSuccess = onSuccess,
            onFailed = onFailed
        )

    private fun registerTakeVideoRequest(onSuccess: (Uri) -> Unit, onFailed: (Throwable) -> Unit) =
        activity.registerTakeVideoLauncher(
            onSuccess = onSuccess,
            onFailed = onFailed
        )


}