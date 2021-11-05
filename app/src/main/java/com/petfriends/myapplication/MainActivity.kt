package com.petfriends.myapplication

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.moongchipicker.MoongchiPicker
import com.moongchipicker.MoongchiPickerListener
import com.moongchipicker.PetMediaType
import com.moongchipicker.util.BitmapHelper
import com.petfriends.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy{
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moongchiPicker = MoongchiPicker(this,
            mediaType = PetMediaType.IMAGE,
            isAutoPermission = true,
            moongchiPickerListener = object : MoongchiPickerListener{
                override fun onSubmitMedia(contentUris: List<Uri>) {
                    if(contentUris.isEmpty()){
                        return
                    }
                    binding.iv.setImageBitmap(BitmapHelper.getBitmapFromUri(contentUris.first(), contentResolver))
                }

                override fun onFailed(t: Throwable) {
                    Log.w("petfriends", t.message.toString())
                }

            })

        binding.iv.setOnClickListener {
            moongchiPicker.show()
        }

    }
}