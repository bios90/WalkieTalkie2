package com.bios.walkietalkie2.utils

import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.ISocketWritable
import com.bios.walkietalkie2.models.messages2.MessageBye
import com.bios.walkietalkie2.models.messages2.MessagePing
import com.bios.walkietalkie2.models.messages2.MessagePong
import com.bios.walkietalkie2.models.messages2.MessageVoice
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object SocketHelper {

    @Volatile
    private var dos: DataOutputStream? = null

    @Volatile
    private var dis: DataInputStream? = null

    fun sendMessage(msg: ISocketWritable, stream: OutputStream) {
        dos = DataOutputStream(stream)
        dos?.let {
            try {
                it.writeShort(msg.getType().getTypeInt())
                it.writeInt(msg.getLength())
                it.write(msg.getBytes())
                it.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun readMessage(stream: InputStream): ISocketReadable? {
        dis = DataInputStream(stream)
        dis?.let {
            try {
                val typeInt = it.readShort().toInt()
                val length = it.readInt()
                val type = TypeSocketMessage.fromInt(typeInt) ?: return null
                val bytes = ByteArray(length)
                it.read(bytes)
                val msg = when (type) {
                    TypeSocketMessage.Ping -> MessagePing()
                    TypeSocketMessage.Pong -> MessagePong()
                    TypeSocketMessage.Voice -> MessageVoice()
                    TypeSocketMessage.Bye -> MessageBye()
                }
                msg.readFromBytes(bytes, length)
                return msg
            } catch (e: Throwable) {
                return null
            }
        }
        return null
    }

    fun sendRaw(bytes: ByteArray, length: Int, typeSocketMessage: TypeSocketMessage, stream: OutputStream) {
        dos = DataOutputStream(stream)
        dos?.let {
            try {
                it.writeShort(typeSocketMessage.getTypeInt())
                it.writeInt(length)
                it.write(bytes)
                it.flush()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}
