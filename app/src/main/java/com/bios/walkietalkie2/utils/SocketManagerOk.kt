package com.bios.walkietalkie2.utils

import com.bios.walkietalkie2.ActCall
import com.xuhao.didi.core.iocore.interfaces.IIOCoreOptions
import com.xuhao.didi.socket.client.sdk.OkSocket
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager
import com.xuhao.didi.socket.common.interfaces.common_interfacies.dispatcher.IRegister
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientPool
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerShutdown

class SocketManagerOk(
    val args: ActCall.Args,
) {
    private val PORT = 9584

    private var serverManager: IServerManager<IIOCoreOptions>? = null
    private var manager: IConnectionManager? = null

    fun start() {
        if (args.isGroupOwner) {
            startServer()
        } else {
            startClient(args.groupOwnerAddress.hostAddress!!)
        }
    }

    fun send() {
      
    }

    private fun startClient(address: String) {
        val connectionInfo = ConnectionInfo(address, PORT)
        manager = OkSocket.open(connectionInfo)
        manager?.connect()
    }

    private fun startServer() {
        val server: IRegister<IServerActionListener, IServerManager<IIOCoreOptions>> = OkSocket.server(9584)
        val manager = server.registerReceiver(object : IServerActionListener {
            override fun onServerListening(serverPort: Int) {
            }

            override fun onClientConnected(client: IClient?, serverPort: Int, clientPool: IClientPool<*, *>?) {
            }

            override fun onClientDisconnected(client: IClient?, serverPort: Int, clientPool: IClientPool<*, *>?) {
            }

            override fun onServerWillBeShutdown(
                serverPort: Int,
                shutdown: IServerShutdown?,
                clientPool: IClientPool<*, *>?,
                throwable: Throwable?
            ) {
            }

            override fun onServerAlreadyShutdown(serverPort: Int) {
            }
        })
        manager.listen()

        serverManager = manager
    }
}
