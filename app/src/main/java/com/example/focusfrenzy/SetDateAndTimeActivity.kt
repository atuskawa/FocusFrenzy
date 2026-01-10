package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class SetDateAndTimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_date_and_time)

        val reminderId = intent.getIntExtra("id", -1) // KEEPING THE ID ALIVE

        findViewById<Button>(R.id.btnSetDateTime).setOnClickListener {
            val date = findViewById<DatePicker>(R.id.datePicker)
            val time = findViewById<TimePicker>(R.id.timePicker)
            val dt = "${date.year}-${date.month + 1}-${date.dayOfMonth} ${time.hour}:${time.minute}"

            val intent = Intent(this, AddNoteActivity::class.java).apply {
                putExtra("id", reminderId)
                putExtra("datetime", dt)
                // Also pass existing data if editing
                putExtra("note", intent.getStringExtra("note"))
                putExtra("usePomodoro", intent.getBooleanExtra("usePomodoro", false))
            }
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (res == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }
    }
}