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
    private var customDurationInMillis: Long = 25 * 60 * 1000 // Timer defaults to 25 minutes, can be adjusted
    private var timeLeftInMillis: Long = customDurationInMillis
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

        //Makes the timer text clickable
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
                    applyCustomDuration(minutes)
                } else {
                    Toast.makeText(this, "Enter valid minutes", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }
    }
//It's in the name..
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
//It's in the name...
    private fun pauseTimer() {
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
    }
//This allows you to reset the timer to the set duration
    private fun resetTimer() {
        timer?.cancel()
        timerRunning = false
        timeLeftInMillis = customDurationInMillis
        updateTimerText()
        binding.btnStart.text = "Start"
    }

    //This allows you to set your own duration (ie: default is 25 minutes, you can change it to 10 minutes or 5 minutes)
    private fun applyCustomDuration(minutes: Int) {
        customDurationInMillis = minutes * 60 * 1000L
        timeLeftInMillis = customDurationInMillis
        updateTimerText()
        timer?.cancel()
        timerRunning = false
        binding.btnStart.text = "Start"
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}
