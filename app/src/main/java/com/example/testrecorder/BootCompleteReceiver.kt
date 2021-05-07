package com.example.testrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.testrecorder.TService

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        /*val serviceIntent = Intent(context, TService::class.java)
        context.startService(serviceIntent)*/
        val serviceIntent = Intent(context, TService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}