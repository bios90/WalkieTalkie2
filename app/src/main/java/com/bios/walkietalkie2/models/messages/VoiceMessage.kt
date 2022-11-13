package com.bios.walkietalkie2.models.messages

import java.nio.ByteBuffer

data class VoiceMessage(
    override val type: TypeMessage = TypeMessage.Voice,
) : BaseMessage {
    var bytes: ByteArray? = null
    override fun fromBytes(buffer: ByteBuffer) {
        val length = buffer.int
        bytes = ByteArray(length)
        buffer.get(bytes)
    }

    override fun toBytes(buffer: ByteBuffer) {
        bytes?.let {
            buffer.putInt(it.size)
            buffer.put(it)
        }
    }
}


/*


fun String.strToBytes(buffer: ByteBuffer) {
    val bytes = this.toByteArray()
    val length = bytes.size
    buffer.putInt(length)
    buffer.put(bytes)
}

fun ByteBuffer.toStringMy(): String {
    val length = this.int
    val bytes = ByteArray(length)
    this.get(bytes)
    return String(bytes)
}

data class PingMessage(
    override val type: TypeMessage = TypeMessage.Ping,
) : BaseMessage {
    var text: String = UUID.randomUUID().toString()

    override fun fromBytes(buffer: ByteBuffer) {
        text = buffer.toStringMy()
    }

    override fun toBytes(buffer: ByteBuffer) = text.strToBytes(buffer)
}


    val bytes = this.toByteArray()
    val length = bytes.size
    buffer.putShort(length.toShort())
    buffer.put(bytes)
*
* data class PingMessage(
    override val type: TypeMessage = TypeMessage.Ping,
) : BaseMessage {
    var text: String = UUID.randomUUID().toString()

    override fun fromBytes(buffer: ByteBuffer) {
        text = buffer.toStringMy()
    }

    override fun toBytes(buffer: ByteBuffer) = text.strToBytes(buffer)
}

*
* */