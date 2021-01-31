package com.yukse.pomodorotimer

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
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import java.text.DecimalFormat

class PomoTimerActivity : AppCompatActivity() {

    private enum class CurrentTimer {
        STUDY,
        SHORTREST,
        LONGREST
    }

    private val PERIOD: Long = 1000
    private val df = DecimalFormat("00")
    private var mMillisInFuture: Long = 0
    private var studyCnt: Int = 1
    private var currentTimer: CurrentTimer = CurrentTimer.STUDY

    //환경설정 변수
    private lateinit var sp: SharedPreferences


    private var timer: CountDownTimer? = null
    private var autoTimer: CountDownTimer? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    //UI - binding으로 이후 변경할 것
    lateinit var tv_title: TextView
    lateinit var tv_minutes: TextView
    lateinit var tv_seconds: TextView
    lateinit var tv_pomo: TextView
    lateinit var bt_start: Button

    //intent를 통해 가져올 데이터 (없으면 환경설정에서 가져옴)
    private var studyT: Long = 1
    private var shortRestT: Long = 1
    private var longRestT: Long = 1
    private var pomo: Int = 4
    private var auto: Boolean = true
    private var noLongRest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomo_timer)
        //환경설정 가져오기
        sp = PreferenceManager.getDefaultSharedPreferences(this@PomoTimerActivity)
        //화면 켜진 상태 유지
        if(sp.getBoolean("display", true)){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        tv_title = findViewById<TextView>(R.id.tv_title)
        tv_minutes = findViewById<TextView>(R.id.tv_timer_minutes)
        tv_seconds = findViewById<TextView>(R.id.tv_timer_seconds)
        tv_pomo = findViewById<TextView>(R.id.tv_pomoTimes)
        bt_start = findViewById<Button>(R.id.bt_start)
        bt_start.setOnClickListener {
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
        vibrator?.cancel()
        ringtone?.stop()

        super.onPause()
    }

    override fun onBackPressed() {
        timer?.cancel()
        autoTimer?.cancel()
        //정말 끌건지 확인하는 다이얼로그 띄우기
        AlertDialog.Builder(this@PomoTimerActivity)
            .setMessage("타이머가 종료됩니다.\n현재 작동 중인 타이머를 종료하시겠습니까?")
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                super.onBackPressed()
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                startCountDownTimer()
            })
            .show()
    }

    fun setTimerValue() {
        studyT = intent.getLongExtra("study", sp.getInt("study_time", 1).toLong())
        shortRestT = intent.getLongExtra("shortRest", sp.getInt("short_rest_time", 1).toLong())
        longRestT = intent.getLongExtra("longRest", sp.getInt("long_rest_time", 1).toLong())
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
                    bt_start.setText("중지")
                }

                override fun onTick(millisUntilFinished: Long) {}
            }
    }

    fun startCountDownTimer() {
        Log.d("startt", "" + mMillisInFuture + " " + currentTimer + " " + studyCnt)

        timer =
            object : CountDownTimer(mMillisInFuture, PERIOD) {

                override fun onFinish() {
                    finishedCountDownTimer()
                }

                override fun onTick(millisUntilFinished: Long) {
                    val s = (millisUntilFinished / PERIOD) % 60
                    val m = millisUntilFinished / (60 * PERIOD)

                    tv_minutes.setText("" + df.format(m))
                    tv_seconds.setText("" + df.format(s))

                    Log.d("startt", "" + m + " " + s + " " + studyCnt)


                    mMillisInFuture = millisUntilFinished

                }
            }
        timer?.start()
    }

    fun pauseCountDownTimer() {
        timer?.cancel()
    }

    fun finishedCountDownTimer() {
        val finishToast: Toast
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
            val default_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarm_uri = PreferenceManager.getDefaultSharedPreferences(this@PomoTimerActivity)
                .getString("alarm_sound", default_sound.toString())
            ringtone = RingtoneManager.getRingtone(this@PomoTimerActivity, alarm_uri?.toUri())

            ringtone?.play()
        }

        if (currentTimer == CurrentTimer.STUDY) {
            if (studyCnt < pomo || noLongRest) {
                //short rest
                finishToast = Toast.makeText(
                    this@PomoTimerActivity,
                    "학습이 종료되었습니다.\n짧은휴식으로 전환됩니다.",
                    Toast.LENGTH_LONG
                )
                currentTimer = CurrentTimer.SHORTREST
                mMillisInFuture = shortRestT * 60 * PERIOD
            } else {
                // long rest
                finishToast = Toast.makeText(
                    this@PomoTimerActivity,
                    "학습이 종료되었습니다.\n긴휴식으로 전환됩니다.",
                    Toast.LENGTH_LONG
                )
                currentTimer = CurrentTimer.LONGREST
                mMillisInFuture = longRestT * 60 * PERIOD
            }
        } else if (currentTimer == CurrentTimer.SHORTREST) {
            finishToast = Toast.makeText(
                this@PomoTimerActivity,
                "짧은휴식이 종료되었습니다.\n학습으로 전환됩니다.",
                Toast.LENGTH_LONG
            )
            studyCnt++
            currentTimer = CurrentTimer.STUDY
            mMillisInFuture = studyT * 60 * PERIOD
        } else {
            finishToast = Toast.makeText(
                this@PomoTimerActivity,
                "긴휴식이 종료되었습니다.\n학습으로 전환됩니다.",
                Toast.LENGTH_LONG
            )
            studyCnt = 1
            currentTimer = CurrentTimer.STUDY
            mMillisInFuture = studyT * 60 * PERIOD
        }
        //종료 토스트 띄우기
        finishToast.show()
        //알람 울리기

        //UI변경
        setUI()
        bt_start.setText("시작")

        //자동일 경우 바로 전환
        if (auto) {
            autoTimer?.start()
        }
    }

    fun setUI() {
        if (currentTimer == CurrentTimer.STUDY) {
            tv_title.setText("학습")
        } else if (currentTimer == CurrentTimer.SHORTREST) {
            tv_title.setText("짧은휴식")
        } else {
            tv_title.setText("긴휴식")
        }

        tv_minutes.setText(df.format(mMillisInFuture / (60 * PERIOD)).toString())
        tv_seconds.setText(df.format((mMillisInFuture / PERIOD) % 60).toString())
        if (noLongRest)
            tv_pomo.setText("$studyCnt / INF")
        else
            tv_pomo.setText("$studyCnt / $pomo")
    }

    fun startButtonClick() {
        if (bt_start.text.equals("시작")) {
            startCountDownTimer()
            bt_start.setText("중지")
        } else {
            pauseCountDownTimer()
            bt_start.setText("시작")

        }
    }
}