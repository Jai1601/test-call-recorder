package com.example.testrecorder

//import com.example.testrecorder.db.RecordingDaoService
//import com.example.testrecorder.db.RecordingDaoServiceImpl
import android.Manifest
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrecorder.TService
import com.example.testrecorder.db.RecordingDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {
    private var mDPM: DevicePolicyManager? = null
    private var mAdminName: ComponentName? = null
    private var adapter: LogsAdapter? = null
    private val filesarray = ArrayList<File>()
    lateinit var recyclerView: RecyclerView
    var mPhoneNumber=""
//    @Inject
//    lateinit var recordingDao: RecordingDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.logs)
    val db = RecordingDatabase(this)

        try {
            // Initiate DevicePolicyManager.
            mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mAdminName = ComponentName(this, DeviceAdminSampleReceiver::class.java)
            val txtservice = findViewById<TextView>(R.id.txtservice)
            val txtadmin = findViewById<TextView>(R.id.txtadmin)
         if (!isActiveAdmin) {
                // Launch the activity to have the user enable our admin.
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.add_admin_extra_app_text))
                startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
            }
            txtadmin.setOnClickListener {
                mDPM!!.removeActiveAdmin(mAdminName!!)
                Toast.makeText(this@MainActivity, "Admin Disabled", Toast.LENGTH_SHORT).show()
            }
            txtservice.setOnClickListener {
                stopService()
                Toast.makeText(this@MainActivity, "Service Stopped", Toast.LENGTH_SHORT).show()
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            setAdapter()
            /*if (TService.service_running =="") {
                Log.e("received", "service started")
                startService()
            }else{
                Log.e("received", "service already running")
            }*/

        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlobalScope.launch(Dispatchers.IO) {
//            var list  = recordingDao.get()
            var list  = db.recordingDao().get()
            if (!list.isEmpty())
            Log.e("dbfiles","${list[0].name}")
        }

        val streetComplaint = arrayOf(
                "Last 48 Hours",
                "Last 24 Hours"
        )
        val adapterStreet = ArrayAdapter(
                this, // Context
                android.R.layout.simple_spinner_item, // Layout
                streetComplaint // Array
        )

        adapterStreet.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        time_spinner.adapter = adapterStreet
        var selectedType = streetComplaint[0]
        time_spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {
                        try {
                            // Display the selected item text on text view
                            val text =
                                    "$position Spinner selected : ${parent.getItemAtPosition(position)}"
                            selectedType= parent.getItemAtPosition(position) as String
                            //Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                    }
                }
    }

    private fun setAdapter(){
        try {
            val pathex = Environment.getExternalStorageDirectory().toString() + "/Recording1"
            val path = this.filesDir.absolutePath+"/Recording1"
            val directory = File(path)
            val files = directory.listFiles()
            filesarray.clear()
            for (i in files.indices) {
                if (files[i].name.contains("Call_"))
                    filesarray.add(files[i])
            }
            filesarray.reverse()
            adapter = LogsAdapter(this, filesarray)
            recyclerView.adapter = adapter
            /*var src = File(path+"/${filesarray[0].name}")
            var dst = File(pathex+"/${filesarray[0].name}")
            copyFile(src,dst)*/
        }catch (e:Exception){}
    }

    @Throws(IOException::class)
    fun copyFile(src: File?, dst: File?) {
        val inChannel = FileInputStream(src).channel
        val outChannel = FileOutputStream(dst).channel
        try {
            inChannel!!.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
        }
    }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    fun startService() {
        if (!isMyServiceRunning(TService::class.java)) {
            val serviceIntent = Intent(this, TService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }else{
            //Toast.makeText(this,"Already Running",Toast.LENGTH_LONG).show()
        }
    }

    fun stopService() {
        val serviceIntent = Intent(this, TService::class.java)
        stopService(serviceIntent)
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
        try {
            val permissionGranted_OutgoingCalls = ActivityCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
            val permissionGranted_phoneState = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            val permissionGranted_recordAudio = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            val permissionGranted_WriteExternal = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val permissionGranted_ReadExternal = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val permissionGranted_ReadCallLog = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
            if (permissionGranted_ReadCallLog) {
                if (permissionGranted_OutgoingCalls) {
                    if (permissionGranted_phoneState) {
                        if (permissionGranted_recordAudio) {
                            if (permissionGranted_WriteExternal) {
                                if (permissionGranted_ReadExternal) {
                                    try {
                                    if (TService.service_running =="") {
                                        startService()
                                    }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 200)
                                }
                            } else {
                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 300)
                            }
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 400)
                        }
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 500)
                    }
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.PROCESS_OUTGOING_CALLS), 600)
                }
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), 700)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 200 || requestCode == 300 || requestCode == 400 || requestCode == 500 || requestCode == 600) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val isActiveAdmin: Boolean
        private get() = mDPM!!.isAdminActive(mAdminName!!)



    companion object {
        private const val REQUEST_CODE = 0
        private const val REQUEST_CODE_ENABLE_ADMIN = 1
    }
}

/*CallRecord callRecord = new CallRecord.Builder(this)
                    .setRecordFileName("RecordFileName")
                    .setRecordDirName("RecordDirName")
                    .setRecordDirPath(Environment.getExternalStorageDirectory().getPath()) // optional & default value
                    .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // optional & default value
                    .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB) // optional & default value
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION) // optional & default value
                    .setShowSeed(true) // optional & default value ->Ex: RecordFileName_incoming.amr || RecordFileName_outgoing.amr
                    .buildService();


            callRecord.startCallRecordService();*/