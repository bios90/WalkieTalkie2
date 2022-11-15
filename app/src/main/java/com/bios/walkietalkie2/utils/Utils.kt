package com.bios.walkietalkie2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.AppClass
import com.bios.walkietalkie2.common.Consts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.nio.ByteBuffer

fun AppCompatActivity.addLifeCycleObserver(
    onCreate: (LifecycleOwner?) -> Unit = { },
    onStart: (LifecycleOwner?) -> Unit = { },
    onResume: (LifecycleOwner?) -> Unit = { },
    onPause: (LifecycleOwner?) -> Unit = { },
    onStop: (LifecycleOwner?) -> Unit = { },
    onDestroy: (LifecycleOwner?) -> Unit = { },
) = lifecycle.addObserver(
    object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) = onCreate.invoke(owner)
        override fun onStart(owner: LifecycleOwner) = onStart.invoke(owner)
        override fun onResume(owner: LifecycleOwner) = onResume.invoke(owner)
        override fun onPause(owner: LifecycleOwner) = onPause.invoke(owner)
        override fun onStop(owner: LifecycleOwner) = onStop.invoke(owner)
        override fun onDestroy(owner: LifecycleOwner) = onDestroy.invoke(owner)
    }
)

fun Boolean.toVisibility(): Int {
    if (this) {
        return View.VISIBLE
    } else {
        return View.GONE
    }
}

fun AppCompatActivity.tryTurnWifiOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply(::startActivity)
    } else {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true
    }
}

fun AppCompatActivity.makeActionDelayed(delayTime: Long, action: () -> Unit) {
    lifecycleScope.launch {
        delay(delayTime)
        action.invoke()
    }
}

@SuppressLint("MissingPermission")
fun WifiP2pManager.connectToAddress(
    channel: WifiP2pManager.Channel,
    address: String,
    onError: () -> Unit = {},
    onSuccess: () -> Unit,
) {
    val config = WifiP2pConfig().apply {
        deviceAddress = address
    }
    connect(
        channel,
        config,
        object : WifiP2pManager.ActionListener {
            override fun onSuccess() = onSuccess.invoke()
            override fun onFailure(p0: Int) = onError.invoke()
        }
    )
}

fun Toast(text: String) {
    android.widget.Toast.makeText(AppClass.getApp(), text, android.widget.Toast.LENGTH_LONG).show()
}

fun Intent.purArgs(args: Serializable) {
    putExtra(Consts.ARGS, args)
}

fun <T : Serializable> Intent.getArgs(): T? {
    return getSerializableExtra(Consts.ARGS) as? T
}

fun <T : Serializable> AppCompatActivity.getArgs(): T? = intent?.getArgs()

fun View.onTouchUpAndDown(
    onDown: () -> Unit,
    onUp: () -> Unit,
) {
    this.setOnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            onDown.invoke()
            true
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            onUp.invoke()
            true
        }
        false
    }
}

inline fun <reified T : Enum<*>> enumValueOrNull(name: String): T? =
    T::class.java.enumConstants.firstOrNull { it.name == name }

fun ByteBuffer.toByteArray() = ByteArray(this.remaining()).also(this::get)

fun bytesToInt(bytes: ByteArray, offset: Int = 0): Int {
    return (bytes[offset + 3].toInt() shl 24) or
            (bytes[offset + 2].toInt() and 0xff shl 16) or
            (bytes[offset + 1].toInt() and 0xff shl 8) or
            (bytes[offset + 0].toInt() and 0xff)
}

fun write4BytesToBuffer(buffer: ByteArray, offset: Int, data: Int) {
    buffer[offset + 0] = (data shr 0).toByte()
    buffer[offset + 1] = (data shr 8).toByte()
    buffer[offset + 2] = (data shr 16).toByte()
    buffer[offset + 3] = (data shr 24).toByte()
}
