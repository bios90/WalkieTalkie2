package com.bios.walkietalkie2.common

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.ActCall
import com.bios.walkietalkie2.utils.ForceInitializable
import com.bios.walkietalkie2.utils.addLifeCycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.yield
import java.net.Socket

class SocketReconnectionHelper(
    private val act: AppCompatActivity,
    private val args: ActCall.Args,
    private val onRecreated: (Socket) -> Unit,
) : ForceInitializable {
    private var connectionJob: Job? = null
    private var socket: Socket? = null

    init {
        act.addLifeCycleObserver(
            onResume = { startPingPong() },
            onDestroy = {
                connectionJob?.cancel()
                socket?.close()
            }
        )
    }

    private fun startPingPong() {
        connectionJob = act.lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                while (true) {
                    yield()
                    if (socket == null || socket!!.isConnected.not()) {
                        if (args.isGroupOwner) {
                            socket = SocketHelper.startServerSocket()
                        } else {
                            socket = SocketHelper.startAsClient(args.groupOwnerAddress)
                        }
                        socket?.let(onRecreated)
                    }
                    delay(3000)
                }
            }
        )
    }
}