package com.bios.walkietalkie2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.common.CompositeAdapter
import com.bios.walkietalkie2.common.adapterDelegateDevice
import com.bios.walkietalkie2.databinding.ActPeersListBinding
import com.bios.walkietalkie2.models.ModelDevice
import com.bios.walkietalkie2.utils.*
import java.net.InetAddress

class ActPeersList : BaseActivity() {

    private val bndActPeersList by lazy { ActPeersListBinding.inflate(layoutInflater, null, false) }
    private val wifiManager by lazy { getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager }
    private val connectionChannel by lazy { wifiManager.initialize(this, this.mainLooper, null) }
    private var foundDevices: List<ModelDevice> = emptyList()
        set(value) {
            field = value
            adapter.setItems(value)
        }
    private val adapter = CompositeAdapter.Builder()
        .add(
            adapterDelegateDevice(
                onClick = ::onDeviceClicked
            )
        )
        .build()
    private var deviceToConnect: ModelDevice? = null
    private var currentCallAddress: InetAddress? = null
    private var currentWifiInfo: WifiP2pInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bndActPeersList.root)
        setupViews()
        initWifiConnections()
    }

    private fun setupViews() {
        bndActPeersList.rvDevices.layoutManager = LinearLayoutManager(this)
        bndActPeersList.rvDevices.adapter = adapter

        bndActPeersList.btnTurnWifi.setOnClickListener {
            tryTurnWifiOn()
//            bndActPeersList.tvConnectionError.visibility = View.GONE
//            bndActPeersList.btnRepeat.visibility = View.GONE
//            makeActionDelayed(
//                delayTime = 5000,
//                action = {
//                    startSignaling()
//                }
//            )
        }
        bndActPeersList.btnRepeat.setOnClickListener {
            startSignaling()
        }
    }

    private fun initWifiConnections() {
        startSignaling()
        createWifiDirectReceiver(
            act = this,
            onP2pStateChanged = ::handleWifiEnabledChange,
            onPeersChanged = ::handlePeersChanged,
            onConnectionChanged = ::handleConnectionChanged
        )
    }

    @SuppressLint("MissingPermission")
    private fun startSignaling() {
        wifiManager.discoverPeers(connectionChannel,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    bndActPeersList.tvConnectionError.visibility = View.GONE
                    bndActPeersList.btnRepeat.visibility = View.GONE
                }

                override fun onFailure(p0: Int) {
                    bndActPeersList.tvConnectionError.visibility = View.VISIBLE
                    bndActPeersList.btnRepeat.visibility = View.VISIBLE
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun handlePeersChanged() {
        if (deviceToConnect != null) {
            sendChangedConnectivity()
            deviceToConnect = null
            return
        }
        wifiManager.requestPeers(connectionChannel) {
            foundDevices = it.deviceList.map {
                it.isGroupOwner
                ModelDevice(
                    macAddress = it.deviceAddress,
                    name = it.deviceName
                )
            }
        }
    }

    private fun handleWifiEnabledChange(isEnabled: Boolean) {
        bndActPeersList.btnTurnWifi.visibility = isEnabled.not().toVisibility()
        if (isEnabled) {
            startSignaling()
        }
    }

    private fun handleConnectionChanged(wifiInfo: WifiP2pInfo?) {
        wifiInfo?.let { currentWifiInfo = it }
        sendChangedConnectivity()
    }

    private fun sendChangedConnectivity() {
        val wifiInfo = currentWifiInfo
        val isConnected = wifiInfo?.groupOwnerAddress != null
        if (isConnected && currentCallAddress == null) {
            currentCallAddress = wifiInfo!!.groupOwnerAddress
            val args = ActCall.Args(
                isGroupOwner = wifiInfo.isGroupOwner,
                groupOwnerAddress = wifiInfo.groupOwnerAddress,
                deviceToConnect = deviceToConnect
            )
            toActCall(args)
        }
    }

    private fun onDeviceClicked(
        device: ModelDevice,
    ) {
        wifiManager.connectToAddress(
            channel = connectionChannel,
            address = device.macAddress,
            onSuccess = {
                deviceToConnect = device
                sendChangedConnectivity()
                wifiManager.requestConnectionInfo(connectionChannel) { wifiInfo ->
                    currentWifiInfo = wifiInfo
                }
            },
            onError = {
                Log.e("ActPeersList", "failed to connect to $device")
            }
        )
    }

    private fun toActCall(args: ActCall.Args) = Intent(this, ActTestMessages::class.java)
        .apply { purArgs(args) }
        .apply(::startActivity)
}