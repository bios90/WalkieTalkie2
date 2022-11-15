package com.bios.walkietalkie2.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.net.Socket

class AudioPlayer(private val scope: CoroutineScope) {
    private var audioTrack: AudioTrack? = null
    private var audioPlayingJob: Job? = null
    private var audioStreamReadingJob: Job? = null

    fun startPlayFromSocket(socket: Socket) {
        audioPlayingJob = scope.launch(
            context = Dispatchers.IO,
            block = {
                val bufferSize = AudioUtils.bufferRecordSize
                audioTrack = AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(AudioUtils.bufferRecordSize)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                ).apply { play() }

                val buffer = ByteArray(bufferSize)
                audioStreamReadingJob = scope.launch(
                    context = Dispatchers.IO,
                    block = {
                        try {
                            val inps = socket.getInputStream()
                            var bytesToRead = inps.read(buffer, 0, bufferSize)
                            while (bytesToRead != -1) {
                                yield()
                                audioTrack?.write(buffer, 0, buffer.size)
                                bytesToRead = inps.read(buffer, 0, bufferSize)
                            }
                            inps.close()
                        } catch (e: Exception) {

                        }
                    }
                )
            }
        )
    }

    fun stopPlayingFromSocket() {
        audioTrack?.stop()
        audioTrack?.release()
        audioPlayingJob?.cancel()
        audioStreamReadingJob?.cancel()
    }
}
