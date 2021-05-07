package com.example.testrecorder

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class LogsAdapter(var context: Context, private var logs: List<File>) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)

        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(logs[position],position,context)


    }

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView =itemView.findViewById(R.id.title)
        var time_ago: TextView =itemView.findViewById(R.id.time_ago)
        var play: ImageView =itemView.findViewById(R.id.play)

        fun bindItems(log: File, position: Int, context: Context) {

            var showName=getName(log.name)
            title.text = showName
            try {
                var tims = log.name.split("_")[2]
                val times = tims.split(":")[1]
                val dates = tims.split(":")[0]
                val pattern = "MMMM d, yyyy"
                val pattern_time = "hh:mm a"
                val format = SimpleDateFormat("dd-MM-yyyy")
                    val date = format.parse(dates)
//                val time_format = SimpleDateFormat("hh-mm-ss")
//                    val time = time_format.parse(times)

                    val simpleDateFormat = SimpleDateFormat(pattern)
                    val final_date = simpleDateFormat.format(date)
//                    val simpleDateFormat2 = SimpleDateFormat(pattern_time)
//                    val final_time = simpleDateFormat2.format(time)
                time_ago.text="$final_date ${times.replace("-",":")}"
            }catch (e:Exception){}
            //getTimeAgo(tims,time_ago)

           // var  mpintro = MediaPlayer.create(context, Uri.parse(log.absolutePath))
            play.setOnClickListener {
//                mpintro.isLooping = true
                try {
                    player(showName,log,context)
                    /*if (mpintro.isPlaying) {
                        play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play))
                        mpintro.stop()
                    } else {
                        play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.stop))
                        mpintro.start()
                    }*/
                }catch (e:Exception){
                    e.printStackTrace()
                    Toast.makeText(context,e.printStackTrace().toString(),Toast.LENGTH_LONG).show()
                }

            }


        }

        private fun getName(name: String):String{
            var temp = name.split("_")
            var rep = temp[temp.size-1]
            return name.replace("_${rep}","")
        }

        private fun getTimeAgo(timestamp: String, timeAgo: TextView){
            val sdf = SimpleDateFormat("dd-MM-yyyy:hh-mm-ss")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            try {
                val time: Long = sdf.parse(timestamp).time
                val now = System.currentTimeMillis()
                val ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
                timeAgo.text=ago
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        
        private fun initializeSeekBar(seek_bar: SeekBar) {
            seek_bar.max = mediaPlayer.seconds

            runnable = Runnable {
                seek_bar.progress = mediaPlayer.currentSeconds

                /*tv_pass.text = "${mediaPlayer.currentSeconds} sec"
                val diff = mediaPlayer.seconds - mediaPlayer.currentSeconds
                tv_due.text = "$diff sec"*/

                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)
        }

        private lateinit var mediaPlayer: MediaPlayer
        private lateinit var runnable:Runnable
        private var handler: Handler = Handler()
        private fun player(name: String,log: File, context: Context) {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.audio_dialog)
            val title = dialog.findViewById(R.id.title) as TextView
            val play = dialog.findViewById(R.id.play) as ImageView
            val seek_bar = dialog.findViewById(R.id.seek_bar) as SeekBar

            title.text=name
//            mediaPlayer = MediaPlayer.create(context, Uri.parse(log.absolutePath))
            var fileInputStream: FileInputStream? = null
            try {
                mediaPlayer = MediaPlayer()
                fileInputStream = FileInputStream(log.absolutePath)
                mediaPlayer.setDataSource(fileInputStream.fd)
                fileInputStream.close()
                mediaPlayer.prepare()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mediaPlayer.start()
            initializeSeekBar(seek_bar)
            play.setOnClickListener {
                dialog.dismiss()
            }
            seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if (mediaPlayer.seconds==i){
                        dialog.dismiss()
                    }
                    /*if (b) {
                        Toast.makeText(context,"Complete $i",Toast.LENGTH_LONG).show()
                        mediaPlayer.seekTo(i * 1000)
                    }*/
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
            dialog.setOnCancelListener {
                try {
                    if (mediaPlayer.isPlaying) {
                        seek_bar.setProgress(0)
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        mediaPlayer.release()
                        handler.removeCallbacks(runnable)

                    }
                }catch (e:Exception){}
            }
            dialog.setOnDismissListener {
                try {
                    if (mediaPlayer.isPlaying) {
                        seek_bar.setProgress(0)
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        mediaPlayer.release()
                        handler.removeCallbacks(runnable)

                    }
                }catch (e:Exception){}
            }
            dialog.show()
            val window = dialog.window
            window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        }
    }


}



val MediaPlayer.seconds:Int
    get() {
        return this.duration / 1000
    }
// Creating an extension property to get media player current position in seconds
val MediaPlayer.currentSeconds:Int
    get() {
        return this.currentPosition/1000
    }