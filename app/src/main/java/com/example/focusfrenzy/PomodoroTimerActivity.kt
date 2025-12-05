package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityPomodoroTimerBinding
import android.os.CountDownTimer

class PomodoroTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroTimerBinding
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 25 * 60 * 1000
    private var timerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Display the reminder at the container
        val dateTime = intent.getStringExtra("datetime") ?: ""
        val note = intent.getStringExtra("note") ?: ""

        val reminderText = buildString {
            append(title)
            if (dateTime.isNotEmpty()) append("\n$dateTime")
            if (note.isNotEmpty()) append("\n$note")
        }

        binding.tvReminder.text = reminderText

        // Initialize the timer display
        updateTimerText()
        binding.btnStart.text = "Start"
        binding.btnStart.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
                binding.btnStart.text = "Stop"
            }
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
                timerRunning = false
                binding.btnStart.text = "Start"
                binding.tvTimer.text = "Finished!"
            }
        }.start()

        timerRunning = true
        binding.btnStart.text = "Stop"
    }

    private fun pauseTimer() {
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
    }

    private fun resetTimer() {
        timer?.cancel()
        timerRunning = false
        timeLeftInMillis = 25 * 60 * 1000
        updateTimerText()
        binding.btnStart.text = "Start"
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}
