package com.example.focusfrenzy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent //cool piece of code, tells the app to move screens
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
                // saved in local DB
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
                binding.etNoteContent.text.clear()
                // Clear yung text field
                binding.etNoteContent.text.clear()

                // babalik eto sa reminder screen since may object na sa reminder
                val intent = Intent(this, AddReminderActivity::class.java)
                intent.putExtra("note", note) // Pass the note to the next activity
                startActivity(intent)
            }
        }
    }
}