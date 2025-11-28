package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityPomodoroTimerBinding
import android.os.CountDownTimer
class PomodoroTimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPomodoroTimerBinding
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 25 * 60 * 1000 // 25 min

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTimerText()

        binding.btnStart.setOnClickListener {
            startTimer()
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            @SuppressLint("SetTextI18n") //shut up line 38!
            override fun onFinish() {
                binding.tvTimer.text = "Finished!"
            }
        }.start()
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = 25 * 60 * 1000
        updateTimerText()
    }

    @SuppressLint("DefaultLocale") //shut up line 52!
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}