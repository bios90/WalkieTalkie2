package com.bios.walkietalkie2.models.messages2

import java.nio.ByteBuffer

data class MessageVoiceSend(
    private var data: ByteArray) : ISocketWritable {

    override fun getType(): TypeSocketMessage = TypeSocketMessage.VoiceSend

    override fun writeToBytes(bytes: ByteArray, offset: Int) {
        System.arraycopy(data, 0, bytes, offset, data.size)
    }

    override fun getLength(): Int = data.size
}
