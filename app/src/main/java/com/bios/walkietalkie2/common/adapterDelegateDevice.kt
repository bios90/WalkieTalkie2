package com.bios.walkietalkie2.common

import com.bios.walkietalkie2.databinding.ItemDeviceBinding
import com.bios.walkietalkie2.models.ModelDevice

fun adapterDelegateDevice(
    onClick: (ModelDevice) -> Unit,
) = adapterDelegate<ModelDevice, ItemDeviceBinding>() {
    binding.lalItem.setOnClickListener {
        onClick.invoke(item)
    }
    bind {
        binding.tvName.text = item.name
        binding.tvAddress.text = item.macAddress
    }
}