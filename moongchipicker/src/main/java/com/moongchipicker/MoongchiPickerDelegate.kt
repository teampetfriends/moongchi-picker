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
 * 권한을 얻기, 사진이나 동영상 촬영, 갤러리에서 미디어 가져오기, [MoongchiPickerDialog] 열기 등
 * [MoongchiPickerDelegate]의 세부적인 구현을 모아놓은 클래스
 * 비디오 여러개 선택은 지원 예정
 */
internal class MoongchiPickerDelegate(
    private val activity: AppCompatActivity
) {

    /**
     *   * Note: context을 이용해 파일을 사진, 동영상을 저장할 임시파일을 만들기 때문에 onCreate에서 불려야한다.
     * @param maxImageCountBuilder : [mediaType]이 [PetMediaType.IMAGE] 일시, 선택할 수 있는 이미지 수.
     * @return [MoongchiPickerDialog] 를 열 수 있는 리퀘스트런처
     */
    fun registerMediaPickRequest(
        mediaType: PetMediaType,
        isAllowMultiple: Boolean,
        maxImageCountBuilder: () -> Int,
        moongchiPickerListener: MoongchiPickerListener
    ): MoongchiPickerDialogListener {
        val cameraRequest = registerCameraRequest(mediaType, moongchiPickerListener)
        val galleryRequest = registerPickFromGalleryRequest(
            mediaType,
            isAllowMultiple,
            maxImageCountBuilder,
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