package com.bios.walkietalkie2.common

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.ActCall
import com.bios.walkietalkie2.utils.ForceInitializable
import com.bios.walkietalkie2.utils.addLifeCycleObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.net.Socket

class SocketReconnectionHelper(
    private val act: AppCompatActivity,
    private val args: ActCall.Args,
    private val onSocketChecked: (Socket?) -> Unit,
) : ForceInitializable {

    init {
        act.addLifeCycleObserver(
            onResume = {
                startPingPong()
                socket?.let(onSocketChecked)
            }
        )
    }

    private fun startPingPong() {
        act.lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                while (true) {
                    yield()
                    if (socket == null || socket?.isConnected?.not() == true) {
                        if (args.isGroupOwner) {
                            socket = SocketHelper.startServerSocket()
                        } else {
                            socket = SocketHelper.startAsClient(args.groupOwnerAddress)
                        }
                    }
                    socket.let(onSocketChecked)
                    delay(1000)
                }
            }
        )
    }

    companion object {
        private var socket: Socket? = null
    }
}
