package com.bios.walkietalkie2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bios.walkietalkie2.models.messages2.ISocketReadable;
import com.bios.walkietalkie2.models.messages2.ISocketWritable;
import com.bios.walkietalkie2.models.messages2.MessageBye;
import com.bios.walkietalkie2.models.messages2.MessagePing;
import com.bios.walkietalkie2.models.messages2.MessagePong;
import com.bios.walkietalkie2.models.messages2.MessageVoice;
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketHelperJava {

    @Nullable
    public static DataOutputStream dos;

    @Nullable
    public static DataInputStream dis;

    private static byte[] readBytes;

    public static void sendMessage(ISocketWritable msg, OutputStream stream) {
        try {
            dos = new DataOutputStream(stream);
            dos.writeShort(msg.getType().getTypeInt());
            dos.writeInt(msg.getLength());
            dos.write(msg.getBytes());
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int typeInt,length;
    private static TypeSocketMessage type;
    private static ISocketReadable writable;
    @Nullable
    public static ISocketReadable readMessage(InputStream stream) {
        try {
            dis = new DataInputStream(stream);
            typeInt = dis.readShort();
            length = dis.readInt();
            type = TypeSocketMessage.Companion.fromInt(typeInt);
            if (type == null) {
                return null;
            }
            readBytes = new byte[length];
            dis.readFully(readBytes, 0, length);
            switch (type) {
                case Ping:
                    writable = new MessagePing();
                    break;
                case Pong:
                    writable = new MessagePong();
                    break;
                case Voice:
                    writable = new MessageVoice();
                    break;
                case Bye:
                    writable = new MessageBye();
                    break;
            }
            writable.readFromBytes(readBytes, length);
            return writable;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendRaw(byte[] bytes, int length, TypeSocketMessage typeSocketMessage, OutputStream stream) {
        try {
            dos = new DataOutputStream(stream);
            dos.writeShort(typeSocketMessage.getTypeInt());
            dos.writeInt(length);
            dos.write(bytes);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

/*    fun sendRaw(bytes: ByteArray, length: Int, typeSocketMessage: TypeSocketMessage, stream: OutputStream) {
        dos = DataOutputStream(stream)
        dos?.let {
            try {
                it.writeShort(typeSocketMessage.getTypeInt())
                it.writeInt(length)
                it.write(bytes)
                it.flush()
            } catch (e: Throwable) {
                it.flush()
            }
        }
    }*/
}
