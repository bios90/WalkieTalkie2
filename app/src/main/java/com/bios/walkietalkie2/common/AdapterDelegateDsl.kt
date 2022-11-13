package com.bios.walkietalkie2.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bios.walkietalkie2.models.DelegateAdapterItem

inline fun <reified M : DelegateAdapterItem, reified V : ViewBinding> adapterDelegate(
    noinline bindingProvider: (ViewGroup) -> V = { parent -> parent.createBinding() },
    noinline on: (item: DelegateAdapterItem) -> Boolean = { item -> item is M },
    noinline init: AdapterDelegateViewHolder<M, V>.() -> Unit = {},
): AdapterDelegate<M> {
    return DslAdapterDelegate(
        bindingProvider = bindingProvider,
        on = on,
        init = init,
    )
}

class DslAdapterDelegate<M : DelegateAdapterItem, V : ViewBinding>(
    private val bindingProvider: (ViewGroup) -> V,
    private val on: (item: DelegateAdapterItem) -> Boolean,
    private val init: AdapterDelegateViewHolder<M, V>.() -> Unit = {},
) : BaseAdapterDelegate<M, AdapterDelegateViewHolder<M, V>>() {

    override fun isViewForType(item: DelegateAdapterItem): Boolean = on(item)

    override fun onBindViewHolder(
        item: M,
        holder: AdapterDelegateViewHolder<M, V>,
        payloads: List<DelegateAdapterItem.Payloadable>,
    ) {
        holder._item = item
        holder._payloads = payloads
        holder._bind?.invoke()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
    ): RecyclerView.ViewHolder {
        val binding = bindingProvider(parent)
        return AdapterDelegateViewHolder<M, V>(binding)
            .also { holder -> init(holder) }
    }

    override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as? AdapterDelegateViewHolder<M, V>
        holder?._onViewRecycled?.invoke()
    }

    override fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as? AdapterDelegateViewHolder<M, V>
        holder?._onViewAttachedToWindow?.invoke()
    }

    override fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as? AdapterDelegateViewHolder<M, V>
        holder?._onViewDetachedFromWindow?.invoke()
    }
}

class AdapterDelegateViewHolder<M : DelegateAdapterItem, V : ViewBinding>(val binding: V) :
    RecyclerView.ViewHolder(binding.root) {

    private object Uninitialized

    internal var _item: Any = Uninitialized
    internal var _payloads: List<DelegateAdapterItem.Payloadable> = listOf()
    internal var _bind: (() -> Unit)? = null
        private set
    internal var _onViewRecycled: (() -> Unit)? = null
        private set
    internal var _onViewAttachedToWindow: (() -> Unit)? = null
        private set
    internal var _onViewDetachedFromWindow: (() -> Unit)? = null
        private set

    @Suppress("UNCHECKED_CAST")
    val item: M
        get() = if (_item === Uninitialized) {
            throw RuntimeException("*** Not initialized ***")
        } else {
            _item as M
        }

    val payloads: List<DelegateAdapterItem.Payloadable>
        get() = _payloads

    fun bind(block: () -> Unit) {
        _bind = block
    }

    fun onViewRecycled(block: () -> Unit) {
        _onViewRecycled = block
    }

    fun onViewAttachedToWindow(block: () -> Unit) {
        _onViewAttachedToWindow = block
    }

    fun onViewDetachedFromWindow(block: () -> Unit) {
        _onViewDetachedFromWindow = block
    }
}

inline fun <reified T : ViewBinding> ViewGroup.createBinding(): T {
    val vbClass = T::class.java
    val inflater = LayoutInflater.from(context)
    val method = vbClass.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
    return method.invoke(null, inflater, this, false) as T
}