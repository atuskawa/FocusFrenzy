package com.example.focusfrenzy

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

            override fun onFinish() {
                binding.tvTimer.text = "Done!"
            }
        }.start()
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = 25 * 60 * 1000
        updateTimerText()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}