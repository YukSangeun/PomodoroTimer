package com.yukse.pomodorotimer

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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


    private var timer: CountDownTimer? = null
    private var autoTimer: CountDownTimer? = null

    //UI - binding으로 이후 변경할 것
    lateinit var tv_title : TextView
    lateinit var tv_minutes :TextView
    lateinit var tv_seconds :TextView
    lateinit var tv_pomo :TextView
    lateinit var bt_start :Button

    //여기값은 설정에서 가져올 값. 아직 설정 없어서 변수로 선언해둔 것
    private val auto: Boolean = true
    private val studyT: Long = 1
    private val shortRestT: Long = 1
    private val longRestT: Long = 1
    private val pomo: Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomo_timer)

        tv_title = findViewById<TextView>(R.id.tv_title)
        tv_minutes = findViewById<TextView>(R.id.tv_timer_minutes)
        tv_seconds = findViewById<TextView>(R.id.tv_timer_seconds)
        tv_pomo = findViewById<TextView>(R.id.tv_pomoTimes)
        bt_start = findViewById<Button>(R.id.bt_start)
        bt_start.setOnClickListener{
            startButtonClick()
        }


        mMillisInFuture = studyT*60*PERIOD
        currentTimer = CurrentTimer.STUDY
        studyCnt = 1

        setUI()
        autoTimerSet()

        if(auto){
            autoTimer?.start()
        }

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
        Log.d("startt", ""+ mMillisInFuture + " " + currentTimer + " " + studyCnt)

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

                    Log.d("startt", ""+ m + " " + s + " " + studyCnt)


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

        if (currentTimer == CurrentTimer.STUDY) {
            if (studyCnt < 4) {
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