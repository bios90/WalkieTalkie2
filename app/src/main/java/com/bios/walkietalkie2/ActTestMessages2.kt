package com.bios.walkietalkie2

import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.AppClass
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.common.SocketReconnectionHelper
import com.bios.walkietalkie2.databinding.ActTestMessagesBinding
import com.bios.walkietalkie2.models.messages.PingMessage
import com.bios.walkietalkie2.models.messages.sendMessage
import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.MessagePing
import com.bios.walkietalkie2.models.messages2.MessagePong
import com.bios.walkietalkie2.models.messages2.MessageVoiceReceive
import com.bios.walkietalkie2.models.messages2.MessageVoiceSend
import com.bios.walkietalkie2.models.messages2.TypeSocketMessage
import com.bios.walkietalkie2.utils.AudioPlayer
import com.bios.walkietalkie2.utils.AudioTools2
import com.bios.walkietalkie2.utils.AudioUtils.bufferRecordSize
import com.bios.walkietalkie2.utils.SocketHelper
import com.bios.walkietalkie2.utils.Toast
import com.bios.walkietalkie2.utils.getArgs
import com.bios.walkietalkie2.utils.onTouchUpAndDown
import com.xuhao.didi.socket.client.sdk.OkSocket
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientPool
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerShutdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileDescriptor
import java.io.OutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.Executors

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
            onRecreated = {
                socket = it
                startMessageListening()
            }
        )
    }

    private val audioPlayer by lazy { AudioTools2.getAudioPlayer() }
    private val recorder by lazy { requireNotNull(AudioTools2.getRecorder()) }
    private var threadAudioRecord: Thread? = null
    private var threadListening: Thread? = null
    private var threadAudioSending: Thread? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bndActTestMessages.root)
        System.setProperty(IO_PARALLELISM_PROPERTY_NAME, Int.MAX_VALUE.toString())
        socketReconnectionHelper.forceInit()
        initAudioPlayer()
        bndActTestMessages.btnPing.setOnClickListener {
            sendPingMessage()
        }
        var record = false

        bndActTestMessages.btnSpeak.onTouchUpAndDown(
            onDown = {
                record = true
                threadAudioRecord = thread {
                    audioPlayer.pause()
                    recorder.startRecording()
                    val audioBuffer = ByteArray(bufferRecordSize)
                    val fos = socket?.getOutputStream()
                    var readedLength: Int = 0
                    while (record && Thread.currentThread().isInterrupted().not()) {
                        readedLength = recorder.read(audioBuffer, 0, audioBuffer.size)
                        thread {
                            if (readedLength > -1) {
                                SocketHelper.sendRaw2(
                                    bytes = audioBuffer,
                                    length = bufferRecordSize,
                                    TypeSocketMessage.VoiceSend,
                                    fos!!
                                )
                            }
                        }
//                        readedLength = recorder.read(audioBuffer, 0, audioBuffer.size)
                    }
                }
            },
            onUp = {
                record = false
                threadAudioRecord?.interrupt()
                recorder.stop()
                audioPlayer.play()
            }
        )
    }

    private fun startMessageListening() {
        threadListening?.interrupt()
        threadListening = thread {
            val fis = socket?.getInputStream()
            if (fis != null) {
                var readResult: SocketHelper.ReadResult?
                while (Thread.currentThread().isInterrupted().not()) {
                    readResult = try {
                        SocketHelper.readRaw2(fis)
                    } catch (e: Exception) {
                        null
                    }
                    when (readResult?.messageType) {
                        TypeSocketMessage.Ping -> {
                            val ping = MessagePing().apply {
                                readFromBytes(readResult.bytes, readResult!!.messageLength)
                            }
                            runOnUiThread {
                                Toast("Got Ping message ${ping.text}")
                            }
                        }
                        TypeSocketMessage.Pong -> {
                            runOnUiThread {
                                Toast("Got Pong message")
                            }
                        }
                        TypeSocketMessage.VoiceReceive, TypeSocketMessage.VoiceSend -> {
                            runOnUiThread {
                                Toast("Got Audio message")
                            }
                            try {
                                audioPlayer.write(
                                    readResult.bytes, 0, readResult.messageLength ?: 0
                                )
                            } catch (e: Exception) {
                                e
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendPingMessage() {
        lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                val fos = socket?.getOutputStream()
                if (fos != null) {
                    val text = UUID.randomUUID().toString()
                    val asBytes = text.toByteArray()
                    SocketHelper.sendRaw2(
                        bytes = asBytes,
                        length = asBytes.size,
                        typeSocketMessage = TypeSocketMessage.Ping,
                        stream = fos
                    )
//                    SocketHelper.sendMessage(ping, socket!!.getOutputStream())
                }
            }
        )
    }

    private fun sendPongMessage(ping: MessagePing) {
        lifecycleScope.launch(
            context = Dispatchers.IO,
            block = {
                if (socket != null) {
                    val pong = MessagePong(ping.text)
//                    SocketHelper.sendMessage(pong, socket!!.getOutputStream())
                }
            }
        )
    }

    private fun initAudioPlayer() {
        thread {
            audioPlayer.play()
        }
//        lifecycleScope.launch(
//            context = Dispatchers.IO,
//            block = {
//                audioPlayer.play()
//            }
//        )
    }
}

private fun thread(action: () -> Unit) = Thread(action).apply { start() }

//    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//        MediaRecorder(AppClass.getApp())
//    } else {
//        MediaRecorder()
//    }
//
//    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
//    recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
//    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//    recorder.setOutputFile(fileDescriptor)
//    recorder.prepare()
/*
*
*  // Byte array for audio record
ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

ParcelFileDescriptor[] descriptors = ParcelFileDescriptor.createPipe();
ParcelFileDescriptor parcelRead = new ParcelFileDescriptor(descriptors[0]);
ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(descriptors[1]);

InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);

MediaRecorder recorder = new MediaRecorder();
recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
recorder.setOutputFile(parcelWrite.getFileDescriptor());
recorder.prepare();

recorder.start();


int read;
byte[] data = new byte[16384];

while ((read = inputStream.read(data, 0, data.length)) != -1) {
byteArrayOutputStream.write(data, 0, read);
}

byteArrayOutputStream.flush();
* */
