package com.example.focusfrenzy

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SetDateAndTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_date_and_time)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val btnSetDateTime = findViewById<Button>(R.id.btnSetDateTime)
        timePicker.setIs24HourView(true)

        val reminder = intent.getStringExtra("reminder")

        btnSetDateTime.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1 // Months = 0 Based Always (heh based)
            val year = datePicker.year

            val hour = timePicker.hour
            val minute = timePicker.minute

            val selectedDateTime = "$year-$month-$day $hour:$minute"
            Toast.makeText(this, "Selected: $selectedDateTime", Toast.LENGTH_LONG).show()

            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("reminder", reminder)
            intent.putExtra("dateTime", selectedDateTime)
            startActivity(intent)
            finish() // Optional: Finish the current activity")
        }
    }
}
