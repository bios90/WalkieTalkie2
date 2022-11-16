package com.bios.walkietalkie2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.SocketHelper
import com.bios.walkietalkie2.common.SocketReconnectionHelper
import com.bios.walkietalkie2.databinding.ActCallBinding
import com.bios.walkietalkie2.models.ModelDevice
import com.bios.walkietalkie2.utils.AudioPlayer
import com.bios.walkietalkie2.utils.MicRecorder
import com.bios.walkietalkie2.utils.Toast
import com.bios.walkietalkie2.utils.getArgs
import com.bios.walkietalkie2.utils.onTouchUpAndDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.net.InetAddress
import java.net.Socket

class ActCall : AppCompatActivity() {
    data class Args(
        val isGroupOwner: Boolean,
        val groupOwnerAddress: InetAddress,
        val deviceToConnect: ModelDevice?,
    ) : Serializable

    private val bndActCall by lazy { ActCallBinding.inflate(layoutInflater, null, false) }
    private val args: Args by lazy { requireNotNull(getArgs()) }
    private val socketReconnectionHelper by lazy {
        SocketReconnectionHelper(
            act = this,
            args = args,
            onSocketChecked = {
                socket = it
                socket?.let {
                    audioPlayer.stopPlayingFromSocket()
                    audioPlayer.startPlayFromSocket(it)
                }
            }
        )
    }

    private var socket: Socket? = null
    private val micRecorder by lazy { MicRecorder(lifecycleScope) }
    private val audioPlayer by lazy { AudioPlayer(lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socketReconnectionHelper.forceInit()
        setContentView(bndActCall.root)
        setListeners()
    }

    private fun setListeners() {

        bndActCall.btnSpeak.onTouchUpAndDown(
            onDown = {
                val socket = socket ?: return@onTouchUpAndDown
                micRecorder.startRecordingToSocket(
                    socket,
                    onError = {
                    }
                )
            },
            onUp = {
                micRecorder.stopRecording()
            }
        )
    }


}
