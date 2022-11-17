package com.bios.walkietalkie2.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bios.walkietalkie2.ActCall
import com.bios.walkietalkie2.common.SocketHelper
import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.ISocketWritable
import com.bios.walkietalkie2.models.messages2.MessageBye
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import okhttp3.internal.closeQuietly
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SocketListenerService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var connectionJob: Job? = null
    private var messageListeningJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startNotification()
        setEvents()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val args = intent?.getArgs<ActCall.Args>() ?: return super.onStartCommand(intent, flags, startId)
        if (socket == null
            || socket?.isConnected?.not() == true
            || connectionJob?.isActive?.not() == true
        ) {
            startSocketConnection(args)
        }
        return START_NOT_STICKY
    }

    private fun startSocketConnection(args: ActCall.Args) {
        connectionJob = serviceScope.launch(
            context = Dispatchers.IO,
            block = {
                while (true) {
                    yield()
                    try {
                        if (socket == null || socket?.isConnected?.not() == true) {
                            if (args.isGroupOwner) {
                                socket = SocketHelper.startServerSocket()
                            } else {
                                socket = SocketHelper.startAsClient(args.groupOwnerAddress)
                            }
                            socket?.let(_flowSocket::tryEmit)
                            startMessagesListening()
                        }
                    } catch (e: Exception) {

                    }
                    delay(1000)
                }
            }
        )
    }

    private fun setEvents() {
        flowShutDown
            .onEach {
                socket?.tryGetOps()?.let { stream ->
                    SocketHelperJava.sendMessage(MessageBye(), stream)
                }
                socket?.closeQuietly()
                stopSelf()
            }
            .flowOn(Dispatchers.IO)
            .launchIn(serviceScope)

        flowMessageToSend
            .onEach { message ->
                socket?.tryGetOps()?.let { stream ->
                    SocketHelperJava.sendMessage(message, stream)
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(serviceScope)
    }

    private fun startMessagesListening() {
        messageListeningJob?.cancel()
        messageListeningJob = serviceScope.launch(
            context = Dispatchers.IO,
            block = {
                val fis = socket?.tryGetIps()
                if (fis != null) {
                    while (true) {
                        yield()
                        val message = SocketHelperJava.readMessage(fis)
                        message?.let(_flowMessagesReceived::tryEmit)
                    }
                }
            }
        )
    }

    private fun startNotification() = startForeground(
        NotificationHelper.SOCKET_NOTIFICATION_ID,
        NotificationHelper.showSocketListeningNotification()
    )

    override fun onDestroy() {
        socket?.close()
        connectionJob?.cancel()
        serviceJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        var socket: Socket? = null
        private val _flowSocket = MutableSharedFlow<Socket>(
            replay = 1,
            extraBufferCapacity = 1,
        )
        val flowSocket = _flowSocket.asSharedFlow()

        private val _flowMessagesReceived = MutableSharedFlow<ISocketReadable>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
        val flowMessagesReceived = _flowMessagesReceived.asSharedFlow()
        private val flowShutDown = MutableSharedFlow<Unit>()

        private val flowMessageToSend = MutableSharedFlow<ISocketWritable>()

        fun sendMessage(message: ISocketWritable) = makeOnBackgroundGlobal { flowMessageToSend.emit(message) }

        fun shutDown() = makeOnBackgroundGlobal {
            flowShutDown.emit(Unit)
        }

        fun sendRawData(
            data: ByteArray,
            messageType: TypeSocketMessage, length: Int = data.size
        ) {
            socket?.tryGetOps()?.let { stream ->
                SocketHelperJava.sendRaw(
                    data,
                    length,
                    messageType,
                    stream
                )
            }
        }
    }
}

private fun makeOnBackgroundGlobal(action: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(
    context = Dispatchers.IO,
    block = action
)

private fun Socket.tryGetOps(): OutputStream? = try {
    getOutputStream()
} catch (e: Exception) {
    e.printStackTrace()
    null
}

private fun Socket.tryGetIps(): InputStream? = try {
    getInputStream()
} catch (e: Exception) {
    e.printStackTrace()
    null
}
