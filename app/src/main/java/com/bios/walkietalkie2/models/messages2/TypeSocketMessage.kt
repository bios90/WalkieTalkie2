package com.bios.walkietalkie2.models.messages2

enum class TypeSocketMessage {
    Ping,
    Pong,
    VoiceSend,
    VoiceReceive;

    fun getTypeInt() = values().indexOf(this)

    companion object {
        fun fromInt(index: Int) = values().getOrNull(index)
    }
}
