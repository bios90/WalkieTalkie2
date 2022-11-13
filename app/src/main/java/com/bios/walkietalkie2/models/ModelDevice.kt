package com.bios.walkietalkie2.models

import java.io.Serializable

data class ModelDevice(
    val macAddress: String,
    val name: String,
) : DelegateAdapterItem, Serializable {
    override fun id() = macAddress
}