package com.bios.walkietalkie2.common

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.ActCall
import com.bios.walkietalkie2.utils.getArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Socket

class SocketService : Service() {
    private val channelId = "123"
    private var args: ActCall.Args? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(p0: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        args = intent?.getArgs()
        val notify = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Some ttitle!!")
            .setContentText("Eto text")
            .build()
        startForeground(213, notify)
        return START_NOT_STICKY
    }

    private fun startSocketPingPong() {
        scope.launch(
            context = Dispatchers.IO,
            block = {
                while (true) {

                }
            }
        )
    }

}