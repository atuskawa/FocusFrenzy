package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class SetDateAndTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_date_and_time)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val btnNext = findViewById<Button>(R.id.btnSetDateTime)
        timePicker.setIs24HourView(true)

        btnNext.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1
            val year = datePicker.year
            val hour = timePicker.hour
            val minute = timePicker.minute
            val selectedDateTime = "$year-$month-$day $hour:$minute"

            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("datetime", selectedDateTime)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
