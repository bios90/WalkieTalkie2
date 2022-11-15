package com.bios.walkietalkie2.utils

import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.ISocketWritable
import com.bios.walkietalkie2.models.messages2.MessagePing
import com.bios.walkietalkie2.models.messages2.MessagePong
import com.bios.walkietalkie2.models.messages2.MessageVoiceReceive
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

object SocketHelper {
    data class ReadResult(
        val messageType: TypeSocketMessage?,
        val messageLength: Int,
        val bytes: ByteArray
    ) {
        fun copyEmpty() = this.copy(
            messageType = null,
            messageLength = -1
        )
    }

    private val sendBufferSize = 44000
    private val readBufferSize = 44000
    private var intSize = 4
    private var bytesToSend: ByteArray = ByteArray(sendBufferSize)
    private val typeAndLengthArray: ByteArray = ByteArray(intSize)
    private var readResult: ReadResult = ReadResult(
        messageType = null,
        messageLength = -1,
        bytes = ByteArray(0)
    )

//    fun sendRaw(bytes: ByteArray, length: Int, typeSocketMessage: TypeSocketMessage, stream: OutputStream) {
//        synchronized(bytesToSend) {
//            val typeInt = typeSocketMessage.getTypeInt()
//            write4BytesToBuffer(bytesToSend, 0, typeInt)
//            write4BytesToBuffer(bytesToSend, intSize, length)
//            System.arraycopy(bytes, 0, bytesToSend, intSize * 2, length)
//            stream.write(bytesToSend, 0, length + (intSize * 2))
//            stream.flush()
//        }
//    }

    private var dos: DataOutputStream? = null
    fun sendRaw2(bytes: ByteArray, length: Int, typeSocketMessage: TypeSocketMessage, stream: OutputStream) {
        dos = DataOutputStream(stream)
        dos?.let {
            synchronized(it) {
                it.writeInt(typeSocketMessage.getTypeInt())
                it.writeInt(length)
                it.write(bytes)
                it.flush()
            }
        }
    }

    private var dis: DataInputStream? = null
    fun readRaw2(inputStream: InputStream): ReadResult? {
        dis = DataInputStream(inputStream)
        synchronized(dis!!) {
            val typeInt = dis!!.readInt()
            val length = dis!!.readInt()
            val type = TypeSocketMessage.fromInt(typeInt) ?: return null
//        dis?.readFully(bytes, 0, length)
            val readedBytes = ByteArray(length)
            dis!!.read(readedBytes)
            return ReadResult(
                messageType = type,
                messageLength = length,
                bytes = readedBytes
            )
        }

    }

//    fun sendMessage(msg: ISocketWritable, stream: OutputStream) {
//
//        synchronized(bytesToSend) {
//            val typeInt = msg.getType().getTypeInt()
//            write4BytesToBuffer(bytesToSend, 0, typeInt)
//            write4BytesToBuffer(bytesToSend, intSize, msg.getLength())
//            msg.writeToBytes(bytesToSend, intSize * 2)
//            val length = (intSize * 2) + msg.getLength()
//            stream.write(bytesToSend, 0, length)
//            stream.flush()
//        }
//    }

//    fun read(stream: InputStream, bytes: ByteArray): ReadResult? {
//        return try {
//            var readLength = stream.read(typeAndLengthArray)
//            if (readLength < 0) {
//                readResult.copyEmpty()
//            }
//            val typeInt = bytesToInt(typeAndLengthArray)
//            val type = TypeSocketMessage.fromInt(typeInt)
//            readLength = stream.read(typeAndLengthArray)
//            if (readLength < 0) {
//                readResult.copyEmpty()
//            }
//            val messageLength = bytesToInt(typeAndLengthArray)
//            readLength = stream.read(bytes, 0, messageLength)
//            if (readLength < 0) {
//                return readResult.copyEmpty()
//            }
//            return readResult.copy(
//                messageType = type,
//                messageLength = messageLength,
//            )
//        } catch (e: Exception) {
//            null
//        }
//    }
}
