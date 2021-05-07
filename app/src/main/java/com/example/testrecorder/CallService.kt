package com.example.testrecorder

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.telephony.TelephonyManager
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallService : Service() {
    private var recorder: MediaRecorder? = null
    private var recordStarted = false
    private var savedNumber: String? = null
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var isIncoming = false
    override fun onBind(arg0: Intent): IBinder? {
        // TODO Auto-generated method stub
        return null
    }

    override fun onDestroy() {
        // ....
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val filter = IntentFilter()
        filter.addAction(ACTION_OUT)
        filter.addAction(ACTION_IN)
        this.registerReceiver(CallReceiver(), filter)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopRecording() {
        if (recordStarted) {
            recorder!!.stop()
            recordStarted = false
        }
    }

    abstract inner class PhoneCallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_OUT) {
                savedNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
            } else {
                val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                savedNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                var state = 0
                if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                    state = TelephonyManager.CALL_STATE_IDLE
                } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK
                } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                    state = TelephonyManager.CALL_STATE_RINGING
                }
                onCallStateChanged(context, state, savedNumber)
            }
        }

        protected abstract fun onIncomingCallReceived(ctx: Context?, number: String?)
        protected abstract fun onIncomingCallAnswered(ctx: Context?, number: String?)
        protected abstract fun onIncomingCallEnded(ctx: Context?, number: String?)
        protected abstract fun onOutgoingCallStarted(ctx: Context?, number: String?)
        protected abstract fun onOutgoingCallEnded(ctx: Context?, number: String?)
        protected abstract fun onMissedCall(ctx: Context?, number: String?)
        fun onCallStateChanged(context: Context?, state: Int, number: String?) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val time = dateFormat.format(Date())
            val sampleDir = File(Environment.getExternalStorageDirectory(), "/callrecorder")
            if (!sampleDir.exists()) {
                sampleDir.mkdirs()
            }
            if (lastState == state) {
                return
            }
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    isIncoming = true
                    savedNumber = number
                    onIncomingCallReceived(context, number)
                    recorder = MediaRecorder()
                    recorder!!.setAudioSamplingRate(8000)
                    /*recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    //                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder!!.setOutputFile("""${sampleDir.absolutePath}/Incoming 
$number  
$time  
 Call.amr""")
                    try {
                        recorder!!.prepare()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    recorder!!.start()
                    recordStarted = true
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    recorder = MediaRecorder()
                    recorder!!.setAudioSamplingRate(8000)
                    /*recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    //                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                    recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder!!.setOutputFile("""${sampleDir.absolutePath}/Outgoing 
$savedNumber  
$time  
 Call.amr""")
                    try {
                        recorder!!.prepare()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    recorder!!.start()
                    recordStarted = true
                    onOutgoingCallStarted(context, savedNumber)
                } else {
                    isIncoming = true
                    onIncomingCallAnswered(context, savedNumber)
                }
                TelephonyManager.CALL_STATE_IDLE -> if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, savedNumber)
                } else if (isIncoming) {
                    stopRecording()
                    onIncomingCallEnded(context, savedNumber)
                } else {
                    stopRecording()
                    onOutgoingCallEnded(context, savedNumber)
                }
            }
            lastState = state
        }
    }

    inner class CallReceiver : PhoneCallReceiver() {
        override fun onIncomingCallReceived(ctx: Context?, number: String?) {}
        override fun onIncomingCallAnswered(ctx: Context?, number: String?) {}
        override fun onIncomingCallEnded(ctx: Context?, number: String?) {}
        override fun onOutgoingCallStarted(ctx: Context?, number: String?) {}
        override fun onOutgoingCallEnded(ctx: Context?, number: String?) {}
        override fun onMissedCall(ctx: Context?, number: String?) {}
    }

    companion object {
        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
    }
}