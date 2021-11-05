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
 * 권한을 얻기, 사진이나 동영상 촬영, 갤러리에서 미디어 가져오기, MediaPickerDialog 열기 등
 * MediaPicker의 세부적인 구현을 모아놓은 클래스
 * 비디오 여러개 선택은 지원 예정
 */
internal class MediaPickerDelegate(
    private val activity: AppCompatActivity
) {

    /**
     *   * Note: context을 이용해 파일을 사진, 동영상을 저장할 임시파일을 만들기 때문에 onCreate에서 불려야한다.
     * @param maxImageCountBuilder : [mediaType]이 [PetMediaType.IMAGE] 일시, 선택할 수 있는 이미지 수.
     * @return [MediaPickerDialog] 를 열 수 있는 리퀘스트런처
     */
    fun registerMediaPickRequest(
        mediaType: PetMediaType,
        isAllowMultiple: Boolean,
        maxImageCountBuilder: () -> Int,
        mediaPickerListener: MediaPickerListener
    ): MediaPickerDialogListener {
        val cameraRequest = registerCameraRequest(mediaType, mediaPickerListener)
        val galleryRequest = registerPickFromGalleryRequest(
            mediaType,
            isAllowMultiple,
            maxImageCountBuilder,
            mediaPickerListener
        )

        return object : MediaPickerDialogListener {
            override fun onSubmitMedia(uris: List<Uri>) {
                mediaPickerListener.onSubmitMedia(uris)
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
        mediaPickerListener: MediaPickerListener
    ): StatefulActivityResultLauncher<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> {
                registerTakePictureRequest(
                    onSuccess = { mediaPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { mediaPickerListener.onFailed(it) }
                )
            }
            PetMediaType.VIDEO -> {
                registerTakeVideoRequest(
                    onSuccess = { mediaPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { mediaPickerListener.onFailed(it) }
                )
            }
        }
    }

    private fun registerPickFromGalleryRequest(
        mediaType: PetMediaType,
        isMultipleImage: Boolean = false,
        maxImageCountBuilder: () -> Int,
        mediaPickerListener: MediaPickerListener
    ): StatefulActivityResultLauncher<String> {
        return when (mediaType) {
            PetMediaType.IMAGE -> {
                if (isMultipleImage) {
                    createPickMultiplePictureFromGallery(
                        onSuccess = {
                            val maxImageCount = maxImageCountBuilder()
                            if (it.size > maxImageCount) {
                                mediaPickerListener.onSelectedMediaCountOverLimit(
                                    maxImageCount
                                )
                            }
                            mediaPickerListener.onSubmitMedia(
                                it.subList(0, min(it.size, maxImageCount))
                            )
                        },
                        onFailed = { mediaPickerListener.onFailed(it) }
                    )
                } else {
                    createPickPictureFromGalleryRequest(
                        onSuccess = { mediaPickerListener.onSubmitMedia(listOf(it)) },
                        onFailed = { mediaPickerListener.onFailed(it) }
                    )
                }
            }
            PetMediaType.VIDEO -> {
                createPickVideoFromGallery(
                    onSuccess = { mediaPickerListener.onSubmitMedia(listOf(it)) },
                    onFailed = { mediaPickerListener.onFailed(it) }
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