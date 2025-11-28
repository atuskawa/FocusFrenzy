package com.example.focusfrenzy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            if (note.isEmpty()) {
                Toast.makeText(this, "Please type a note!", Toast.LENGTH_SHORT).show()
            } else {
                // Here you can save the note in memory, database, or pass it back
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
                binding.etNoteContent.text.clear()
            }
        }
    }
}
