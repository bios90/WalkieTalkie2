package com.bios.walkietalkie2.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack

object AudioUtils {
    const val SAMPLE_RATE = 4086
    val bufferSize: Int = SAMPLE_RATE
//        get() = run {
//            val size = AudioRecord.getMinBufferSize(
//                SAMPLE_RATE,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT)
//            if (size == AudioTrack.ERROR || size == AudioTrack.ERROR_BAD_VALUE) {
//                SAMPLE_RATE * 2
//            } else {
//                size
//            }
//        }
}

/*
* if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }
* */