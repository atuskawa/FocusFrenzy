package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        val dt = intent.getStringExtra("datetime") ?: ""

        // Pre-fill if editing
        binding.etNoteContent.setText(intent.getStringExtra("note") ?: "")
        binding.cbImportant.isChecked = intent.getBooleanExtra("usePomodoro", false)

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            if (note.isEmpty()) {
                Toast.makeText(this, "Type something!", Toast.LENGTH_SHORT).show()
            } else {
                val res = Intent().apply {
                    putExtra("id", id)
                    putExtra("datetime", dt)
                    putExtra("note", note)
                    putExtra("usePomodoro", binding.cbImportant.isChecked)
                }
                setResult(RESULT_OK, res)
                finish()
            }
        }
    }
}
