package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var db: SQLiteManager

    private val reminderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val id = data.getIntExtra("id", -1)
            val dateTime = data.getStringExtra("datetime") ?: ""
            val note = data.getStringExtra("note") ?: ""
            val usePomodoro = data.getBooleanExtra("usePomodoro", false)
            val shouldNotify = data.getBooleanExtra("sendNotification", true)

            if (id == -1) {
                val newId = db.addReminder(note, dateTime, usePomodoro).toInt()
                addReminderToUI(newId, note, dateTime, usePomodoro)
                if (shouldNotify) scheduleNotification(newId, note, dateTime)
            } else {
                db.updateReminder(id, note, dateTime, usePomodoro)
                addReminderToUI(id, note, dateTime, usePomodoro)
                // Kill old one and set new one if enabled
                cancelAlarm(id)
                if (shouldNotify) scheduleNotification(id, note, dateTime)
            }
            updateEmptyState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = SQLiteManager.getInstance(this)

        binding.btnAddActivity.setOnClickListener {
            reminderLauncher.launch(Intent(this, SetDateAndTimeActivity::class.java))
        }

        binding.btnConnectThing.setOnClickListener {
            Toast.makeText(this, "Feature coming soon 💀", Toast.LENGTH_SHORT).show()
        }

        loadRemindersFromDB()
    }

    // This goes AFTER the onCreate method ends
    private fun scheduleNotification(taskId: Int, note: String, dateTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("note_content", note)
            putExtra("id", taskId)
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timeInMillis = sdf.parse(dateTime)?.time ?: return

            // If the time is in the past, don't even bother
            if (timeInMillis <= System.currentTimeMillis()) return

            val pendingIntent = PendingIntent.getBroadcast(
                this, taskId, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Security check for Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    // Fallback to inexact if they haven't granted permission yet
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleSystemReminder(note: String, dateTime: String, taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val triggerTime: Long = try {
            sdf.parse(dateTime)?.time ?: return
        } catch (e: Exception) { return }

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
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                // They didn't give you permission, so we have to use a "flexible" alarm
                // OR take them to settings. Let's do a flexible one so it doesn't crash.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Toast.makeText(this, "Permission missing: Reminder might be slightly late.", Toast.LENGTH_SHORT).show()

                // Optional: Send them to settings to fix it
                // startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        } else {
            // Old phones don't care about your privacy/battery
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelAlarm(taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("id", taskId) // Match intent filter exactly
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, taskId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    @SuppressLint("SetTextI18n")
    private fun addReminderToUI(id: Int, note: String, dateTime: String, usePomodoro: Boolean) {
        val existing = binding.reminderContainer.findViewWithTag<LinearLayout>(id)

        if (existing != null) {
            // FIX: Index 0 is the TextView. Index 1 is the Checkbox.
            val tv = existing.getChildAt(0) as? TextView
            tv?.text = "$dateTime\n$note ${if (usePomodoro) "🕛" else ""}"
            return
        }

        val layout = LinearLayout(this).apply {
            tag = id
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.reminder_background)
            gravity = android.view.Gravity.CENTER_VERTICAL

            isClickable = true
            isFocusable = true

            setOnLongClickListener {
                AlertDialog.Builder(context).setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) { //Flow is Set Date and Time, then Add Note.
                        val intent = Intent(this@AddReminderActivity, SetDateAndTimeActivity::class.java).apply {
                            putExtra("id", id); putExtra("note", note); putExtra("datetime", dateTime); putExtra("usePomodoro", usePomodoro)
                        }
                        reminderLauncher.launch(intent)
                    } else {
                        cancelAlarm(id)
                        db.deleteReminder(id)
                        binding.reminderContainer.removeView(this)
                        updateEmptyState()
                    }
                }.show()
                true
            }
        }

        val tv = TextView(this).apply {
            text = "$dateTime\n$note ${if (usePomodoro) "🕛" else ""}"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val cb = CheckBox(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    cancelAlarm(id)
                    db.markAsComplete(id)
                    binding.reminderContainer.removeView(layout)
                    updateEmptyState()
                }
            }
        }

        layout.addView(tv)
        layout.addView(cb)
        binding.reminderContainer.addView(layout, 0)
    }

    private fun loadRemindersFromDB() {
        binding.reminderContainer.removeAllViews()
        val cursor = db.allReminders
        if (cursor.moveToFirst()) {
            do {
                addReminderToUI(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("usePomodoro")) == 1
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        binding.tvNoReminders.visibility = if (binding.reminderContainer.childCount == 0) View.VISIBLE else View.GONE
    }
}