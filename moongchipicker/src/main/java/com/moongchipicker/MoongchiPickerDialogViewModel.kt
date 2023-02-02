package com.moongchipicker

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.moongchipicker.data.Media
import com.moongchipicker.data.MediaType
import com.moongchipicker.util.MediaLoader
import com.moongchipicker.util.toLiveData
import com.moongchipicker.util.toSafe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MoongchiPickerDialogViewModel(
    private val mediaLoader: MediaLoader,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _selectedMediaList = MutableLiveData<List<Media>>(mutableListOf())
    val selectedMediaList = _selectedMediaList.toLiveData()

    private val _mediaList = MutableLiveData<List<Media>>(emptyList())
    val mediaList = _mediaList.map {
        listOf(Media.empty(), Media.empty()) + it
    }

    fun loadMedia(mediaType: MediaType, maxMediaCount: Int) {
        viewModelScope.launch(ioDispatcher) {
            val uriList: List<Uri> = kotlin.runCatching {
                val fromExternal = mediaLoader.loadMediaFromExternalStorage(mediaType, maxMediaCount)
                val imageRemain = maxMediaCount - fromExternal.size
                if (imageRemain > 0) {
                    fromExternal + mediaLoader.loadMediaFromInternalStorage(mediaType, imageRemain)
                } else {
                    fromExternal
                }
            }.getOrElse {
                it.printStackTrace()
                emptyList()
            }
            _mediaList.postValue(uriList.map { Media(it, mediaType) })
        }
    }

    fun addMediaSelect(media: Media) {
        _selectedMediaList.value = _selectedMediaList.value.toSafe() + listOf(media)
    }

    fun removeMediaSelect(media: Media) {
        _selectedMediaList.value = _selectedMediaList.value.toSafe() - listOf(media).toSet()
    }


}