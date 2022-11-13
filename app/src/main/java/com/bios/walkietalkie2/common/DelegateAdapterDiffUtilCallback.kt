package com.bios.walkietalkie2.common

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.bios.walkietalkie2.models.DelegateAdapterItem

class DelegateAdapterDiffUtilCallback : DiffUtil.ItemCallback<DelegateAdapterItem>() {

    override fun areItemsTheSame(
        oldItem: DelegateAdapterItem,
        newItem: DelegateAdapterItem,
    ): Boolean = oldItem::class == newItem::class && oldItem.id() == newItem.id()

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: DelegateAdapterItem,
        newItem: DelegateAdapterItem,
    ): Boolean {
        val isEqual = newItem.equalToOther(oldItem)
        return isEqual
    }

    override fun getChangePayload(oldItem: DelegateAdapterItem, newItem: DelegateAdapterItem): Any {
        return oldItem.payload(newItem)
    }
}