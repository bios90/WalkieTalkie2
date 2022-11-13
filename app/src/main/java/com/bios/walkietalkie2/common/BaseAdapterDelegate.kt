package com.bios.walkietalkie2.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bios.walkietalkie2.models.DelegateAdapterItem

abstract class BaseAdapterDelegate<M : DelegateAdapterItem, VH : RecyclerView.ViewHolder> :
    AdapterDelegate<M> {

    override fun isViewForType(items: List<DelegateAdapterItem>, position: Int): Boolean =
        isViewForType(items[position])

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(parent, inflater)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        items: List<DelegateAdapterItem>,
        position: Int,
        payloads: List<DelegateAdapterItem.Payloadable>,
    ) = onBindViewHolder(items[position] as M, holder as VH, payloads)

    abstract fun onBindViewHolder(
        item: M,
        holder: VH,
        payloads: List<DelegateAdapterItem.Payloadable>,
    )

    abstract fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
    ): RecyclerView.ViewHolder

    abstract fun isViewForType(item: DelegateAdapterItem): Boolean

    override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) = Unit

    override fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder) = Unit

    override fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder) = Unit
}