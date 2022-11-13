package com.bios.walkietalkie2.common

import io.socket.client.IO
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

object SocketChannelHelper {
    private const val PORT_USED = 9584
    private const val TIMEOUT = 10000

    fun startServerSocketChannel(): SocketChannel? {
        try {
            val serverSocket = ServerSocketChannel.open()
            serverSocket.socket().bind(InetSocketAddress(PORT_USED))
            val socket = serverSocket.accept()
            return socket
        } catch (e: Exception) {
            return null
        }
    }

    fun startAsClient(address: InetAddress): SocketChannel? {
        try {
            val socket = SocketChannel.open(InetSocketAddress(address.hostAddress, PORT_USED))
            return socket
        } catch (e: Exception) {
            return null
        }
    }
}

/*
*
*     SocketChannel client = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));

                    // write
                    String request = "hello - from client [" + Thread.currentThread().getName() + "}";
                    byte[] bs = request.getBytes(StandardCharsets.UTF_8);
                    ByteBuffer buffer = ByteBuffer.wrap(bs);
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
* */