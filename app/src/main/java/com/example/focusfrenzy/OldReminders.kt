package com.example.focusfrenzy

import android.graphics.Paint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityOldRemindersBinding

class OldReminders : AppCompatActivity() {
    private lateinit var binding: ActivityOldRemindersBinding
    private lateinit var db: SQLiteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOldRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteManager.getInstance(this)
        loadCompletedReminders()
    }

    private fun loadCompletedReminders() {
        binding.oldRemindersContainer.removeAllViews()
        val cursor = db.completedReminders // This calls your Java getCompletedReminders()

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))

                addReminderToHistoryUI(title, date)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun addReminderToHistoryUI(title: String, date: String) {
        val tv = TextView(this).apply {
            text = "✓ $title\n$date"
            textSize = 16f
            setPadding(20, 24, 20, 24)
            setTextColor(resources.getColor(android.R.color.darker_gray))
            // Strike-through effect
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            alpha = 0.6f
        }
        binding.oldRemindersContainer.addView(tv, 0)
    }
}