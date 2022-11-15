package com.bios.walkietalkie2.models.messages

import java.nio.ByteBuffer

data class VoiceMessage(
    override val type: TypeMessage = TypeMessage.Voice,
) : BaseMessage {
    var bytes: ByteArray? = null
    var receivedBuffer: ByteBuffer? = null
    override fun fromBytes(buffer: ByteBuffer) {
        receivedBuffer = buffer
//        val length = buffer.int
//        bytes = ByteArray(length)
//        buffer.get(bytes)
    }

    override fun toBytes(buffer: ByteBuffer) {
        bytes?.let {
            buffer.putInt(it.size)
            buffer.put(it)
        }
    }
}
