package com.bios.walkietalkie2

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.KeyEvent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.databinding.ActTestMessagesBinding
import com.bios.walkietalkie2.models.messages2.ISocketReadable
import com.bios.walkietalkie2.models.messages2.MessageBye
import com.bios.walkietalkie2.models.messages2.MessagePing
import com.bios.walkietalkie2.models.messages2.MessagePong
import com.bios.walkietalkie2.models.messages2.MessageVoice
import com.bios.walkietalkie2.utils.AudioTools2
import com.bios.walkietalkie2.utils.AudioUtils.bufferRecordSize
import com.bios.walkietalkie2.utils.CompassSensorEventListener
import com.bios.walkietalkie2.utils.YaCupLocationManager
import com.bios.walkietalkie2.utils.PermissionsManager
import com.bios.walkietalkie2.utils.SocketListenerService
import com.bios.walkietalkie2.utils.Toast
import com.bios.walkietalkie2.utils.getArgs
import com.bios.walkietalkie2.utils.onTouchUpAndDown
import com.bios.walkietalkie2.utils.purArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class ActTestMessages2 : BaseActivity() {
    private val bndActTestMessages by lazy {
        ActTestMessagesBinding.inflate(
            layoutInflater,
            null,
            false
        )
    }
    private val args: ActCall.Args by lazy { requireNotNull(getArgs()) }

    private val audioPlayer by lazy { AudioTools2.getAudioPlayer() }
    private val recorder by lazy { requireNotNull(AudioTools2.getRecorder()) }
    private var jobAudioRecord: Job? = null
    private var record = false
    private val permissionsManager by lazy { PermissionsManager(this) }
    private val locationManager by lazy {
        YaCupLocationManager(
            activity = this,
            permissionsManager = permissionsManager
        )
    }
    private val compassRotatior by lazy {
        CompassSensorEventListener(
            activity = this,
            locationFrom = {
                Location("").apply {
                    latitude = 55.761597
                    longitude = 37.567981
                }
            },
            locationTo = {
                Location("").apply {
                    latitude = 55.762253
                    longitude = 37.564020
                }
            },
            onRotationChanged = ::rotateImage
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(bndActTestMessages.root)
        System.setProperty(IO_PARALLELISM_PROPERTY_NAME, Int.MAX_VALUE.toString())
        startService(
            Intent(this, SocketListenerService::class.java)
                .apply { purArgs(args) }
        )
        initAudioPlayer()
        setListeners()
        setEvents()
    }

    override fun onDestroy() {
        if (isChangingConfigurations.not()) {
            SocketListenerService.shutDown()
        }
        super.onDestroy()
    }

    private fun setEvents() {
        SocketListenerService
            .flowMessagesReceived
            .onEach(::handleMessage)
            .flowOn(Dispatchers.IO)
            .launchIn(lifecycleScope)

        locationManager.flowLocation
            .onEach(::handleLocationChanged)
            .flowOn(Dispatchers.IO)
            .launchIn(lifecycleScope)
    }

    private fun rotateImage(rotation: Float) {
        val anim = RotateAnimation(
            bndActTestMessages.imgArrow.rotation,
            rotation,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            interpolator = LinearInterpolator()
        }
        bndActTestMessages.imgArrow.startAnimation(anim)
        /*
        * RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
rotate.setDuration(5000);
rotate.setInterpolator(new LinearInterpolator());

ImageView image= (ImageView) findViewById(R.id.imageView);

image.startAnimation(rotate);
        * */

        bndActTestMessages.imgArrow.rotation = rotation
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
        compassRotatior.hashCode()
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
            var readLength: Int = recorder.read(audioBuffer, 0, audioBuffer.size)
            var voiceMessage: MessageVoice? = null
            while (record && readLength > -1) {
                voiceMessage = MessageVoice(data = audioBuffer)
                SocketListenerService.sendMessage(voiceMessage)
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
                    finish()
                }
            }
        }
    }

    private fun handleLocationChanged(location: Location) {
        makeOnUi {
            Toast(
                text = "Got location ${location.latitude} ||| ${location.longitude}"
            )
        }
    }

    private fun sendPong(ping: MessagePing) = MessagePong(ping.text)
        .apply(SocketListenerService::sendMessage)

    private fun sendPing() = MessagePing()
        .apply(SocketListenerService::sendMessage)

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
