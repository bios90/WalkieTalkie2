package com.bios.walkietalkie2.utils

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.bios.walkietalkie2.utils.AudioUtils.bufferSize

object AudioTools2 {

    @SuppressLint("MissingPermission")
    fun getRecorder(): AudioRecord? {
        val bufferSize = AudioUtils.bufferSize
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            AudioUtils.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        return audioRecord.takeIf { audioRecord.state == AudioRecord.STATE_INITIALIZED }
    }

    fun getAudioPlayer() = AudioTrack(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build(),
        AudioFormat.Builder()
            .setSampleRate(AudioUtils.SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
        bufferSize,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )
}