package com.bios.walkietalkie2

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bios.walkietalkie2.common.BaseActivity
import com.bios.walkietalkie2.databinding.ActWelcomeBinding
import com.bios.walkietalkie2.utils.AudioUtils
import com.bios.walkietalkie2.utils.PermissionsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActWelcome : BaseActivity() {

    private val permissionsManager by lazy { PermissionsManager(this) }
    private val bndActWelcome by lazy { ActWelcomeBinding.inflate(layoutInflater, null, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bndActWelcome.root)
        permissionsManager.forceInit()
        setListeners()
        checkInitialPermissions()
    }

    private fun setListeners() {
        bndActWelcome.btnAdd.setOnClickListener {
            permissionsManager.checkPermissions(
                onGranted = {
                    toActPeersList()
                },
                onDenied = {}
            )
        }
    }

    private fun checkInitialPermissions() {
        if (permissionsManager.arePermissionsGranted()) {
            toActPeersList()
        } else {
            bndActWelcome.viewOverlay.visibility = View.GONE
        }
    }

    private fun toActPeersList() =
        Intent(this, ActPeersList::class.java)
            .let(::startActivity)
            .also { finish() }
}
