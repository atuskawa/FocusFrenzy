package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var reminderContainer: LinearLayout
    private lateinit var db: SQLiteManager

    // Launcher for SetDateAndTimeActivity
    private val reminderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val dateTime = result.data?.getStringExtra("datetime") ?: return@registerForActivityResult
                val note = result.data?.getStringExtra("note") ?: ""
                val usePomodoro = result.data?.getBooleanExtra("usePomodoro", false) ?: false

                // Insert only if this exact reminder does not exist (DB-level uniqueness check)
                if (!db.reminderExists(note, dateTime)) {
                    db.addReminder(note, dateTime, usePomodoro)
                }

                // Rebuild UI from DB
                loadRemindersFromDB()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteManager.getInstance(this)
        reminderContainer = findViewById(R.id.reminderContainer)

        binding.btnAddActivity.setOnClickListener {
            val intent = Intent(this, SetDateAndTimeActivity::class.java)
            reminderLauncher.launch(intent)
        }

        // Load reminders initially
        loadRemindersFromDB()
    }

    override fun onResume() {
        super.onResume()
        // Rebuild UI every time to prevent duplication
        loadRemindersFromDB()
    }

    private fun loadRemindersFromDB() {
        reminderContainer.removeAllViews() // Clear old views

        val cursor = db.getAllReminders()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val usePomodoro = cursor.getInt(cursor.getColumnIndexOrThrow("usePomodoro")) == 1

                addReminderToUI(id, title, date, usePomodoro)
            } while (cursor.moveToNext())
        }
        cursor.close()

        binding.tvNoReminders.visibility = if (reminderContainer.childCount == 0) View.VISIBLE else View.GONE

    }

    private fun addReminderToUI(id: Int, note: String, dateTime: String, usePomodoro: Boolean) {
        // Prevent duplicate UI for same DB ID
        if (reminderContainer.findViewWithTag<LinearLayout>(id) != null) return

        val reminderLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.reminder_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 16) }
            tag = id
        }

        val reminderView = TextView(this).apply {
            text = buildString {
                append(" $dateTime")
                if (note.isNotEmpty()) append("\n$note")
                if (usePomodoro) append(" 🕛")
            }
            textSize = 16f
            setTextColor(resources.getColor(R.color.black))
        }

        reminderLayout.addView(reminderView)
        reminderContainer.addView(reminderLayout, 0)

        // Pomodoro click
        if (usePomodoro) {
            reminderLayout.setOnClickListener {
                val intent = Intent(this, PomodoroTimerActivity::class.java)
                intent.putExtra("datetime", dateTime)
                intent.putExtra("note", note)
                startActivity(intent)
            }
        }

        // Long press delete
        reminderLayout.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete this Reminder")
                .setMessage("Do you want to delete this reminder?")
                .setPositiveButton("Yes") { dialog, _ ->
                    db.deleteReminder(id)
                    loadRemindersFromDB() // rebuild UI after deletion
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
            true
        }
    }
}
