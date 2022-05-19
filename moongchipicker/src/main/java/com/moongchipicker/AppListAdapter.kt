package com.moongchipicker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class AppListAdapter<T, B : ViewDataBinding>(
    private val layoutId: Int,
    diffUtilCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, AppListAdapter.ViewHolder<B>>(diffUtilCallback) {

    class ViewHolder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false))
    }

}