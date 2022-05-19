package com.moongchipicker

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.MoongchiItemMediaBinding
import com.moongchipicker.util.dpToPx

internal interface MediaItemClickListener {
    fun onClickCamera()
    fun onClickGallery()
    fun onMediaSelected(media : Media)
    fun onMediaDeSelected(media: Media)
    fun isMediaSelected(media: Media) : Boolean
    fun getSelectedMediaCount() : Int


    /**
     *  when [MediaListAdapter.maxImageCount] is 1, then once user click media tile
     *  the uri submitted immediately
     */
    fun onSubmit(uri: Uri)

    /**
     * for instance, fail to load bimap from [Media.uri]
     */
    fun onFailed(t: Throwable)
}


internal class MediaListAdapter(
    private val maxImageCount: Int,
    private val onMediaItemClickListener: MediaItemClickListener,
) : ListAdapter<Media, MediaListAdapter.ViewHolder>(Media.diffUtil) {

    class ViewHolder(val binding: MoongchiItemMediaBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MoongchiItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.binding.root.context
        val mediaImageView = holder.binding.media
        when (position) {
            //for camera button
            0 -> {
                mediaImageView.setImageResource(R.drawable.moongchi_photo_camera_24)
                mediaImageView.setPadding(mediaImageView.context.dpToPx(ICON_PADDING_IN_DP.toFloat()))
                mediaImageView.setBackgroundColor(ContextCompat.getColor(context, R.color.mc_black))
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickCamera()
                }
                mediaImageView.setColorFilter(ContextCompat.getColor(context, R.color.mc_transparent))
            }
            //for gallery button
            1 -> {
                mediaImageView.setImageResource(R.drawable.moongchi_photo_library_24)
                mediaImageView.setPadding(mediaImageView.context.dpToPx(ICON_PADDING_IN_DP.toFloat()))
                mediaImageView.setBackgroundColor(ContextCompat.getColor(context, R.color.mc_light_gray))
                mediaImageView.setOnClickListener {
                    onMediaItemClickListener.onClickGallery()
                }
                mediaImageView.setColorFilter(ContextCompat.getColor(context, R.color.mc_transparent))
            }
            else -> {
                mediaImageView.setPadding(0)

                val currentMedia = getItem(position)

                kotlin.runCatching {
                    mediaImageView.setImageBitmap(currentMedia.getBitmap(context))
                }.onFailure {
                    onMediaItemClickListener.onFailed(it)
                }

                if (onMediaItemClickListener.isMediaSelected(currentMedia)) {
                    //선택표시
                    mediaImageView.setColorFilter(ContextCompat.getColor(context, R.color.mc_black_translucent))
                } else {
                    mediaImageView.setColorFilter(ContextCompat.getColor(context, R.color.mc_transparent))
                }


                mediaImageView.setOnClickListener {
                    if (maxImageCount <= 1) {
                        onMediaItemClickListener.onSubmit(currentMedia.uri)
                        return@setOnClickListener
                    }
                    //when user click selected tile, then tile should removed from selectedMediaList
                    if (onMediaItemClickListener.isMediaSelected(currentMedia)) {
                        onMediaItemClickListener.onMediaDeSelected(currentMedia)
                        notifyItemChanged(position)
                    } else {
                        if (onMediaItemClickListener.getSelectedMediaCount() < maxImageCount) {
                            onMediaItemClickListener.onMediaSelected(currentMedia)
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

    companion object {
        private const val ICON_PADDING_IN_DP = 35
    }
}
