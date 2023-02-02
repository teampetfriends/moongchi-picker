package com.moongchipicker.data

import java.io.Serializable

enum class MediaType(val mimeType: String) : Serializable {
    IMAGE("image/*"), VIDEO("video/*")
}
