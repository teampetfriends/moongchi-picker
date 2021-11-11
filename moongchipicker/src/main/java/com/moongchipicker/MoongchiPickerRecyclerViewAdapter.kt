package com.moongchipicker

import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.moongchipicker.data.Photo
import com.moongchipicker.databinding.ItemMediaBinding
import com.moongchipicker.util.toSafe

internal interface MediaItemClickListener {
    fun onClickCamera()
    fun onClickGallery()

    /**
     *  when [MoongchiPickerRecyclerViewAdapter.maxImageCount] is 1, then once user click media tile
     *  the uri submitted immediately
     */
    fun onSubmit(uri: Uri)
}


internal class MoongchiPickerRecyclerViewAdapter(
    private val maxImageCount: Int = 1,
    private val selectedPhotos: MutableLiveData<MutableList<Photo>>,
    lifecycleOwner: LifecycleOwner,
    private val onMediaItemClickListener: MediaItemClickListener
) : RecyclerView.Adapter<MoongchiPickerRecyclerViewAdapter.ViewHolder>() {

    //Photo.empty() is placeholder for camera, gallery tile
    private var photos = mutableListOf(Photo.empty(), Photo.empty())

    class ViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        selectedPhotos.observe(lifecycleOwner, Observer {
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.binding.root.context
        val mediaImageView = holder.binding.media
        when (position) {
            //for camera button
            0 -> {
                mediaImageView.setImageDrawable(
                    ResourcesCompat.getDrawableForDensity(
                        context.resources,
                        R.drawable.moongchi_photo_camera_24,
                        DisplayMetrics.DENSITY_XHIGH,
                        null
                    )
                )
                mediaImageView.setPadding(ICON_PADDING)
                mediaImageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.pet_black,
                        null
                    )
                )
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickCamera()
                }
            }
            //for gallery button
            1 -> {
                mediaImageView.setImageDrawable(
                    ResourcesCompat.getDrawableForDensity(
                        context.resources,
                        R.drawable.moongchi_photo_library_24,
                        DisplayMetrics.DENSITY_XHIGH,
                        null
                    )
                )
                mediaImageView.setPadding(ICON_PADDING)
                mediaImageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.pet_light_gray,
                        null
                    )
                )
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickGallery()
                }
            }
            else -> {
                mediaImageView.setPadding(0)

                val currentPhoto = photos.getOrNull(position) ?: return
                mediaImageView.setImageBitmap(currentPhoto.bitmap)

                if (selectedPhotos.value?.contains(currentPhoto).toSafe()) {
                    //선택표시
                    mediaImageView.setColorFilter(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.pet_black_translucent,
                            null
                        )
                    )
                } else {
                    mediaImageView.setColorFilter(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.pef_transparent,
                            null
                        )
                    )
                }


                mediaImageView.setOnClickListener {
                    if (maxImageCount <= 1) {
                        onMediaItemClickListener.onSubmit(currentPhoto.uri)
                        return@setOnClickListener
                    }
                    //when user click selected tile, then tile should removed from selectedPhotos
                     if (selectedPhotos.value?.contains(currentPhoto).toSafe()) {
                        selectedPhotos.value = selectedPhotos.value.toSafe().toMutableList()
                            .apply { remove(currentPhoto) }

                        notifyItemChanged(position)
                    } else {
                        if (selectedPhotos.value?.size.toSafe() < maxImageCount) {
                            selectedPhotos.value = selectedPhotos.value.toSafe().toMutableList()
                                .apply { add(currentPhoto) }
                            notifyItemChanged(position)
                        }
                        //when user select media over limit
                        else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.err_select_over_limit),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }


    fun addPhoto(photo: Photo) {
        photos.add(photo)
        notifyItemInserted(photos.size - 1)
    }

    companion object{
        private const val ICON_PADDING = 115
    }
}
