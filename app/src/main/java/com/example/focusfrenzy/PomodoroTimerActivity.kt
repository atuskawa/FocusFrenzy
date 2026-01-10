package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityPomodoroTimerBinding
import android.os.CountDownTimer
import android.graphics.Color
import android.text.InputType

class PomodoroTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroTimerBinding
    private lateinit var db: SQLiteManager
    private var timer: CountDownTimer? = null

    // This is now a variable so you can actually change it, genius.
    private var workTime = 25 * 60 * 1000L
    private val breakTime = 5 * 60 * 1000L

    private var timeLeftInMillis: Long = workTime
    private var timerRunning = false
    private var isBreakMode = false
    private var reminderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteManager.getInstance(this)
        reminderId = intent.getIntExtra("reminderId", -1)
        binding.tvReminder.text = intent.getStringExtra("note") ?: "Focus Session"

        updateTimerText()

        binding.btnStart.setOnClickListener {
            if (timerRunning) pauseTimer() else startTimer()
        }

        binding.btnReset.setOnClickListener {
            if (isBreakMode) startWorkManually() else startBreakManually()
        }

        // Tap the timer to set custom minutes (1-60)
        binding.tvTimer.setOnClickListener {
            if (!timerRunning) {
                showSetTimeDialog()
            } else {
                Toast.makeText(this, "Pause the timer first, main character.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSetTimeDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Minutes (1-60)"
        }

        AlertDialog.Builder(this)
            .setTitle("Set Focus Duration")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val mins = input.text.toString().toIntOrNull()
                if (mins != null && mins in 1..60) {
                    workTime = mins * 60 * 1000L
                    if (!isBreakMode) {
                        timeLeftInMillis = workTime
                        updateTimerText()
                    }
                    Toast.makeText(this, "Focus set to $mins mins", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "1 to 60. It's not that deep.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                timerRunning = false
                if (!isBreakMode) {
                    Toast.makeText(this@PomodoroTimerActivity, "Focus done! Break time.", Toast.LENGTH_SHORT).show()
                    startBreakManually()
                } else {
                    Toast.makeText(this@PomodoroTimerActivity, "Break over! Back to work.", Toast.LENGTH_SHORT).show()
                    startWorkManually()
                }
            }
        }.start()

        timerRunning = true
        binding.btnStart.text = "Stop"
        updateUIColors()
    }

    private fun startBreakManually() {
        timer?.cancel()
        timerRunning = false
        isBreakMode = true
        timeLeftInMillis = breakTime
        binding.btnReset.text = "Resume Focus"
        updateTimerText()
        updateUIColors()
        startTimer()
    }

    private fun startWorkManually() {
        timer?.cancel()
        timerRunning = false
        isBreakMode = false
        timeLeftInMillis = workTime // Uses your custom time!
        binding.btnReset.text = "Break"
        updateTimerText()
        updateUIColors()
        startTimer()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        binding.tvSessionType.text = if (isBreakMode) "Break Time" else "Focusing..."
    }

    private fun updateUIColors() {
        val color = if (isBreakMode) "#B3E4C7" else "#D67A7A"
        binding.root.setBackgroundColor(Color.parseColor(color))
        val btnColor = if (isBreakMode) "#95C6A9" else "#B85252"
        binding.btnStart.setBackgroundColor(Color.parseColor(btnColor))
        binding.btnReset.setBackgroundColor(Color.parseColor(btnColor))
    }
}
