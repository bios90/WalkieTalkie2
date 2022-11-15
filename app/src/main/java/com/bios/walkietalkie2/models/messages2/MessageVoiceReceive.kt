package com.bios.walkietalkie2.models.messages2

class MessageVoiceReceive(
    var bytes: ByteArray? = null,
) : ISocketReadable {

    override fun getType(): TypeSocketMessage = TypeSocketMessage.VoiceReceive
    var length: Int = 0

    override fun readFromBytes(bytes: ByteArray, length: Int) {
        this.bytes = bytes
        this.length = length
    }
}
