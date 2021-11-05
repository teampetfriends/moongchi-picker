package com.moongchipicker

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moongchipicker.*
import com.moongchipicker.data.Photo
import com.moongchipicker.databinding.DialogMoongchiPickerBinding
import com.moongchipicker.databinding.ItemSelectedMediaBinding
import com.moongchipicker.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

internal interface MoongchiPickerDialogListener : Serializable {
    fun onSubmitMedia(uris: List<Uri>)
    fun onClickCamera()
    fun onClickGallery()
}

internal class MoongchiPickerDialog private constructor(
) : BottomSheetDialogFragment() {

    private val binding: DialogMoongchiPickerBinding by lazy {
        DialogMoongchiPickerBinding.inflate(layoutInflater)
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


        val moongchiPickerListener =
            arguments?.getSerializable(EXTRA_MEDIA_PICKER_LISTENER) as? MoongchiPickerDialogListener ?: return
        val maxImageCount = arguments?.getInt(EXTRA_MAX_IMAGE_COUNT) ?: 1
        val mediaType = arguments?.getSerializable(EXTRA_MEDIA_TYPE) as? PetMediaType ?: return

        if (maxImageCount <= 1) {
            binding.selectedMediaFrame.visibility = View.GONE
            binding.submit.visibility = View.GONE
        }
        when (mediaType) {
            PetMediaType.IMAGE -> binding.title.text = getString(R.string.pick_image)
            PetMediaType.VIDEO -> binding.title.text = getString(R.string.pick_video)
        }


        val selectedPhotos = MutableLiveData<MutableList<Photo>>(mutableListOf())
        val mediaItemRecyclerViewAdapter = MoongchiPickerRecyclerViewAdapter(
            maxImageCount,
            selectedPhotos,
            viewLifecycleOwner,
            object : MediaItemClickListener {
                override fun onClickCamera() {
                    moongchiPickerListener.onClickCamera()
                    dismiss()
                }

                override fun onClickGallery() {
                    moongchiPickerListener.onClickGallery()
                    dismiss()
                }

                override fun onSubmit(uri: Uri) {
                    moongchiPickerListener.onSubmitMedia(listOf(uri))
                    dismiss()
                }
            })

        binding.recyclerMoongchiPicker.adapter = mediaItemRecyclerViewAdapter


        selectedPhotos.observe(this, Observer {
            if (it.size > 0) {
                binding.selectedMediaPlaceholder.visibility = View.INVISIBLE
            } else {
                binding.selectedMediaPlaceholder.visibility = View.VISIBLE
            }

            updateSelectedImageLayout(it) { deselectedPhoto ->
                selectedPhotos.value =
                    selectedPhotos.value.toSafe().toMutableList().apply { remove(deselectedPhoto) }
            }
        })

        binding.submit.setOnClickListener {
            moongchiPickerListener.onSubmitMedia(selectedPhotos.value?.map { it.uri }.toSafe())
            dismiss()
        }


        loadMediaFileFromStorage(mediaType, MAX_MEDIA_SIZE) {
            //most recently modified file comes first
            for (uri in it.reversed()) {
                addMediaToAdapter(
                    mediaType,
                    requireContext().contentResolver,
                    uri,
                    mediaItemRecyclerViewAdapter
                )
            }
        }

    }


    private fun updateSelectedImageLayout(photos: List<Photo>, onDeselect: (Photo) -> Unit) {
        binding.selectedMedia.removeAllViews()
        for (photo in photos) {
            val itemBinding =
                ItemSelectedMediaBinding.inflate(layoutInflater, binding.selectedMedia, false)
            itemBinding.media.setImageBitmap(photo.bitmap)
            binding.selectedMedia.addView(itemBinding.root)
            itemBinding.remove.setOnClickListener {
                onDeselect(photo)
            }
        }
    }


    private fun loadMediaFileFromStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int,
        onLoaded: suspend (List<Uri>) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            requireContext().apply {
                onLoaded(loadMedia(mediaType, maxMediaSize))
            }
        }
    }


    private suspend fun loadMedia(mediaType: PetMediaType, maxMediaCount: Int): List<Uri> {
        val fromExternal = loadMediaFromExternalStorage(mediaType, maxMediaCount)
        val imageRemain = maxMediaCount - fromExternal.size
        return if (imageRemain > 0) {
            fromExternal + loadMediaFromInternalStorage(mediaType, imageRemain)
        } else {
            fromExternal
        }
    }

    private suspend fun loadMediaFromExternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> requireContext().loadImagesFromExternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> requireContext().loadVideosFromExternalStorage(maxMediaSize)
        }
    }

    private suspend fun loadMediaFromInternalStorage(
        mediaType: PetMediaType,
        maxMediaSize: Int
    ): List<Uri> {
        return when (mediaType) {
            PetMediaType.IMAGE -> requireContext().loadImagesFromInternalStorage(maxMediaSize)
            PetMediaType.VIDEO -> requireContext().loadVideosFromInternalStorage(maxMediaSize)
        }
    }

    private fun getMediaBitmap(
        mediaType: PetMediaType,
        contentResolver: ContentResolver,
        uri: Uri
    ): Bitmap? {
        return when (mediaType) {
            PetMediaType.IMAGE -> {
                BitmapHelper.getBitmapFromUri(
                    uri,
                    contentResolver,
                    BitmapHelper.BitmapSize.SMALL
                )
            }
            PetMediaType.VIDEO -> {
                BitmapHelper.getBitmapFromVideo(requireContext(), uri)
            }
        }
    }


    private suspend fun addMediaToAdapter(
        mediaType: PetMediaType,
        contentResolver: ContentResolver,
        uri: Uri,
        moongchiItemAdapter: MoongchiPickerRecyclerViewAdapter
    ) {
        withContext(Dispatchers.IO) {
            val bitmap = getMediaBitmap(mediaType, contentResolver, uri) ?: return@withContext
            withContext(Dispatchers.Main) {
                moongchiItemAdapter.addPhoto(Photo(uri, bitmap))
            }
        }
    }


    companion object {
        private const val EXTRA_MAX_IMAGE_COUNT = "EXTRA_MAX_IMAGE_COUNT"
        private const val EXTRA_MEDIA_PICKER_LISTENER = "EXTRA_MEDIA_PICKER_LISTENER"
        private const val EXTRA_MEDIA_TYPE = "EXTRA_MEDIA_TYPE"

        private const val MAX_MEDIA_SIZE = 25

        fun newInstance(
            mediaType: PetMediaType,
            moongchiPickerDialogListener: MoongchiPickerDialogListener,
            maxImageCount: Int = 1
        ): MoongchiPickerDialog {
            val args = Bundle()
            args.putInt(EXTRA_MAX_IMAGE_COUNT, maxImageCount)
            args.putSerializable(EXTRA_MEDIA_PICKER_LISTENER, moongchiPickerDialogListener)
            args.putSerializable(EXTRA_MEDIA_TYPE, mediaType)
            val fragment = MoongchiPickerDialog()
            fragment.arguments = args
            return fragment
        }
    }
}