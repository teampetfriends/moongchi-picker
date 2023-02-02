package com.moongchipicker

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.DialogMoongchiPickerBinding
import com.moongchipicker.databinding.MoongchiItemSelectedMediaBinding
import com.moongchipicker.util.MediaLoader
import com.moongchipicker.util.setResult
import com.moongchipicker.util.toSafe
import kotlinx.parcelize.Parcelize


class MoongchiPickerDialog : BottomSheetDialogFragment() {

    @Parcelize
    data class DialogInfo(
        val mediaType: MediaType,
        val maxSelectableMediaCount: Int = 1,
        val maxVisibleMediaCount: Int = 25
    ) : Parcelable

    sealed interface DialogResult : Parcelable {
        @Parcelize
        class Success(val mediaUriList: List<Uri>) : DialogResult

        @Parcelize
        class Failure(val errorMessage: String?) : DialogResult

        @Parcelize
        object OpenGallery : DialogResult

        @Parcelize
        object OpenCamera : DialogResult
    }

    private lateinit var binding: DialogMoongchiPickerBinding

    private val dialogInfo: DialogInfo by lazy {
        arguments?.getParcelable(DIALOG_INFO_KEY)!!
    }


    private val vm: MoongchiPickerDialogViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MoongchiPickerDialogViewModel(MediaLoader(requireContext())) as T
            }
        }).get(MoongchiPickerDialogViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMoongchiPickerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm
        binding.isImagePicker =
            dialogInfo.mediaType == MediaType.IMAGE
        binding.allowMultipleSelection = dialogInfo.maxSelectableMediaCount.toSafe() > 1
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadMedia(dialogInfo.mediaType, dialogInfo.maxVisibleMediaCount)

        val mediaListAdapter = MediaListAdapter(
            dialogInfo.maxSelectableMediaCount,
            object : MediaItemClickListener {
                override fun onClickCamera() {
                    setResult(DialogResult.OpenCamera)
                    dismiss()
                }

                override fun onClickGallery() {
                    setResult(DialogResult.OpenGallery)
                    dismiss()
                }

                override fun onMediaSelected(media: Media) {
                    vm.addMediaSelect(media)
                }

                override fun onMediaDeSelected(media: Media) {
                    vm.removeMediaSelect(media)
                }

                override fun isMediaSelected(media: Media): Boolean {
                    return vm.selectedMediaList.value.toSafe().contains(media)
                }

                override fun getSelectedMediaCount(): Int {
                    return vm.selectedMediaList.value.toSafe().size
                }

                override fun onSubmit(uri: Uri) {
                    setResult(DialogResult.Success(listOf(uri)))
                    dismiss()
                }

                override fun onFailed(t: Throwable) {
                    setResult(DialogResult.Failure(t.message))
                    dismiss()
                }
            })

        binding.mediaItems.adapter = mediaListAdapter

        //미디어 아이템 선택시
        vm.selectedMediaList.observe(this, Observer { mediaList ->
            //ui 업데이트
            mediaListAdapter.notifyDataSetChanged()
            updateSelectedMediaView(mediaList)
        })

        binding.submit.setOnClickListener {
            val selectedMediaUriList: List<Uri> =
                vm.selectedMediaList.value?.map { it.uri }.toSafe()
            setResult(DialogResult.Success(selectedMediaUriList))
            dismiss()
        }

    }


    fun updateSelectedMediaView(selectedMediaList: List<Media>) {
        binding.selectedMediaItems.removeAllViews()
        selectedMediaList.forEach { item ->
            MoongchiItemSelectedMediaBinding.inflate(
                layoutInflater,
                binding.selectedMediaItems,
                true
            ).apply {
                media.setImageBitmap(item.getBitmap(root.context))
                remove.setOnClickListener {
                    vm.removeMediaSelect(item)
                }
            }
        }
    }

    private fun setResult(result: Any?) {
        setResult(REQUEST_MOONGCHI_PICKER_DIALOG, bundleOf(DIALOG_RESULT to result))
    }

    companion object {
        const val DIALOG_INFO_KEY = "DIALOG_INFO_KEY"
        const val DIALOG_RESULT = "DIALOG_RESULT"

        fun parseDialogResult(result: Bundle): DialogResult? = result.getParcelable(DIALOG_RESULT)
    }
}