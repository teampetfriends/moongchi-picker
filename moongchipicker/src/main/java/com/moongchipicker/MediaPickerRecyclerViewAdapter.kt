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
     * [MediaPickerRecyclerViewAdapter]의
     * [MediaPickerRecyclerViewAdapter.maxImageCount]가 1 일시, 사진을 누르자마자 submit한다.
     */
    fun onSubmit(uri: Uri)
}


internal class MediaPickerRecyclerViewAdapter(
    private val maxImageCount: Int = 1,
    private val selectedPhotos: MutableLiveData<MutableList<Photo>>,
    lifecycleOwner: LifecycleOwner,
    private val onMediaItemClickListener: MediaItemClickListener
) : RecyclerView.Adapter<MediaPickerRecyclerViewAdapter.ViewHolder>() {

    //Photo.empty()는 카메라, 갤러리를 위한 빈공간을 가지기 위해 사용
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
            //카메라 요청버튼
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
            //갤러리 요청버튼
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
                    //이미 선택되어있는 사진을 클릭시에 selectedPhotos 에서 그 사진을 제거한다.
                    //Note : selectedPhotos는 liveData이기 때문에 단순히 add 로는 이벤트를 발생시키지 않는다.
                    //따라서 리스트를 새로 만들어서 넣어준다.
                     if (selectedPhotos.value?.contains(currentPhoto).toSafe()) {
                        selectedPhotos.value = selectedPhotos.value.toSafe().toMutableList()
                            .apply { remove(currentPhoto) }

                        notifyItemChanged(position)
                    } else {
                        //아니면 선택된 된 사진을 selectedPhotos에 추가한다.
                        if (selectedPhotos.value?.size.toSafe() < maxImageCount) {
                            selectedPhotos.value = selectedPhotos.value.toSafe().toMutableList()
                                .apply { add(currentPhoto) }
                            notifyItemChanged(position)
                        }
                        //허용 이미지 갯수를 넘은 경우
                        else {
                            Toast.makeText(
                                context,
                                "더이상 이미지를 추가할 수 없어요!",
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
