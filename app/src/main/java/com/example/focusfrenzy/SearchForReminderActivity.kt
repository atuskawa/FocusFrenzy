package com.example.focusfrenzy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivitySearchForReminderBinding

class SearchForReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchForReminderBinding
    private lateinit var db: SQLiteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchForReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteManager.getInstance(this)

        // Real-time search as you type
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                performSearch(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        binding.searchResultContainer.removeAllViews()

        if (query.isEmpty()) {
            binding.tvNoResults.visibility = View.VISIBLE
            return
        }

        val cursor = db.searchReminders(query)
        val hasResults = cursor.count > 0

        if (cursor.moveToFirst()) {
            do {
                val note = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                addResultToUI(note, date)
            } while (cursor.moveToNext())
        }
        cursor.close()

        binding.tvNoResults.visibility = if (hasResults) View.GONE else View.VISIBLE
    }

    private fun addResultToUI(note: String, date: String) {
        // Changes the Date format from 24H to 12H"
        val prettyDate = try {
            val inputFormat =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val outputFormat =
                java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date // Fallback if your database string is weird
        }

        val tv = TextView(this).apply {
            text = "$prettyDate\n$note"
            textSize = 16f
            setPadding(40, 40, 40, 40)
            setTextColor(resources.getColor(R.color.black, null))
            setBackgroundResource(R.drawable.reminder_background)

            // Keeping that personal space we talked about
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        binding.searchResultContainer.addView(tv)
    }
}