package com.moongchipicker

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.DialogMoongchiPickerBinding
import com.moongchipicker.databinding.MoongchiItemSelectedMediaBinding
import com.moongchipicker.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import kotlin.jvm.Throws

internal interface MoongchiPickerDialogListener : Serializable {
    @MainThread
    fun onSubmitMedia(uris: List<Uri>)

    @MainThread
    fun onClickCamera()

    @MainThread
    fun onClickGallery()

    @MainThread
    fun onFailed(t: Throwable)
}

internal class MoongchiPickerDialog(
) : BottomSheetDialogFragment() {

    private val binding: DialogMoongchiPickerBinding by lazy {
        DialogMoongchiPickerBinding.inflate(layoutInflater)
    }

    private var moongchiPickerDialogListener: MoongchiPickerDialogListener? = null

    fun setMoongchiPickerDialogListener(listener: MoongchiPickerDialogListener) {
        moongchiPickerDialogListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.recyclerMoongchiPicker.layoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(moongchiPickerDialogListener == null){
            dismiss()
            return
        }

        val maxSelectableMediaCount = arguments?.getInt(EXTRA_MAX_SELECTABLE_MEDIA_COUNT) ?: 1
        val mediaType = arguments?.getSerializable(EXTRA_MEDIA_TYPE) as? PetMediaType ?: return
        val maxVisibleMediaCount = arguments?.getInt(EXTRA_MAX_VISIBLE_MEDIA_COUNT) ?: MAX_VISIBLE_MEDIA_COUNT


        if (maxSelectableMediaCount <= 1) {
            binding.selectedMediaFrame.visibility = View.GONE
            binding.submit.visibility = View.GONE
        }
        when (mediaType) {
            PetMediaType.IMAGE -> binding.title.text = getString(R.string.mc_pick_image)
            PetMediaType.VIDEO -> binding.title.text = getString(R.string.mc_pick_video)
        }


        val selectedMediaList = MutableLiveData<MutableList<Media>>(mutableListOf())

        val mediaItemRecyclerViewAdapter = MoongchiPickerRecyclerViewAdapter(
            maxSelectableMediaCount,
            selectedMediaList,
            viewLifecycleOwner,
            object : MediaItemClickListener {
                override fun onClickCamera() {
                    moongchiPickerDialogListener?.onClickCamera()
                    dismiss()
                }

                override fun onClickGallery() {
                    moongchiPickerDialogListener?.onClickGallery()
                    dismiss()
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

        binding.recyclerMoongchiPicker.adapter = mediaItemRecyclerViewAdapter



        selectedMediaList.observe(this, Observer {
            if (it.size > 0) {
                binding.selectedMediaPlaceholder.visibility = View.INVISIBLE
            } else {
                binding.selectedMediaPlaceholder.visibility = View.VISIBLE
            }

            kotlin.runCatching {
                updateSelectedImageLayout(it) { deselectedMedia ->
                    selectedMediaList.value =
                        selectedMediaList.value.toSafe().toMutableList().apply { remove(deselectedMedia) }
                }
            }.onFailure { t ->
                moongchiPickerDialogListener?.onFailed(t)
            }

        })

        binding.submit.setOnClickListener {
            moongchiPickerDialogListener?.onSubmitMedia(selectedMediaList.value?.map { it.uri }.toSafe())
            dismiss()
        }



        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val uriList = loadMedia(mediaType, maxVisibleMediaCount)
                for (uri in uriList) {
                    withContext(Dispatchers.Main) {
                        mediaItemRecyclerViewAdapter.addMedia(Media(uri, mediaType))
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    moongchiPickerDialogListener?.onFailed(it)
                }
            }
        }
    }


    @Throws
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

    @Throws
    private suspend fun loadMedia(mediaType: PetMediaType, maxMediaCount: Int): List<Uri> {
        val fromExternal = loadMediaFromExternalStorage(mediaType, maxMediaCount)
        val imageRemain = maxMediaCount - fromExternal.size
        return if (imageRemain > 0) {
            fromExternal + loadMediaFromInternalStorage(mediaType, imageRemain)
        } else {
            fromExternal
        }
    }

    @Throws
    private suspend fun loadMediaFromExternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> requireContext().loadImagesFromPublicExternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> requireContext().loadVideosFromPublicExternalStorage(maxMediaSize)
        }
    }

    @Throws
    private suspend fun loadMediaFromInternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> requireContext().loadImagesFromInternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> requireContext().loadVideosFromInternalStorage(maxMediaSize)
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