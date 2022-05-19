package com.moongchipicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moongchipicker.data.Media
import com.moongchipicker.databinding.MoongchiItemSelectedMediaBinding

class SelectedMediaListAdapter(
    private val onDeselect: (Media) -> Unit
) : AppListAdapter<Media, MoongchiItemSelectedMediaBinding>(R.layout.moongchi_item_selected_media, Media.diffUtil) {
    override fun onBindViewHolder(holder: ViewHolder<MoongchiItemSelectedMediaBinding>, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            media.setImageBitmap(item.getBitmap(root.context))
            remove.setOnClickListener {
                onDeselect(item)
            }
        }
    }
}