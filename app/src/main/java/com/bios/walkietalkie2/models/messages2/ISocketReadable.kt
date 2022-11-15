package com.bios.walkietalkie2.models.messages2

import java.nio.ByteBuffer

interface ISocketReadable {
    fun getType(): TypeSocketMessage
    fun readFromBytes(bytes: ByteArray, length:Int)
}
