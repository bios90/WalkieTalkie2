package com.bios.walkietalkie2.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity

fun createWifiDirectReceiver(
    act: AppCompatActivity,
    onP2pStateChanged: (Boolean) -> Unit,
    onPeersChanged: () -> Unit,
    onConnectionChanged: (WifiP2pInfo?) -> Unit,
) {
    val intentFilter = IntentFilter()
        .apply {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.let(::addAction)
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.let(::addAction)
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.let(::addAction)
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.let(::addAction)
        }
    val receiver = WiFiDirectBroadcastReceiver(
        onP2pStateChanged = onP2pStateChanged,
        onPeersChanged = onPeersChanged,
        onConnectionChanged = onConnectionChanged
    )
    act.addLifeCycleObserver(
        onResume = {
            act.registerReceiver(receiver, intentFilter)
        },
        onPause = {
            act.unregisterReceiver(receiver)
        }
    )
}

class WiFiDirectBroadcastReceiver(
    private val onP2pStateChanged: (Boolean) -> Unit,
    private val onPeersChanged: () -> Unit,
    private val onConnectionChanged: (WifiP2pInfo?) -> Unit,
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val p2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                    WifiP2pManager.WIFI_P2P_STATE_DISABLED)
                val isEnabled = p2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                onP2pStateChanged.invoke(isEnabled)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> onPeersChanged.invoke()
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val wifiP2pInfo =
                    intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                onConnectionChanged.invoke(wifiP2pInfo)
            }
        }
    }
}