package com.bios.walkietalkie2.models.messages2

import java.nio.ByteBuffer

interface ISocketWritable {
    fun getType(): TypeSocketMessage
    fun writeToBytes(bytes: ByteArray, offset: Int)
    fun getLength(): Int
}
