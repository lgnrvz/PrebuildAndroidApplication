package com.nrvz.prebuildapplication.utilities.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class SimpleDiffUtilAdapter(private val layoutRes: Int, private val onClickCallBack: Any? = null) :
    SimpleListAdapter<Any>(
        diffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    private lateinit var mRecyclerView: RecyclerView

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutRes,
            parent,
            false
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun bind(binding: ViewDataBinding, item: Any, position: Int) {
        when (binding) {

        }
    }
}