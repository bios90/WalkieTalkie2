package com.bios.walkietalkie2.models.messages2

import java.nio.ByteBuffer
import java.util.*

data class MessagePong(
    override var text: String = UUID.randomUUID().toString()
) : ISocketTextMessage {
    override fun getType(): TypeSocketMessage = TypeSocketMessage.Pong
}
