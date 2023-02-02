package com.moongchipicker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoongchiPickerParam(
    val mediaType: MediaType,
    val maxSelectableMediaCount: Int = 1,
    val maxVisibleMediaCount: Int = 25
) : Parcelable
