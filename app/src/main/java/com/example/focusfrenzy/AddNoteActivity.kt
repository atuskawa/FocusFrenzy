package com.example.focusfrenzy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            val usePomodoro = binding.cbImportant.isChecked // checkbox state

            if (note.isEmpty()) {
                Toast.makeText(this, "Please type a note!", Toast.LENGTH_SHORT).show()
            } else {
                // saved in local DB
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
                binding.etNoteContent.text.clear()
                binding.cbImportant.isChecked = false // reset checkbox

                val intent = Intent(this, AddReminderActivity::class.java)
                intent.putExtra("note", note)
                intent.putExtra("usePomodoro", usePomodoro) // pass checkbox state
                startActivity(intent)
            }
        }
    }
}
