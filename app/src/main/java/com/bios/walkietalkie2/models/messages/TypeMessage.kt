package com.bios.walkietalkie2.models.messages

enum class TypeMessage {
    Ping,
    Pong,
    Voice;

    fun getTypeInt() = TypeMessage.values().indexOf(this)

    companion object {
        fun initFromInt(index: Int) = TypeMessage.values().get(index)
    }
}
