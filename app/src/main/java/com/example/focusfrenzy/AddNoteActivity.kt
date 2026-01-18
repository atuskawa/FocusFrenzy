package com.example.focusfrenzy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        val dt = intent.getStringExtra("datetime") ?: ""

        // Setup existing data
        binding.etNoteContent.setText(intent.getStringExtra("note") ?: "")
        binding.cbImportant.isChecked = intent.getBooleanExtra("usePomodoro", false)

        binding.btnSaveNote.setOnClickListener {
            val note = binding.etNoteContent.text.toString().trim()
            val usePomo = binding.cbImportant.isChecked
            val shouldNotify = binding.cbSendNotification.isChecked

            if (note.isEmpty()) {
                Toast.makeText(this, "Type something, bestie! 💅", Toast.LENGTH_SHORT).show()
            } else {
                // Just packing the result to send back to AddReminderActivity
                val res = Intent().apply {
                    putExtra("id", id)
                    putExtra("datetime", dt)
                    putExtra("note", note)
                    putExtra("usePomodoro", usePomo)
                    putExtra("sendNotification", shouldNotify)
                }
                setResult(RESULT_OK, res)
                finish()
            }
        }
    }

    /**
     * This is the magic sauce that closes the keyboard when you tap outside the EditText.
     * It intercepts every touch on the screen.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}