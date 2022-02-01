package com.nrvz.prebuildapplication.utilities.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class SimpleListAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, SimpleViewHolder>(AsyncDifferConfig.Builder<T>(diffCallback).build()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val binding = createBinding(parent, viewType)
        val viewHolder = SimpleViewHolder(binding)
        binding.lifecycleOwner = viewHolder
        return viewHolder
    }

    override fun onViewAttachedToWindow(holder: SimpleViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
    }

    override fun onViewDetachedFromWindow(holder: SimpleViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        if (position < itemCount) {
            bind(holder.binding, getItem(position), position)
            holder.binding.executePendingBindings()
        }
    }

    override fun submitList(list: List<T>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    protected abstract fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding

    protected abstract fun bind(binding: ViewDataBinding, item: T, position: Int)
}