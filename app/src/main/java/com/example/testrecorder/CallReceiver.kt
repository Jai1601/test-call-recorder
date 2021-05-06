package com.example.testrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class CallReceiver : BroadcastReceiver() {
    var flag=true
    override fun onReceive(context: Context, intent: Intent) {
        val phoneListener = MyPhoneStateListener()
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        if (TService.service_running =="" && flag) {
            flag=false
            TService.wasRinging=true
            val serviceIntent = Intent(context, TService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.e("received", "Service Started : CAllReceiver")
            Handler().postDelayed({
                //doSomethingHere()
                flag=true
            }, 3000)
        }
    }
    /*
    * New Comment Added branch3
    * */
    inner class MyPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> Log.d("DEBUG", "CALL_STATE_IDLE : $incomingNumber")
                TelephonyManager.CALL_STATE_OFFHOOK ->                     // CALL_STATE_OFFHOOK;
                    Log.d("DEBUG", "CALL_STATE_OFFHOOK : $incomingNumber")
                TelephonyManager.CALL_STATE_RINGING -> Log.d("DEBUG", "RINGING : $incomingNumber")
                else -> {
                }
            }
        }
    }
}