package com.example.focusfrenzy

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var db: SQLiteManager

    // Formatters: input handles your DB string, output handles the UI "Vibe"
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

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

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchForReminderActivity::class.java))
        }

        binding.btnMenu.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menu.add("History")
            popup.menu.add("Settings")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "History" -> {
                        startActivity(Intent(this, OldReminders::class.java))
                        true
                    }
                    "Settings" -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
        loadRemindersFromDB()
    }

    // helper so it converts from 24H to 12H
    private fun formatTime(dateTime: String): String {
        return try {
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateTime // If it's garbage, just show the garbage
        }
    }

    private fun scheduleNotification(taskId: Int, note: String, dateTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("note_content", note)
            putExtra("id", taskId)
        }

        try {
            // Using 'mm' for double-digit minutes. No more 14:9 logic.
            val timeInMillis = inputFormat.parse(dateTime)?.time ?: return

            if (timeInMillis <= System.currentTimeMillis()) return

            val pendingIntent = PendingIntent.getBroadcast(
                this, taskId, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    // Inexact fallback so the app doesn't ghost the user
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    Toast.makeText(this, "Notification might be late (Permission missing).", Toast.LENGTH_SHORT).show()
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm(taskId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("id", taskId)
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
        val prettyTime = formatTime(dateTime)
        val displayText = "$prettyTime\n$note ${if (usePomodoro) "🕛" else ""}"

        val existing = binding.reminderContainer.findViewWithTag<LinearLayout>(id)

        if (existing != null) {
            (existing.getChildAt(0) as? TextView)?.text = displayText
            return
        }

        val layout = LinearLayout(this).apply {
            tag = id
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 32, 32, 32)
            setBackgroundResource(R.drawable.reminder_background)
            gravity = android.view.Gravity.CENTER_VERTICAL

            // gives each reminder a space so they don't stick together
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 8) // This adds 16dp of space at the bottom of each item
            layoutParams = params

            isClickable = true
            isFocusable = true

            setOnClickListener {
                if (usePomodoro) {
                    val intent = Intent(this@AddReminderActivity, PomodoroTimerActivity::class.java).apply {
                        putExtra("note", note)
                        putExtra("id", id)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "No timer enabled for this reminder", Toast.LENGTH_SHORT).show()
                }
            }

            setOnLongClickListener {
                AlertDialog.Builder(context).setItems(arrayOf("Edit Reminder", "Delete")) { _, which ->
                    if (which == 0) {
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
            text = displayText
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val cb = CheckBox(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    cancelAlarm(id)
                    db.markAsComplete(id)
                    Toast.makeText(this@AddReminderActivity, "Task complete! Moved to History", Toast.LENGTH_SHORT).show()
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