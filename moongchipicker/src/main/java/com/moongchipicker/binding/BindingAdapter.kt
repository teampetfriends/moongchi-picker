package com.moongchipicker.binding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moongchipicker.util.whatIfNotNullAs

@BindingAdapter("submitList")
fun RecyclerView.bindSubmitList(itemList: List<Any>?) {
    adapter.whatIfNotNullAs<ListAdapter<Any, *>> { adapter ->
        adapter.submitList(itemList ?: listOf())
    }
}

@BindingAdapter("isVisible")
fun View.bindSubmitList(isVisible : Boolean) {
    if(isVisible){
        visibility = View.VISIBLE
    }else{
        visibility = View.GONE
    }
}