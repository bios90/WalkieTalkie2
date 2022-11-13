package com.bios.walkietalkie2.common

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bios.walkietalkie2.models.DelegateAdapterItem

interface AdapterDelegate<T : DelegateAdapterItem> {
    fun isViewForType(items: List<DelegateAdapterItem>, position: Int): Boolean
    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        items: List<DelegateAdapterItem>,
        position: Int,
        payloads: List<DelegateAdapterItem.Payloadable>,
    )

    fun onViewRecycled(viewHolder: RecyclerView.ViewHolder)
    fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder)
    fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder)
}

