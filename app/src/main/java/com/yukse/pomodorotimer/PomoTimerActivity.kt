package com.yukse.pomodorotimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.yukse.pomodorotimer.databinding.ActivityPomoTimerBinding
import java.text.DecimalFormat

class PomoTimerActivity : AppCompatActivity() {

    private enum class CurrentTimer {
        STUDY,
        SHORTREST,
        LONGREST
    }

    private enum class CurrentButton{
        PLAY,
        PAUSE
    }

    //binding
    private lateinit var binding: ActivityPomoTimerBinding

    private val PERIOD: Long = 1000
    private val df = DecimalFormat("00")
    private var mMillisInFuture: Long = 0
    private var studyCnt: Int = 1
    private var currentTimer: CurrentTimer = CurrentTimer.STUDY
    private var currentButton: CurrentButton = CurrentButton.PAUSE

    //환경설정 변수
    private lateinit var sp: SharedPreferences


    private var timer: CountDownTimer? = null
    private var autoTimer: CountDownTimer? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    // notification 객체를 생성해주는 건축가 객체 생성 builder
    private lateinit var noti_builder: NotificationCompat.Builder
    private val NOTIFICATION_ID = 1

    //intent를 통해 가져올 데이터 (없으면 환경설정에서 가져옴)
    private var studyT: Long = 1
    private var shortRestT: Long = 1
    private var longRestT: Long = 1
    private var pomo: Int = 4
    private var auto: Boolean = true
    private var noLongRest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomoTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // tool bar
        setSupportActionBar(binding.timerToolbar)
        // 툴바에 홈 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 기존 툴바 타이틀 사용안함
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //환경설정 가져오기
        sp = PreferenceManager.getDefaultSharedPreferences(this@PomoTimerActivity)
        //화면 켜진 상태 유지
        if (sp.getBoolean("display", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        // 공지 띄울 준비
        createNotification()

        binding.btStart.setOnClickListener {
            startButtonClick()
        }

        setTimerValue()
        mMillisInFuture = studyT * 60 * PERIOD
        currentTimer = CurrentTimer.STUDY
        studyCnt = 1

        setUI()
        autoTimerSet()

        if (auto) {
            autoTimer?.start()
        }

    }

    override fun onPause() {
        Log.d("Txx", "pause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("Txx", "stop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Txx", "destroy")
        vibrator?.cancel()
        ringtone?.stop()
        timer?.cancel()
        autoTimer?.cancel()
        NotificationManagerCompat.from(this@PomoTimerActivity).cancel(NOTIFICATION_ID)

        super.onDestroy()
    }

    override fun onBackPressed() {
        timer?.cancel()
        autoTimer?.cancel()
        //정말 끌건지 확인하는 다이얼로그 띄우기
        AlertDialog.Builder(this@PomoTimerActivity)
            .setMessage(R.string.timer_exit)
            .setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, which ->
                super.onBackPressed()
            })
            .setNegativeButton(R.string.button_cancle, DialogInterface.OnClickListener { dialog, which ->
                if(currentButton == CurrentButton.PLAY)
                    startCountDownTimer()
            })
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNotification() {
        // 알림을 관리하는 관리자 객체를 운영체제(context)로부터 소환
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = getString(R.string.app_name)
        val channelID = "${this.packageName}-${channelName}"

        // Oreo 버전(API 26) 이상에서는 알림시에 channel 필수 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW)
            //알림 매니저에게 채널 객체 생성을 요청
            notificationManager.createNotificationChannel(channel)
        }
        // 알림 건축가 객체 생성
        noti_builder = NotificationCompat.Builder(this, channelID)

        //pending intent
        val intent = Intent(baseContext, PomoTimerActivity::class.java)
        //    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //     intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent =
            PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //건축가에게 원하는 알림의 설정작업
        noti_builder.setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentIntent(pendingIntent)
    }

    private fun updateNotification(
        title: String
        , text: String
    ) {
        noti_builder.setContentTitle(title)
            .setContentText(text)

        NotificationManagerCompat.from(this@PomoTimerActivity)
            .notify(NOTIFICATION_ID, noti_builder.build())
    }

    fun setTimerValue() {
        studyT = intent.getLongExtra("study", sp.getInt("study_time", 25).toLong())
        shortRestT = intent.getLongExtra("shortRest", sp.getInt("short_rest_time", 5).toLong())
        longRestT = intent.getLongExtra("longRest", sp.getInt("long_rest_time", 20).toLong())
        pomo = intent.getIntExtra("pomo", sp.getInt("long_rest_pomo", 4))
        auto = intent.getBooleanExtra("auto", sp.getBoolean("auto_timer", false))
        noLongRest = intent.getBooleanExtra("noLongRest", !(sp.getBoolean("use_long_rest", true)))
    }

    //1초 후 자동 학습/휴식 전환
    fun autoTimerSet() {
        autoTimer =
            object : CountDownTimer(PERIOD * 1, PERIOD) {
                override fun onFinish() {
                    startCountDownTimer()
                    currentButton = CurrentButton.PLAY
                    binding.btStart.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24)
                }

                override fun onTick(millisUntilFinished: Long) {}
            }
    }

    fun startCountDownTimer() {
        Log.d("startt", "" + mMillisInFuture + " " + currentTimer + " " + studyCnt)

        timer =
            object : CountDownTimer(mMillisInFuture, PERIOD/10) {

                override fun onFinish() {
                    binding.cpbCirclebar.progress = 0
                    finishedCountDownTimer()
                }

                override fun onTick(millisUntilFinished: Long) {
                    val s = (millisUntilFinished / PERIOD) % 60
                    val m = (millisUntilFinished / (60 * PERIOD)) % 60
                    val h = (millisUntilFinished / (60 * PERIOD)) / 60

                    binding.cpbCirclebar.progress = millisUntilFinished.toInt()
                    if (h > 0) {
                        updateNotification(
                            binding.timerToolbarTitle.text.toString(),
                            "${df.format(h)} : ${df.format(m)} : ${df.format(s)}"
                        )
                        binding.tvTimerTime.setText(
                            "${df.format(h)} : ${df.format(m)} : ${df.format(s)}"
                        )
                    } else {
                        binding.tvTimerTime.setText("${df.format(m)} : ${df.format(s)}")
                        updateNotification(
                            binding.timerToolbarTitle.text.toString(),
                            "${df.format(m)} : ${df.format(s)}"
                        )
                    }

                    mMillisInFuture = millisUntilFinished

                }
            }
        timer?.start()
    }

    fun pauseCountDownTimer() {
        timer?.cancel()
    }

    fun finishedCountDownTimer() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        //진동
        if (sp.getBoolean("vibration_set", true)) {
            // O(26) 버전 전후로 쓰는 방법이 다르다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator?.vibrate(1000)
            }
        }
        //소리
        if (sp.getBoolean("alarm_set", true)) {
            val default_sound: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarm_uri = PreferenceManager.getDefaultSharedPreferences(this@PomoTimerActivity)
                .getString("alarm_sound", default_sound.toString())
            ringtone = RingtoneManager.getRingtone(this@PomoTimerActivity, alarm_uri?.toUri())

            ringtone?.play()
        }

        if (currentTimer == CurrentTimer.STUDY) {
            if (studyCnt < pomo || noLongRest) {
                //short rest
                currentTimer = CurrentTimer.SHORTREST
                mMillisInFuture = shortRestT * 60 * PERIOD
            } else {
                // long rest
                currentTimer = CurrentTimer.LONGREST
                mMillisInFuture = longRestT * 60 * PERIOD
            }
        } else if (currentTimer == CurrentTimer.SHORTREST) {
            studyCnt++
            currentTimer = CurrentTimer.STUDY
            mMillisInFuture = studyT * 60 * PERIOD
        } else {
            studyCnt = 1
            currentTimer = CurrentTimer.STUDY
            mMillisInFuture = studyT * 60 * PERIOD
        }
        //UI변경
        setUI()
        currentButton = CurrentButton.PAUSE
        binding.btStart.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24)

        //자동일 경우 바로 전환
        if (auto) {
            autoTimer?.start()
        }
    }

    fun setUI() {
        if (currentTimer == CurrentTimer.STUDY) {
            binding.timerToolbarTitle.setText(R.string.timer_study_name)
        } else if (currentTimer == CurrentTimer.SHORTREST) {
            binding.timerToolbarTitle.setText(R.string.timer_rest_name)
        } else {
            binding.timerToolbarTitle.setText(R.string.timer_long_rest_name)
        }
        binding.cpbCirclebar.max = mMillisInFuture.toInt()
        binding.cpbCirclebar.progress = mMillisInFuture.toInt()

        val s = (mMillisInFuture / PERIOD) % 60
        val m = (mMillisInFuture / (60 * PERIOD)) % 60
        val h = (mMillisInFuture / (60 * PERIOD)) / 60

        if (h > 0) {
            binding.tvTimerTime.setText("${df.format(h)} : ${df.format(m)} : ${df.format(s)}")
        } else {
            binding.tvTimerTime.setText("${df.format(m)} : ${df.format(s)}")
        }

        if (noLongRest)
            binding.tvPomoTimes.setText("$studyCnt / INF")
        else
            binding.tvPomoTimes.setText("$studyCnt / $pomo")
    }

    fun startButtonClick() {
        if (currentButton == CurrentButton.PAUSE) {
            startCountDownTimer()
            binding.btStart.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24)
            currentButton = CurrentButton.PLAY
        } else {
            pauseCountDownTimer()
            binding.btStart.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24)
            currentButton = CurrentButton.PAUSE
        }
    }
}