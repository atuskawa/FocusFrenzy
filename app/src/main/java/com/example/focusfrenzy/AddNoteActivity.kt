package com.example.focusfrenzy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        val dt = intent.getStringExtra("datetime") ?: ""

        binding.etNoteContent.setText(intent.getStringExtra("note") ?: "")
        binding.cbImportant.isChecked = intent.getBooleanExtra("usePomodoro", false)

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            val usePomo = binding.cbImportant.isChecked
            val shouldNotify = binding.cbSendNotification.isChecked

            if (note.isEmpty()) {
                Toast.makeText(this, "Type something!", Toast.LENGTH_SHORT).show()
            } else {
                // THE KILL SWITCH: We always try to cancel first to avoid duplicates
                cancelSystemReminder(id)

                if (shouldNotify && dt.isNotEmpty()) {
                    scheduleSystemReminder(note, dt, id)
                }

                val res = Intent().apply {
                    putExtra("id", id)
                    putExtra("datetime", dt)
                    putExtra("note", note)
                    putExtra("usePomodoro", usePomo)
                    putExtra("sendNotification", shouldNotify)
                }
                setResult(RESULT_OK, res)
                finish()
            }
        }
    }

    private fun cancelSystemReminder(taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("id", taskId) // Key consistency!
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun scheduleSystemReminder(note: String, dateTime: String, taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val triggerTime: Long = try {
            sdf.parse(dateTime)?.time ?: return
        } catch (e: Exception) {
            return
        }

        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("note_content", note)
            putExtra("id", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // THE FIX: Check if we have permission for EXACT alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // They didn't give you permission, so we have to use a "flexible" alarm
                // OR take them to settings. Let's do a flexible one so it doesn't crash.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Toast.makeText(
                    this,
                    "Permission missing: Reminder might be slightly late.",
                    Toast.LENGTH_SHORT
                ).show()

                // Optional: Send them to settings to fix it
                // startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        } else {
            // Old phones don't care about your privacy/battery
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}