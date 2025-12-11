package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityPomodoroTimerBinding
import android.os.CountDownTimer

class PomodoroTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroTimerBinding
    private var timer: CountDownTimer? = null
    private var customDurationInMillis: Long = 25 * 60 * 1000
    private var timeLeftInMillis: Long = customDurationInMillis
    private var timerRunning = false

    // Colors
    private val sageGreen = "#B3E4C7"
    private val dustyRed = "#D67A7A"
    private val darkRed = "#B85252"
    private val buttonOriginal = "#95C6A9"
    private val textOriginal = "#FFFFFF"
    private val reminderOriginal = "#95C6A9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Display the reminder
        val dateTime = intent.getStringExtra("datetime") ?: ""
        val note = intent.getStringExtra("note") ?: ""

        val reminderText = buildString {
            append(title)
            if (dateTime.isNotEmpty()) append("\n$dateTime")
            if (note.isNotEmpty()) append("\n$note")
        }
        binding.tvReminder.text = reminderText

        // Initialize timer
        updateTimerText()
        binding.btnStart.text = "Start"
        binding.btnStart.setOnClickListener {
            if (timerRunning) pauseTimer() else startTimer()
        }

        //Resets the Timer to selected time
        binding.btnReset.setOnClickListener { resetTimer() }

        //the shit that makes the timer go red and green
        binding.tvTimer.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Set timer (minutes)")

            val input = EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.hint = "Minutes"
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, _ ->
                val minutes = input.text.toString().toIntOrNull()
                if (minutes != null && minutes > 0) {
                    if (minutes > 60) {
                        Toast.makeText(this, "Maximum is 60 minutes", Toast.LENGTH_SHORT).show()
                    } else {
                        applyCustomDuration(minutes)
                    }
                } else {
                    Toast.makeText(this, "You Cannot Leave This Blank", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }
//----------------------------------------------------------------------------------------------------------------------------------
    private fun startTimer() {
        // Change colors of root, buttons, timer, reminder when active and idle
        binding.root.setBackgroundColor(android.graphics.Color.parseColor(dustyRed))
        binding.btnStart.setBackgroundColor(android.graphics.Color.parseColor(darkRed))
        binding.btnReset.setBackgroundColor(android.graphics.Color.parseColor(darkRed))
        binding.tvTimer.setTextColor(android.graphics.Color.parseColor(textOriginal))
        binding.tvReminder.setBackgroundColor(android.graphics.Color.parseColor(darkRed))
        binding.tvReminder.setTextColor(android.graphics.Color.parseColor(textOriginal))

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }
            override fun onFinish() {
                timerRunning = false
                binding.btnStart.text = "Start"
                binding.tvTimer.text = "Finished!"
                resetColors()
            }
        }.start()
        timerRunning = true
        binding.btnStart.text = "Stop"
    }

    private fun pauseTimer() {
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
        resetColors()
    }

    private fun resetTimer() {
        timer?.cancel()
        timerRunning = false
        timeLeftInMillis = customDurationInMillis
        updateTimerText()
        binding.btnStart.text = "Start"
        resetColors()
    }

    private fun applyCustomDuration(minutes: Int) {
        customDurationInMillis = minutes * 60 * 1000L
        timeLeftInMillis = customDurationInMillis
        updateTimerText()
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
        resetColors()
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun resetColors() {
        // Reset all colors to original
        binding.root.setBackgroundColor(android.graphics.Color.parseColor(sageGreen))
        binding.btnStart.setBackgroundColor(android.graphics.Color.parseColor(buttonOriginal))
        binding.btnReset.setBackgroundColor(android.graphics.Color.parseColor(buttonOriginal))
        binding.tvTimer.setTextColor(android.graphics.Color.parseColor(textOriginal))
        binding.tvReminder.setBackgroundColor(android.graphics.Color.parseColor(reminderOriginal))
        binding.tvReminder.setTextColor(android.graphics.Color.parseColor("#000000"))
    }
}
