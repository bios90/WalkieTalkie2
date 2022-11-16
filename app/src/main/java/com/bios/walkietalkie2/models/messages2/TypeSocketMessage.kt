package com.bios.walkietalkie2.models.messages2

enum class TypeSocketMessage {
    Ping,
    Pong,
    Voice,
    Bye
    ;

    fun getTypeInt() = values().indexOf(this)

    companion object {
        fun fromInt(index: Int) = values().getOrNull(index)
    }
}
