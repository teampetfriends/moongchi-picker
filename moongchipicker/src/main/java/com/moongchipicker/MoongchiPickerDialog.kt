package com.moongchipicker

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.DialogMoongchiPickerBinding
import com.moongchipicker.databinding.MoongchiItemSelectedMediaBinding
import com.moongchipicker.util.*
import java.io.Serializable

internal interface MoongchiPickerDialogListener : Serializable {
    fun onSubmitMedia(uris: List<Uri>)
    fun onClickCamera()
    fun onClickGallery()
    fun onFailed(t: Throwable)
}

internal class MoongchiPickerDialog(
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogMoongchiPickerBinding

    private var moongchiPickerDialogListener: MoongchiPickerDialogListener? = null

    fun setMoongchiPickerDialogListener(listener: MoongchiPickerDialogListener) {
        moongchiPickerDialogListener = listener
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
        binding.recyclerMoongchiPicker.layoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        binding.isImagePicker =
            (arguments?.getSerializable(EXTRA_MEDIA_TYPE) as? PetMediaType ?: PetMediaType.IMAGE) == PetMediaType.IMAGE
        binding.allowMultipleSelection = arguments?.getInt(EXTRA_MAX_SELECTABLE_MEDIA_COUNT).toSafe() > 1
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (moongchiPickerDialogListener == null) {
            dismiss()
            return
        }

        val maxVisibleMediaCount = arguments?.getInt(EXTRA_MAX_VISIBLE_MEDIA_COUNT) ?: MAX_VISIBLE_MEDIA_COUNT
        val mediaType = arguments?.getSerializable(EXTRA_MEDIA_TYPE) as? PetMediaType ?: PetMediaType.IMAGE
        val maxSelectableMediaCount = arguments?.getInt(EXTRA_MAX_SELECTABLE_MEDIA_COUNT).toSafe()

        vm.loadMedia(mediaType, maxVisibleMediaCount)

        val mediaListAdapter = MediaListAdapter(
            maxSelectableMediaCount,
            object : MediaItemClickListener {
                override fun onClickCamera() {
                    moongchiPickerDialogListener?.onClickCamera()
                    dismiss()
                }

                override fun onClickGallery() {
                    moongchiPickerDialogListener?.onClickGallery()
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
                    moongchiPickerDialogListener?.onSubmitMedia(listOf(uri))
                    dismiss()
                }

                override fun onFailed(t: Throwable) {
                    moongchiPickerDialogListener?.onFailed(t)
                    dismiss()
                }
            })

        binding.recyclerMoongchiPicker.adapter = mediaListAdapter

        //미디어 아이템 선택시
        vm.selectedMediaList.observe(this, Observer { mediaList ->
            //ui 업데이트
            mediaListAdapter.notifyDataSetChanged()
            updateSelectedImageLayout(mediaList) { deselected ->
                vm.removeMediaSelect(deselected)
            }
        })

        binding.submit.setOnClickListener {
            moongchiPickerDialogListener?.onSubmitMedia(vm.selectedMediaList.value?.map { it.uri }.toSafe())
            dismiss()
        }

    }


    private fun updateSelectedImageLayout(mediaList: List<Media>, onDeselect: (Media) -> Unit) {
        binding.selectedMedia.removeAllViews()
        for (media in mediaList) {
            val itemBinding =
                MoongchiItemSelectedMediaBinding.inflate(layoutInflater, binding.selectedMedia, false)
            itemBinding.media.setImageBitmap(media.getBitmap(requireContext()))
            binding.selectedMedia.addView(itemBinding.root)
            itemBinding.remove.setOnClickListener {
                onDeselect(media)
            }
        }
    }


    companion object {
        private const val EXTRA_MEDIA_TYPE = "EXTRA_MEDIA_TYPE"
        private const val EXTRA_MAX_SELECTABLE_MEDIA_COUNT = "EXTRA_MAX_IMAGE_COUNT"
        private const val EXTRA_MAX_VISIBLE_MEDIA_COUNT = "EXTRA_MAX_MEDIA_SIZE"
        const val MAX_VISIBLE_MEDIA_COUNT = 25

        /**
         * @param maxVisibleMediaCount MoongchiPickerDialog shows this amount of media items
         */
        fun newInstance(
            mediaType: PetMediaType,
            moongchiPickerDialogListener: MoongchiPickerDialogListener,
            maxSelectableMediaCount: Int = 1,
            maxVisibleMediaCount: Int = MAX_VISIBLE_MEDIA_COUNT
        ): MoongchiPickerDialog {
            val args = Bundle()
            args.putSerializable(EXTRA_MEDIA_TYPE, mediaType)
            args.putInt(EXTRA_MAX_SELECTABLE_MEDIA_COUNT, maxSelectableMediaCount)
            args.putInt(EXTRA_MAX_VISIBLE_MEDIA_COUNT, maxVisibleMediaCount)
            val fragment = MoongchiPickerDialog()
            fragment.setMoongchiPickerDialogListener(moongchiPickerDialogListener)
            fragment.arguments = args
            return fragment
        }
    }
}