package com.bios.walkietalkie2

import android.media.AudioTrack
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.common.SocketChannelReconnectionHelper
import com.bios.walkietalkie2.databinding.ActTestMessagesBinding
import com.bios.walkietalkie2.models.messages.BaseMessage
import com.bios.walkietalkie2.models.messages.PingMessage
import com.bios.walkietalkie2.models.messages.PongMessage
import com.bios.walkietalkie2.models.messages.VoiceMessage
import com.bios.walkietalkie2.models.messages.readMessage
import com.bios.walkietalkie2.models.messages.sendMessage
import com.bios.walkietalkie2.utils.AudioTools2
import com.bios.walkietalkie2.utils.AudioUtils.bufferRecordSize
import com.bios.walkietalkie2.utils.Toast
import com.bios.walkietalkie2.utils.getArgs
import com.bios.walkietalkie2.utils.onTouchUpAndDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class ActTestMessages : BaseActivity() {

    private val bndActTestMessages by lazy {
        ActTestMessagesBinding.inflate(
            layoutInflater,
            null,
            false
        )
    }
    private val args: ActCall.Args by lazy { requireNotNull(getArgs()) }
    private var socketChannel: SocketChannel? = null
    private val socketReconnectionHelper by lazy {
        SocketChannelReconnectionHelper(
            act = this,
            args = args,
            onRecreated = {
                socketChannel = it
                startMessageListening()
            }
        )
    }
    private val audioPlayer by lazy { AudioTools2.getAudioPlayer() }
    private val recorder by lazy { requireNotNull(AudioTools2.getRecorder()) }
    private var jobAudioRecord: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bndActTestMessages.root)
        socketReconnectionHelper.forceInit()
        initAudioPlayer()
        bndActTestMessages.btnPing.setOnClickListener {
            sendPingMessage()
        }

        var record: Boolean = false
        bndActTestMessages.btnSpeak.onTouchUpAndDown(
            onDown = {
                record = true
                jobAudioRecord = lifecycleScope.launch(
                    context = Dispatchers.IO,
                    block = {
                        recorder.startRecording()
                        audioPlayer.pause()
                        val audioBuffer = ByteArray(bufferRecordSize)
                        var msg: VoiceMessage? = null
                        while (record) {
                            yield()
                            recorder.read(audioBuffer, 0, audioBuffer.size)
                            msg = VoiceMessage().apply { bytes = audioBuffer }
                            socketChannel?.let {
                                sendMessage(msg, it)
                            }
                            msg.bytes = null
//                            sendVoiceMessage(msg)
                        }
                    }
                )
            },
            onUp = {
                record = false
                recorder.stop()
                audioPlayer.play()
                jobAudioRecord?.cancel()
            }
        )
    }

    private fun startMessageListening() {
        lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                val byteBuffer: ByteBuffer = ByteBuffer.allocate(bufferRecordSize)
                var message: BaseMessage?
                while (true) {
                    yield()
                    if (socketChannel != null && socketChannel?.isOpen == true) {
                        message = readMessage(socketChannel!!, byteBuffer)
                        when (message) {
                            is PingMessage -> {
                                withContext(Dispatchers.Main) {
                                    Toast("Got ping with message\n${message.text} ")
                                }
                                sendPongMessage(message)
                            }
                            is PongMessage -> {
                                withContext(Dispatchers.Main) {
                                    Toast("Got pong with message\n${message.text} ")
                                }
                            }
                            is VoiceMessage -> {
                                withContext(Dispatchers.Main) {
                                    Toast("Got audio message")
                                }
                                message.receivedBuffer?.let {
                                    val length = it.int
                                    audioPlayer.write(it, length, AudioTrack.WRITE_BLOCKING)
                                    message.bytes = null
                                    message.receivedBuffer = null
                                }
//                                playAudioMessage(message)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun sendPongMessage(pingMessage: PingMessage) {
        lifecycleScope.launch(context = Dispatchers.IO,
            block = {
                if (socketChannel != null) {
                    val pong = PongMessage()
                        .apply { text = pingMessage.text }
                    sendMessage(pong, socketChannel!!)
                }
            }
        )
    }

    private fun sendPingMessage() {
        lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                if (socketChannel != null) {
                    val ping = PingMessage()
                    sendMessage(ping, socketChannel!!)
                    withContext(Dispatchers.Main) {
                        Toast("Send ping with message\n${ping.text}")
                    }
                }
            }
        )
    }

//    private fun sendVoiceMessage(msg: VoiceMessage) {
//        lifecycleScope.launch(
//            context = Dispatchers.IO,
//            block = {
//                socketChannel?.let {
//                    sendMessage(msg, it)
//                }
//            }
//        )
//    }

    private fun initAudioPlayer() {
        lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                audioPlayer.play()
            }
        )
    }

//    private fun playAudioMessage(msg: VoiceMessage) {
//        lifecycleScope.launch(
//            context = Dispatchers.IO,
//            block = {
//                withContext(Dispatchers.Main) {
//                    Toast("Got audio message")
//                }
//                msg.bytes?.let {
//                    audioPlayer.write(it, 0, it.size)
//                    msg.bytes = null
//                }
//            }
//        )
//    }
}
