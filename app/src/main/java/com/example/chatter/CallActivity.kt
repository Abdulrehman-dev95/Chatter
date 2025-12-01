package com.example.chatter

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Create a container for the fragment programmatically
        val layout = FrameLayout(this)
        layout.id = android.view.View.generateViewId()
        setContentView(layout)

        // 2. Get data from Intent
        val userID = intent.getStringExtra("userID") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val callID = intent.getStringExtra("callID") ?: ""
        val appID = intent.getLongExtra("appID", 0L)
        val appSign = intent.getStringExtra("appSign") ?: ""
        val isVideo = intent.getBooleanExtra("isVideo", true)

        // 3. Generate Config
        val config = if (isVideo) {
            ZegoUIKitPrebuiltCallConfig.groupVideoCall()
        } else {
            ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
        }

        // 4. Create Fragment
        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, userID, userName, callID, config
        )

        // 5. Add Fragment
        supportFragmentManager.beginTransaction()
            .replace(layout.id, fragment)
            .commitNow()

//        // 6. Handle Hangup
//        fragment.setLeaveCallListener(object : ZegoLeaveCallListener {
//            override fun onLeaveCall() {
//                finish() // Close the activity when call ends
//            }
//        })
    }
}