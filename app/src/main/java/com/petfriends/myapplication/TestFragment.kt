package com.petfriends.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.moongchipicker.MoongchiPickerListener
import com.moongchipicker.PetMediaType
import com.moongchipicker.createMoongchiPicker

class TestFragment : Fragment(R.layout.fragment_test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iv = requireView().findViewById<ImageView>(R.id.iv)
        val picker = createMoongchiPicker(
            mediaType = PetMediaType.VIDEO,
            allowPermissionRequest = true,
            allowMultiple = false,
            maxMediaCountBuilder = { 5 },
            moongchiPickerListener = object : MoongchiPickerListener {
                override fun onSubmitMedia(contentUris: List<Uri>) {
                    //do something you want to do with media
                    if(contentUris.isEmpty()){
                        return
                    }
                    iv.setImageBitmap(BitmapHelper.getBitmapFromUri(contentUris.first(), requireContext().contentResolver))
                }

                override fun onFailed(t: Throwable) {

                }

                override fun onSelectedMediaCountOverLimit(limit: Int) {

                }
            })

        iv.setOnClickListener {
            picker.show()
        }

    }
}