package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding

@Suppress("DEPRECATION")
class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var reminderContainer: LinearLayout
    private var firstReminderAdded = false

    // Launcher to receive final reminder data from AddNoteActivity (via SetDateAndTimeActivity)
    private val reminderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val dateTime = result.data?.getStringExtra("datetime") ?: return@registerForActivityResult
                val note = result.data?.getStringExtra("note") ?: ""
                val usePomodoro = result.data?.getBooleanExtra("usePomodoro", false) ?: false

                // --------------------------------------------------------------------------
                // Container for the reminder
                val reminderContainerLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 24, 24, 24)
                    setBackgroundResource(R.drawable.reminder_background)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 16, 0, 16)
                    this.layoutParams = layoutParams
                }

                // TextView for reminder content
                val reminderView = TextView(this).apply {
                    text = buildString {
                        append(" $dateTime")
                        if (note.isNotEmpty()) append("\n$note")
                        if (usePomodoro) append(" 🕛")
                    }
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.black))
                }

                reminderContainerLayout.addView(reminderView)

                // Add container to the top of reminderContainer
                reminderContainer.addView(reminderContainerLayout, 0)

                // → Hide "No reminders added" text when a reminder is added
                binding.tvNoReminders.visibility = View.GONE

                // Clickable if Pomodoro
                if (usePomodoro) {
                    reminderContainerLayout.setOnClickListener {
                        val intent = Intent(this, PomodoroTimerActivity::class.java)
                        intent.putExtra("datetime", dateTime)
                        intent.putExtra("note", note)
                        startActivity(intent)
                    }
                }

                // Long-press to delete reminder
                reminderContainerLayout.setOnLongClickListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle("Delete Reminder")
                    builder.setMessage("Do you want to delete this reminder?")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        reminderContainer.removeView(reminderContainerLayout)

                        // → Show "No reminders added" if container is empty
                        if (reminderContainer.childCount == 0) {
                            binding.tvNoReminders.visibility = View.VISIBLE
                        }

                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                    true
                }

                // Shift title on first reminder
                if (!firstReminderAdded) {
                    firstReminderAdded = true
                    val params = binding.tvTitle.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    binding.tvTitle.layoutParams = params
                }
            }
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------
    //everything here, it just makes it so that when a reminder is added, the "no reminder added" alert disappears
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reminderContainer = findViewById(R.id.reminderContainer)

        // → Show "No reminders added" initially if container is empty
        binding.tvNoReminders.visibility = if (reminderContainer.childCount == 0) View.VISIBLE else View.GONE

        binding.btnAddActivity.setOnClickListener {
            val intent = Intent(this, SetDateAndTimeActivity::class.java)
            reminderLauncher.launch(intent)
        }
    }
}
