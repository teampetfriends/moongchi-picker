package com.moongchipicker.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoongchiPickerParam(
    val mediaType: MediaType,
    val maxSelectableMediaCount: Int = 1,
    val maxVisibleMediaCount: Int = 25
) : Parcelable

@Parcelize
internal data class MoongchiPickerDialogParam(
    val mediaType: MediaType,
    val maxSelectableMediaCount: Int = 1,
    val maxVisibleMediaCount: Int = 25,
    val selectedUriList: List<Uri> = emptyList()
) : Parcelable

internal fun MoongchiPickerParam.toDialogParam() = MoongchiPickerDialogParam(
    mediaType = mediaType,
    maxSelectableMediaCount = maxSelectableMediaCount,
    maxVisibleMediaCount = maxVisibleMediaCount
)



