package com.bios.walkietalkie2

import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.common.SocketReconnectionHelper
import com.bios.walkietalkie2.databinding.ActTestMessagesBinding
import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.MessageBye
import com.bios.walkietalkie2.models.messages2.MessagePing
import com.bios.walkietalkie2.models.messages2.MessagePong
import com.bios.walkietalkie2.models.messages2.MessageVoice
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage
import com.bios.walkietalkie2.utils.AudioTools2
import com.bios.walkietalkie2.utils.AudioUtils.bufferRecordSize
import com.bios.walkietalkie2.utils.SocketHelper
import com.bios.walkietalkie2.utils.SocketHelperJava
import com.bios.walkietalkie2.utils.Toast
import com.bios.walkietalkie2.utils.getArgs
import com.bios.walkietalkie2.utils.onTouchUpAndDown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import okhttp3.internal.closeQuietly
import java.io.OutputStream
import java.net.Socket

class ActTestMessages2 : BaseActivity() {
    private val bndActTestMessages by lazy {
        ActTestMessagesBinding.inflate(
            layoutInflater,
            null,
            false
        )
    }
    private val args: ActCall.Args by lazy { requireNotNull(getArgs()) }
    private var socket: Socket? = null
    private val socketReconnectionHelper by lazy {
        SocketReconnectionHelper(
            act = this,
            args = args,
            onSocketChecked = {
                if (socket == null && it != null) {
                    socket = it
                    restartMessagingListener()
                }
            }
        )
    }

    private val audioPlayer by lazy { AudioTools2.getAudioPlayer() }
    private val recorder by lazy { requireNotNull(AudioTools2.getRecorder()) }
    private var jobAudioRecord: Job? = null
    private var jobListening: Job? = null
    private var record = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bndActTestMessages.root)
        System.setProperty(IO_PARALLELISM_PROPERTY_NAME, Int.MAX_VALUE.toString())
        socketReconnectionHelper.forceInit()
        initAudioPlayer()
        setListeners()
    }

    override fun onDestroy() {
        if (isChangingConfigurations.not()) {
            GlobalScope.launch(
                context = Dispatchers.IO,
                block = {
                    socket?.getOutputStream()?.let { stream ->
                        SocketHelperJava.sendMessage(MessageBye(), stream)
                    }
                }
            )
            socket?.closeQuietly()
        }
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val action = event?.action
        val keyCode = event?.keyCode
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    startRecord()
                } else {
                    stopRecord()
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private fun setListeners() {
        bndActTestMessages.btnPing.setOnClickListener {
            sendPing()
        }

        with(bndActTestMessages.btnSpeak) {
            onTouchUpAndDown(
                onDown = ::startRecord,
                onUp = ::stopRecord
            )
        }
    }

    private fun startRecord() {
        if (record) {
            return
        }
        record = true
        jobAudioRecord = makeOnBackground {
            audioPlayer.pause()
            recorder.startRecording()
            val audioBuffer = ByteArray(bufferRecordSize)
            val fos = socket?.getOutputStream()
            var readLength: Int = recorder.read(audioBuffer, 0, audioBuffer.size)
            while (record && readLength > -1) {
                fos?.let { sendVoice(audioBuffer, fos, readLength) }
                readLength = recorder.read(audioBuffer, 0, audioBuffer.size)
                yield()
            }
        }
    }

    private fun stopRecord() {
        record = false
        jobAudioRecord?.cancel()
        recorder.stop()
        audioPlayer.play()
    }

    private fun restartMessagingListener() {
        jobListening?.cancel()
        jobListening = makeOnBackground {
            val fis = socket?.getInputStream()
            if (fis != null) {
                while (true) {
                    yield()
                    val message = SocketHelperJava.readMessage(fis)
                    message?.let(::handleMessage)
                }
            }
        }
    }

    private fun initAudioPlayer() {
        makeOnBackground {
            audioPlayer.play()
        }
    }

    private fun handleMessage(msg: ISocketReadable) {
        when (msg) {
            is MessagePing -> sendPong(msg)
            is MessagePong -> makeOnUi { Toast("Got pong message ${msg.text}") }
            is MessageVoice -> playVoice(msg)
            is MessageBye -> makeOnUi {
                if (isFinishing.not()) {
                    socket?.closeQuietly()
                    finish()
                }
            }
        }
    }

    private fun sendPong(ping: MessagePing) = socket?.getOutputStream()?.let { stream ->
        val message = MessagePong(ping.text)
        SocketHelperJava.sendMessage(message, stream)
    }

    private fun sendPing() = socket?.getOutputStream()?.let { stream ->
        makeOnBackground {
            val message = MessagePing()
            SocketHelperJava.sendMessage(message, stream)
        }
    }

    fun sendVoice(data: ByteArray, stream: OutputStream, length: Int) {
        SocketHelperJava.sendRaw(
            data,
            length,
            TypeSocketMessage.Voice,
            stream
        )
    }

    private fun playVoice(voice: MessageVoice) {
        try {
            audioPlayer.write(voice.getBytes(), 0, voice.getLength())
            voice.clear()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    private fun makeOnBackground(action: suspend CoroutineScope.() -> Unit): Job = lifecycleScope.launch(
        context = Dispatchers.IO,
        block = action
    )

    private fun makeOnUi(action: CoroutineScope.() -> Unit): Job = lifecycleScope.launch(
        context = Dispatchers.Main,
        block = action
    )
}
