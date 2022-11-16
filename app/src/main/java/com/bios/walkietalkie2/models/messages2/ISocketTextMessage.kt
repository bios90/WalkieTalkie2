package com.bios.walkietalkie2.models.messages2

import java.nio.ByteBuffer

interface ISocketTextMessage : ISocketReadable, ISocketWritable {
    var text: String

    override fun readFromBytes(bytes: ByteArray, length: Int) {
        val copy = ByteArray(length)
        System.arraycopy(bytes, 0, copy, 0, length)
        this.text = String(copy)
    }

    override fun writeToBytes(bytes: ByteArray, offset: Int) {
        val textBytes = text.toByteArray()
        System.arraycopy(textBytes, 0, bytes, offset, textBytes.size)
    }

    override fun getBytes(): ByteArray = text.toByteArray()

    override fun getLength(): Int = text.toByteArray().size

    override fun clear() {
        this.text = ""
    }
}
