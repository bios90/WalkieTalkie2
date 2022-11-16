package com.bios.walkietalkie2.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack

object AudioUtils {
    val SAMPLE_RATE = 8000
    val bufferRecordSize
        get() = getMinimumBufferSize()

    private fun getMinimumBufferSize(): Int {
        val size = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        return if (size == AudioTrack.ERROR || size == AudioTrack.ERROR_BAD_VALUE) {
            SAMPLE_RATE * 2
        } else {
            size
        }
    }
}

/*
* if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }
* */
