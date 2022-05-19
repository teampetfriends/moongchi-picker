package com.moongchipicker

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.MainThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.*
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
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (moongchiPickerDialogListener == null) {
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

            mediaItemRecyclerViewAdapter.notifyDataSetChanged()
        })

        binding.submit.setOnClickListener {
            moongchiPickerDialogListener?.onSubmitMedia(selectedMediaList.value?.map { it.uri }.toSafe())
            dismiss()
        }


        vm.loadMedia(mediaType, maxVisibleMediaCount)


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