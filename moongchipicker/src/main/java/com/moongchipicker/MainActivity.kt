package com.moongchipicker

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moongchipicker.util.BitmapHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iv = findViewById<ImageView>(R.id.preview)
        val imagePicker = createSingleImageMediaPicker(iv)
        iv.setOnClickListener {
            imagePicker.show()
        }

    }


    private fun createSingleImageMediaPicker(iv: ImageView): MediaPicker {
        return MediaPicker(activity = this,
            mediaType = PetMediaType.IMAGE,
            isAllowMultiple = true,
            maxImageCountBuilder = { 5 },
            mediaPickerListener = object : MediaPickerListener {
                override fun onSubmitMedia(contentUris: List<Uri>) {
                    if (contentUris.isEmpty()) {
                        return
                    }
                    iv.setImageBitmap(
                        BitmapHelper.getBitmapFromUri(contentUris.first(), contentResolver)
                    )
                }

                override fun onFailed(t: Throwable) {
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onSelectedMediaCountOverLimit(limit: Int) {
                    super.onSelectedMediaCountOverLimit(limit)
                    Toast.makeText(this@MainActivity, "이미지 최대 추가 갯수를 초과했습니다", Toast.LENGTH_LONG)
                        .show()
                }

            })
    }

}