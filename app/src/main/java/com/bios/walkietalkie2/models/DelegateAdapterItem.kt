package com.bios.walkietalkie2.models

interface DelegateAdapterItem {

    fun id(): Any

    fun equalToOther(other: Any): Boolean {
        return this == other
    }

    fun payload(other: Any): Payloadable = Payloadable.None

    interface Payloadable {
        object None : Payloadable
    }
}