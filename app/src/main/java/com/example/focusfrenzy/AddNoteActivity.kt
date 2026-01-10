package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: SQLiteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteManager.getInstance(this)

        // Only get datetime from intent; no title
        val dateTime = intent.getStringExtra("datetime") ?: ""

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            val usePomodoro = binding.cbImportant.isChecked

            if (note.isEmpty()) {
                Toast.makeText(this, "Please type a note!", Toast.LENGTH_SHORT).show()
            } else {
                db.addReminder(note, dateTime, usePomodoro)
                // Return data back to SetDateAndTimeActivity
                val resultIntent = Intent()
                resultIntent.putExtra("datetime", dateTime)
                resultIntent.putExtra("note", note)
                resultIntent.putExtra("usePomodoro", usePomodoro)

                setResult(RESULT_OK, resultIntent)
                finish() // closes AddNoteActivity
            }
        }
    }
}
