package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding

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

            if (id == -1) {
                val newId = db.addReminder(note, dateTime, usePomodoro).toInt()
                addReminderToUI(newId, note, dateTime, usePomodoro)
            } else {
                db.updateReminder(id, note, dateTime, usePomodoro)
                addReminderToUI(id, note, dateTime, usePomodoro)
            }
            updateEmptyState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = SQLiteManager.getInstance(this)

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchForReminderActivity::class.java))
        }

        binding.btnAddActivity.setOnClickListener {
            reminderLauncher.launch(Intent(this, SetDateAndTimeActivity::class.java))
        }
        loadRemindersFromDB()
    }

    private fun loadRemindersFromDB() {
        binding.reminderContainer.removeAllViews()
        val cursor = db.getAllReminders()
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

    private fun addReminderToUI(id: Int, note: String, dateTime: String, usePomodoro: Boolean) {
        val existing = binding.reminderContainer.findViewWithTag<LinearLayout>(id)
        if (existing != null) {
            (existing.getChildAt(0) as TextView).text = "$dateTime\n$note ${if (usePomodoro) "🕛" else ""}"
            return
        }

        val layout = LinearLayout(this).apply {
            tag = id
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.reminder_background)
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
            textSize = 16f
            setTextColor(resources.getColor(R.color.black))
        }
        layout.addView(tv)
        binding.reminderContainer.addView(layout, 0)
    }

    private fun updateEmptyState() {
        binding.tvNoReminders.visibility = if (binding.reminderContainer.childCount == 0) View.VISIBLE else View.GONE
    }
}