package com.moongchipicker.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface MoongchiPickerResult : Parcelable {
    @Parcelize
    class Success(val uriList: List<Uri>) : MoongchiPickerResult

    @Parcelize
    class Failure(val errorMsg: String?) : MoongchiPickerResult
}
