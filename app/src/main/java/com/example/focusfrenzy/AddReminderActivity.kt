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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Toast.makeText(this, "Notification permission denied. L.", Toast.LENGTH_SHORT).show()
    }

    private val reminderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val id = data.getIntExtra("id", -1)
            val dateTime = data.getStringExtra("datetime") ?: ""
            val note = data.getStringExtra("note") ?: ""
            val usePomodoro = data.getBooleanExtra("usePomodoro", false)

            if (id == -1) {
                val newId = db.addReminder(note, dateTime, usePomodoro).toInt()
                addReminderToUI(newId, note, dateTime, usePomodoro)
                scheduleNotification(note, dateTime)
            } else {
                db.updateReminder(id, note, dateTime, usePomodoro)
                addReminderToUI(id, note, dateTime, usePomodoro)
                scheduleNotification(note, dateTime)
            }
            updateEmptyState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = SQLiteManager.getInstance(this)

        checkNotificationPermission()
        checkExactAlarmPermission()

        binding.btnConnectThing.setOnClickListener {
            Toast.makeText(this, "Feature is Coming Soon", Toast.LENGTH_SHORT).show()
        }

        //Shows 3 Buttons in the main screen (excluding "Connect to Thing" since it is not developed yet)
        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchForReminderActivity::class.java))
        }

        binding.btnMenu.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(this, view)
            popup.menu.add("History")
            popup.menu.add("Settings") //To be used for Case 10 (Customize pomodoro settings)

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

        binding.btnAddActivity.setOnClickListener {
            reminderLauncher.launch(Intent(this, SetDateAndTimeActivity::class.java))
        }

        loadRemindersFromDB()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("I need 'Exact Alarm' permission to yell at you on time. Go to settings?")
                    .setPositiveButton("Sure") { _, _ ->
                        startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    private fun scheduleNotification(note: String, dateTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java).apply {
            putExtra("title", note)
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(dateTime)
            val timeInMillis = date?.time ?: return

            if (timeInMillis <= System.currentTimeMillis()) return

            val requestCode = (note.hashCode() + timeInMillis.toInt())
            val pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cancelAlarm(note: String, dateTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiveReminder::class.java)

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(dateTime)
            val timeInMillis = date?.time ?: return
            val requestCode = (note.hashCode() + timeInMillis.toInt())

            val pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadRemindersFromDB() {
        binding.reminderContainer.removeAllViews()
        val cursor = db.allReminders // Corrected for Java method naming
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val note = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val pom = cursor.getInt(cursor.getColumnIndexOrThrow("usePomodoro")) == 1
                addReminderToUI(id, note, date, pom)
            } while (cursor.moveToNext())
        }
        cursor.close()
        updateEmptyState()
    }

    @SuppressLint("SetTextI18n")
    private fun addReminderToUI(id: Int, note: String, dateTime: String, usePomodoro: Boolean) {
        val existing = binding.reminderContainer.findViewWithTag<LinearLayout>(id)
        if (existing != null) {
            // Index 1 because Checkbox is now at Index 0
            (existing.getChildAt(1) as TextView).text = "$dateTime\n$note ${if (usePomodoro) "🕛" else ""}"
            return
        }

        val layout = LinearLayout(this).apply {
            tag = id
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.reminder_background)
            gravity = android.view.Gravity.CENTER_VERTICAL

            setOnClickListener {
                if (usePomodoro) {
                    val intent = Intent(this@AddReminderActivity, PomodoroTimerActivity::class.java)
                    intent.putExtra("reminderId", id)
                    intent.putExtra("note", note)
                    intent.putExtra("datetime", dateTime)
                    startActivity(intent)
                }
            }
            setOnLongClickListener {
                AlertDialog.Builder(this@AddReminderActivity).setItems(arrayOf("Edit", "Delete")) { _, which ->
                    if (which == 0) {
                        val intent = Intent(this@AddReminderActivity, SetDateAndTimeActivity::class.java)
                        intent.putExtra("id", id)
                        intent.putExtra("note", note)
                        intent.putExtra("datetime", dateTime)
                        intent.putExtra("usePomodoro", usePomodoro)
                        reminderLauncher.launch(intent)
                    } else {
                        cancelAlarm(note, dateTime)
                        db.deleteReminder(id)
                        binding.reminderContainer.removeView(this)
                        updateEmptyState()
                        Toast.makeText(this@AddReminderActivity, "Task deleted.", Toast.LENGTH_SHORT).show()
                    }
                }.show()
                true
            }
        }


        val tv = TextView(this).apply {
            text = "$dateTime\n$note ${if (usePomodoro) "🕛" else ""}"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(resources.getColor(R.color.black))
        }
        // CHECKBOX FOR USE CASE #8
        val cb = CheckBox(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    cancelAlarm(note, dateTime)
                    db.markAsComplete(id)
                    binding.reminderContainer.removeView(layout)
                    updateEmptyState()
                    Toast.makeText(this@AddReminderActivity, "Task finished! Moved to History", Toast.LENGTH_SHORT).show()
                }
            }
        }

        layout.addView(tv)
        layout.addView(cb)
        binding.reminderContainer.addView(layout, 0)
    }

    private fun updateEmptyState() {
        binding.tvNoReminders.visibility = if (binding.reminderContainer.childCount == 0) View.VISIBLE else View.GONE
    }
}