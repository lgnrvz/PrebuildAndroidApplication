package com.nrvz.prebuildapplication.utilities.adapter

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

class SimpleViewHolder constructor(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root), LifecycleOwner {

    private val mLifecycleRegistry = LifecycleRegistry(this)

    init {
        mLifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    fun markAttach() {
        mLifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun markDetach() {
        mLifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }
}