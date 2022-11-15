package com.bios.walkietalkie2.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.yield
import java.net.Socket
import kotlin.Exception

class MicRecorder(private val scope: CoroutineScope) {

    private var audioRecordJob: Job? = null
    private var audioStreamingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun startRecordingToSocket(
        socket: Socket,
        onError: () -> Unit,
    ) {
        audioRecordJob = scope.launch(
            context = Dispatchers.IO,
            block = {
                runInterruptible {
                    try {
                        val bufferSize = AudioUtils.bufferRecordSize
                        audioRecord = AudioRecord(
                            MediaRecorder.AudioSource.VOICE_RECOGNITION,
                            AudioUtils.bufferRecordSize,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize
                        )

                        if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                            onError.invoke()
                            return@runInterruptible
                        }
                        audioRecord?.let {
                            it.startRecording()
                            audioStreamingJob = scope.launch(
                                context = Dispatchers.IO,
                                block = {
                                    try {
                                        val audioBuffer = ByteArray(bufferSize)
                                        val fos = socket.getOutputStream()
                                        while (true) {
                                            yield()

                                            audioRecord?.read(audioBuffer, 0, audioBuffer.size)
                                            fos.write(audioBuffer)
                                            fos.flush()
                                        }
                                    } catch (e: Exception) {
                                        onError.invoke()
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {

                    }

                }
            }
        )
    }

    fun stopRecording() {
        audioStreamingJob?.cancel()
        audioRecordJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
