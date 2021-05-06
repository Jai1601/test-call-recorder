package com.example.testrecorder

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.testrecorder.db.RecordingDatabase
import com.example.testrecorder.db.RecordingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class TService : Service() {
    var recorder: MediaRecorder? = null
    var audiofile: File? = null
    var name: String? = null
    var phonenumber: String? = null
    var audio_format: String? = null
    var Audio_Type: String? = null
    var audioSource = 0
    var context: Context? = null
    private val handler: Handler? = null
    var timer: Timer? = null
    var varoffHook = false
    var ringing = false
    var toast: Toast? = null
    var flagInOut = "in"
    private var recordstarted = false
    private var br_call: CallBr? = null
    lateinit var db : RecordingDatabase

//    private var mCallsManager : CallsManager? = null
    override fun onBind(arg0: Intent): IBinder? {
        // TODO Auto-generated method stub
        return null
    }
/*
* New Comment Added branch3
* post create
* */
    override fun onDestroy() {
        Log.d("service", "destroy")
        service_running=""
        super.onDestroy()
    }
    val CHANNEL_ID = "ForegroundServiceChannel"
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        service_running="running"
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Recorder")
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
        val filter = IntentFilter()
        filter.addAction(ACTION_OUT)
        filter.addAction(ACTION_IN)
        br_call = CallBr()
        this.registerReceiver(br_call, filter)
        db = RecordingDatabase(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.registerReceiver(mMuteChangeReceiver, IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED))
        }


        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private val mMuteChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (AudioManager.ACTION_MICROPHONE_MUTE_CHANGED == intent.action) {
                    var audioManager=context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    if(audioManager.isMicrophoneMute){
                        Toast.makeText(context, "receive Mute", Toast.LENGTH_LONG).show()
                        callRecordStop(context)
                    }else{
                        Toast.makeText(context, "receive Unmute", Toast.LENGTH_LONG).show()
                        callRecordStart(flagInOut,"part")

                    }
                } else {
                    //Log.w(this, "Received non-mute-change intent");
                }
            } finally {
                //Log.endSession();
            }
        }
    }

    inner class CallBr : BroadcastReceiver() {
        var bundle: Bundle? = null
        var state: String? = null
        var lastState: String? = null
        var inCall: String? = null
        var outCall: String? = null
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_IN) {
                if (intent.extras.also { bundle = it } != null) {
                    state = bundle!!.getString(TelephonyManager.EXTRA_STATE)
                    Log.d("Files", "state: $state")
                    if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                        inCall = bundle!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        caller = "$inCall"
                        wasRinging = true
                        //Toast.makeText(context, "IN : $inCall", Toast.LENGTH_LONG).show()
                    } else if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        if (wasRinging == true && !recordstarted) {
//                            Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show()
                            flagInOut = "in"
                            callRecordStart(flagInOut,"new")
                        } else {
                            if (!recordstarted) {
//                                Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show()
                                flagInOut = "out"
                                callRecordStart(flagInOut,"new")
                            }
                        }
                    } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                        wasRinging = false
//                        Toast.makeText(context, "REJECT || DISCO", Toast.LENGTH_LONG).show()

                        callRecordStop(context)
                    }
                    lastState = state
                }
            } else if (intent.action == ACTION_OUT) {
                if (intent.extras.also { bundle = it } != null) {
                    outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                    Toast.makeText(context, "OUT : $outCall", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun callRecordStop(context: Context) {
        if (recordstarted) {
            recorder!!.stop()
            recordstarted = false
            var duration = getDuration(audiofile!!.absolutePath,context)
            var entity = RecordingEntity(0,audiofile!!.name,audiofile!!.absolutePath,"$duration","$in_time", "$caller","$call_type")
            Log.e("dbfiles","${entity.name} ${entity.path} ${entity.duration} ${entity.time} ${entity.caller} ${entity.calltype}")
            in_time=""
            caller=""
            call_type=""
            GlobalScope.launch(Dispatchers.IO) { db.recordingDao().insertRecording(entity) }
        }
    }
    var in_time=""
    var caller=""
    var call_type=""
    fun callRecordStart(type :String, part :String){
        call_type=type
        val out = SimpleDateFormat("dd-MM-yyyy:hh-mm-aa").format(Date())
//        val sampleDir = File(Environment.getExternalStorageDirectory(), "/Recording1")
        in_time= "$out"
        val sampleDir = File(this.filesDir.absolutePath, "/Recording1")
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }

//                            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                            val mobile = tm.line1Number
        val file_name = "Call_${type}_${out}_${part}_"
        try {
            audiofile = File.createTempFile(file_name, ".amr", sampleDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val path = Environment.getExternalStorageDirectory().absolutePath
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)  // for two way communication
//        recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK)  // for caller source only
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder!!.setOutputFile(audiofile!!.absolutePath)
        try {
            recorder!!.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        recorder!!.start()
        recordstarted = true
    }

    fun getDuration(pathStr: String, context: Context): String {
        //val uri: Uri = Uri.parse(pathStr)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(pathStr)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return millisecondsToTime(durationStr.toLong())
    }

    private fun millisecondsToTime(milliseconds: Long): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        val secondsStr = java.lang.Long.toString(seconds)
        val secs: String
        secs = if (secondsStr.length >= 2) {
            secondsStr.substring(0, 2)
        } else {
            "0$secondsStr"
        }
        return "$minutes:$secs"
    }

    companion object {
        var service_running=""
        var wasRinging = false
        private const val ACTION_IN = "android.intent.action.PHONE_STATE"
        private const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
    }
}