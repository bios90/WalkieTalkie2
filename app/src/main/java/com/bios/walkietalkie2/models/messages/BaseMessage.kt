package com.bios.walkietalkie2.models.messages

import com.bios.walkietalkie2.utils.AudioUtils
import com.bios.walkietalkie2.utils.enumValueOrNull
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*

interface BaseMessage {
    val type: TypeMessage
    fun fromBytes(buffer: ByteBuffer)
    fun toBytes(buffer: ByteBuffer)
}

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

data class PongMessage(
    override val type: TypeMessage = TypeMessage.Pong,
) : BaseMessage {

    var text: String = ""
    override fun fromBytes(buffer: ByteBuffer) {
        text = buffer.toStringMy()
    }

    override fun toBytes(buffer: ByteBuffer) = text.strToBytes(buffer)
}

private val textBuffer = ByteBuffer.allocate(2048)
private val voiceBuffer = ByteBuffer.allocate(AudioUtils.bufferRecordSize)
private val headerBuffer = ByteBuffer.allocate(10)

fun sendMessage(msg: BaseMessage, socketChannel: SocketChannel) {
//    val size = if (msg is VoiceMessage) {
//        bufferSize
//    } else {
//        2048
//    }
//    val bbMsg = ByteBuffer.allocate(size)
    val bbMsg = if (msg is VoiceMessage) {
        voiceBuffer
    } else {
        textBuffer
    }
    bbMsg.putInt(msg.type.getTypeInt())
    msg.toBytes(bbMsg)
    bbMsg.flip()
//    msg.type.name.strToBytes(bbMsg)
//    msg.toBytes(bbMsg)
//    bbMsg.flip()

//    val bbOverall = ByteBuffer.allocate(10)
    val bbOverall = headerBuffer.apply { clear() }
    bbOverall.putInt(bbMsg.remaining())
    bbOverall.flip()

    val finalData = arrayOf(bbOverall, bbMsg)
    socketChannel.write(finalData)
    bbMsg.clear()
    bbOverall.clear()
}

fun ensureBytesAvailable(socketChannel: SocketChannel, buffer: ByteBuffer, required: Int) {
    if (buffer.position() != 0) {
        buffer.compact()
    }

    while (buffer.position() < required) {
        val length = socketChannel.read(buffer)
        if (socketChannel.isOpen.not() || length <= 0) {
            throw IOException("Socket closed while reading")
        }
    }

    buffer.flip()
}


fun readMessage(socketChannel: SocketChannel, dataBuffer: ByteBuffer): BaseMessage? {
    ensureBytesAvailable(socketChannel, dataBuffer, 8)
    val length = dataBuffer.int
    ensureBytesAvailable(socketChannel, dataBuffer, length)
//    val typeMessage: TypeMessage? = dataBuffer.toStringMy().let(::enumValueOrNull)
    val typeMessage: TypeMessage? = dataBuffer.int.let(TypeMessage::initFromInt)
    val msg = when (typeMessage) {
        TypeMessage.Ping -> PingMessage()
        TypeMessage.Pong -> PongMessage()
        TypeMessage.Voice -> VoiceMessage()
        else -> null
    }

    msg?.fromBytes(dataBuffer)
    return msg
}
