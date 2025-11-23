package com.example.josh.recorder

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File
import java.io.IOException
import java.util.UUID

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFile: File? = null

    fun startRecording(): String? {
        val fileName = "audio_${UUID.randomUUID()}.mp4"
        val out = File(context.filesDir, fileName)
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(out.absolutePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
        currentFile = out
        return out.absolutePath
    }

    fun stopRecording(): String? {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        return currentFile?.absolutePath
    }

    fun play(path: String, onCompletion: (() -> Unit)? = null) {
        stopPlayback()
        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
            setOnCompletionListener {
                onCompletion?.invoke()
            }
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) {
            // ignore
        }
        player = null
    }
}
