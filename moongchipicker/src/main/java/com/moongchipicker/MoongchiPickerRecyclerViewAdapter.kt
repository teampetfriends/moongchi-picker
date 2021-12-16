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
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.MoongchiItemMediaBinding
import com.moongchipicker.util.toSafe

internal interface MediaItemClickListener {
    fun onClickCamera()
    fun onClickGallery()

    /**
     *  when [MoongchiPickerRecyclerViewAdapter.maxImageCount] is 1, then once user click media tile
     *  the uri submitted immediately
     */
    fun onSubmit(uri: Uri)

    /**
     * for instance, fail to load bimap from [Media.uri]
     */
    fun onFailed(t: Throwable)
}


internal class MoongchiPickerRecyclerViewAdapter(
    private val maxImageCount: Int = 1,
    private val selectedMediaList: MutableLiveData<MutableList<Media>>,
    lifecycleOwner: LifecycleOwner,
    private val onMediaItemClickListener: MediaItemClickListener
) : RecyclerView.Adapter<MoongchiPickerRecyclerViewAdapter.ViewHolder>() {

    //Photo.empty() is placeholder for camera, gallery tile
    private var mediaList = mutableListOf(Media.empty(), Media.empty())

    class ViewHolder(val binding: MoongchiItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        selectedMediaList.observe(lifecycleOwner, Observer {
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MoongchiItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
                        R.color.mc_black,
                        null
                    )
                )
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickCamera()
                }
                mediaImageView.setColorFilter(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.mc_transparent,
                        null
                    )
                )
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
                        R.color.mc_light_gray,
                        null
                    )
                )
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickGallery()
                }
                mediaImageView.setColorFilter(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.mc_transparent,
                        null
                    )
                )
            }
            else -> {
                mediaImageView.setPadding(0)

                val currentMedia = mediaList.getOrNull(position) ?: return

                kotlin.runCatching {
                    mediaImageView.setImageBitmap(currentMedia.getBitmap(context))
                }.onFailure {
                    onMediaItemClickListener.onFailed(it)
                }

                if (selectedMediaList.value?.contains(currentMedia).toSafe()) {
                    //선택표시
                    mediaImageView.setColorFilter(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.mc_black_translucent,
                            null
                        )
                    )
                } else {
                    mediaImageView.setColorFilter(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.mc_transparent,
                            null
                        )
                    )
                }


                mediaImageView.setOnClickListener {
                    if (maxImageCount <= 1) {
                        onMediaItemClickListener.onSubmit(currentMedia.uri)
                        return@setOnClickListener
                    }
                    //when user click selected tile, then tile should removed from selectedMediaList
                    if (selectedMediaList.value?.contains(currentMedia).toSafe()) {
                        selectedMediaList.value = selectedMediaList.value.toSafe().toMutableList()
                            .apply { remove(currentMedia) }

                        notifyItemChanged(position)
                    } else {
                        if (selectedMediaList.value?.size.toSafe() < maxImageCount) {
                            selectedMediaList.value = selectedMediaList.value.toSafe().toMutableList()
                                .apply { add(currentMedia) }
                            notifyItemChanged(position)
                        }
                        //when user select media over limit
                        else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.mc_err_select_over_limit),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }


    fun addMedia(media: Media) {
        mediaList.add(media)
        notifyItemInserted(mediaList.size - 1)
    }

    companion object {
        private const val ICON_PADDING = 115
    }
}
