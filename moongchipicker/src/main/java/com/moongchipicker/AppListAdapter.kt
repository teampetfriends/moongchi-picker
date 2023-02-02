package com.moongchipicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class AppListAdapter<T, B : ViewDataBinding>(
    private val layoutId: Int,
    diffUtilCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, AppListAdapter.ViewHolder<B>>(diffUtilCallback) {

    class ViewHolder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> {
        val binding : B = DataBindingUtil.inflate<B>(LayoutInflater.from(parent.context), layoutId, parent, false).apply {
            lifecycleOwner = parent.findViewTreeLifecycleOwner()
        }
        return ViewHolder(binding)
    }

}