package com.bios.walkietalkie2.common

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bios.walkietalkie2.models.DelegateAdapterItem

class CompositeAdapter(
    private val delegates: SparseArray<AdapterDelegate<*>>,
) : ListAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>(DelegateAdapterDiffUtilCallback()) {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    var isSmoothScroll = false

    override fun getItemViewType(position: Int): Int {
        for (i in 0 until delegates.size()) {
            if (delegates[i].isViewForType(currentList, position)) {
                return delegates.keyAt(i)
            }
        }
        throw NullPointerException(
            "Can not get viewType for position $position and type ${
                currentList.get(
                    position
                ).javaClass.simpleName
            }"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        delegates[viewType].onCreateViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        this.layoutManager = recyclerView.layoutManager!!
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewDetachedFromWindow(holder)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewAttachedToWindow(holder)
        super.onViewAttachedToWindow(holder)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val delegateAdapter = delegates[getItemViewType(position)]

        if (delegateAdapter != null) {
            val delegatePayloads = payloads.map { it as DelegateAdapterItem.Payloadable }
            delegateAdapter.onBindViewHolder(holder, currentList, position, delegatePayloads)
        } else {
            throw NullPointerException("can not find adapter for position $position")
        }
    }

    fun setItems(items: List<DelegateAdapterItem>) = this.submitList(items)

    private fun scrollToPos(pos: Int) {
        if (isSmoothScroll) {
            val scroller = getSmoothScroller()
            scroller.targetPosition = pos
            layoutManager.startSmoothScroll(scroller)
        } else {
            this.recyclerView.scrollToPosition(pos)
        }
    }

    private fun getSmoothScroller() = object : LinearSmoothScroller(recyclerView.context) {
        override fun getVerticalSnapPreference(): Int {
            return LinearSmoothScroller.SNAP_TO_START
        }
    }

    class Builder {
        private var count: Int = 0
        private val delegates: SparseArray<AdapterDelegate<*>> =
            SparseArray()

        @Suppress("UNCHECKED_CAST")
        fun add(delegateAdapter: AdapterDelegate<out DelegateAdapterItem>): Builder = apply {
            delegates.put(
                count++,
                delegateAdapter as AdapterDelegate<DelegateAdapterItem>
            )
        }

        fun add(vararg delegateAdapters: AdapterDelegate<out DelegateAdapterItem>) = apply {
            for (delegate in delegateAdapters) {
                add(delegate)
            }
        }

        fun build(): CompositeAdapter {
            require(count != 0) { "Register at least one adapter" }
            return CompositeAdapter(delegates)
        }
    }
}
