package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
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

                // ------------------------------------------------------------------------------
                //this is just a container for the reminders so it looks cleaner
                val reminderContainerLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 24, 24, 24)
                    setBackgroundResource(R.drawable.reminder_background) // shaded background
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 16, 0, 16) // spacing between reminders
                    this.layoutParams = layoutParams
                }

                // ✅ Create TextView for reminder content
                val reminderView = TextView(this).apply {
                    text = buildString {
                        append("• $dateTime")               // Show date/time
                        if (note.isNotEmpty()) append("\n$note") // Then the note
                        if (usePomodoro) append("\n(Pomodoro)")
                    }
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.black))
                }

                // Add TextView to container
                reminderContainerLayout.addView(reminderView)

                // Add container to the top of reminderContainer
                reminderContainer.addView(reminderContainerLayout, 0)

                // Make clickable if Pomodoro
                if (usePomodoro) {
                    reminderContainerLayout.setOnClickListener {
                        val intent = Intent(this, PomodoroTimerActivity::class.java)
                        intent.putExtra("datetime", dateTime)
                        intent.putExtra("note", note)
                        startActivity(intent)
                    }
                }

                // ✅ Add long-press to delete reminder
                reminderContainerLayout.setOnLongClickListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle("Delete Reminder")
                    builder.setMessage("Do you want to delete this reminder?")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        reminderContainer.removeView(reminderContainerLayout)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                    true // indicates the long press is consumed
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reminderContainer = findViewById(R.id.reminderContainer)

        binding.btnAddActivity.setOnClickListener {
            val intent = Intent(this, SetDateAndTimeActivity::class.java)
            reminderLauncher.launch(intent)
        }
    }
}
