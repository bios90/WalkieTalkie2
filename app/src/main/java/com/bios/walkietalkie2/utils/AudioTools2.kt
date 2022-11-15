package com.bios.walkietalkie2.utils

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder

object AudioTools2 {

    @SuppressLint("MissingPermission")
    fun getRecorder(): AudioRecord? {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            AudioUtils.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioUtils.bufferRecordSize
        )

        return audioRecord.takeIf { audioRecord.state == AudioRecord.STATE_INITIALIZED }

        /*
        *    AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);
        * */
    }

    fun getAudioPlayer(): AudioTrack {
        val bufferSize = AudioTrack.getMinBufferSize(
            AudioUtils.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).takeIf { it != AudioTrack.ERROR && it != AudioTrack.ERROR_BAD_VALUE }
            ?: AudioUtils.SAMPLE_RATE * 2

        return AudioTrack(
            AudioManager.STREAM_MUSIC,
            AudioUtils.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM

        )
        /*
        *
        *               int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                Log.d("PLAY", "buffersize = "+bufferSize);

                 audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();

        * */
    }

//    fun getAudioPlayer() = AudioTrack(
//        AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//            .build(),
//        AudioFormat.Builder()
//            .setSampleRate(bufferRecordSize)
//            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
//        bufferRecordSize,
//        AudioTrack.MODE_STREAM,
//        AudioManager.AUDIO_SESSION_ID_GENERATE
//
//    )

    /**/
}
