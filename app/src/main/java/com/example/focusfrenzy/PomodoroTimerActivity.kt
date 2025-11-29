package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityPomodoroTimerBinding
import android.os.CountDownTimer

class PomodoroTimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPomodoroTimerBinding

    // Timer stuff
    private var timer: CountDownTimer? = null

    // milliseconds
    private var timeLeftInMillis: Long = 25 * 60 * 1000 // 25 min

    // Flag to track if the timer is running
    private var timerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the timer display
        updateTimerText()
        binding.btnStart.text = "Start"
        binding.btnStart.setOnClickListener {
            if (timerRunning) {
                pauseTimer() //this shows pause if the timer is running
            } else {
                startTimer() //this shows start if the timer is not running
                binding.btnStart.text = "Stop"
            }
        }

        // Reset button
        binding.btnReset.setOnClickListener {
            resetTimer() // Stops timer, resets time, leaves button as "Start"
        }
    }

    private fun startTimer() {
        // Create a new CountDownTimer with the remaining time
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished // Update remaining time
                updateTimerText() // Refresh the UI
            }

            override fun onFinish() {
                timerRunning = false // Timer finished, mark as not running
                binding.btnStart.text = "Start" // Reset button text
                binding.tvTimer.text = "Finished!" // Show finished text
            }
        }.start()

        timerRunning = true // Mark timer as running
        binding.btnStart.text = "Stop" // Update button text to reflect stop action
    }

    private fun pauseTimer() {
        timer?.cancel() // Stop the timer
        timerRunning = false // Mark timer as not running
        binding.btnStart.text = "Start" // Button goes back to "Start"
    }

    private fun resetTimer() {
        timer?.cancel() // Stop the timer if running
        timerRunning = false // Make sure timer is marked as not running
        timeLeftInMillis = 25 * 60 * 1000 // Reset timer to full 25 minutes
        updateTimerText() // Update the UI
        binding.btnStart.text = "Start" // Ensure button text shows "Start"
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}
