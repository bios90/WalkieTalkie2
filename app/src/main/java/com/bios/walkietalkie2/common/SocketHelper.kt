package com.bios.walkietalkie2.common

import io.socket.client.IO
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

object SocketHelper {
    private const val PORT_USED = 9584
    private const val TIMEOUT = 10000

    fun startServerSocket(): Socket? {
        try {
            val serverSocket = ServerSocket(PORT_USED)
            val socket = serverSocket.accept()
            return socket
        } catch (e: Exception) {
            return null
        }
    }

    fun startAsClient(address: InetAddress): Socket? {
        try {
            val socket = Socket()
            socket.connect(
                InetSocketAddress(address.hostAddress, PORT_USED),
                TIMEOUT
            )
            return socket
        } catch (e: Exception) {
            return null
        }
    }
}