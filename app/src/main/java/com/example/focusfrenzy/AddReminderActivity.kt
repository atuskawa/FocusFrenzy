package com.example.focusfrenzy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivityAddReminderBinding

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddActivity.setOnClickListener {
            val intent = Intent(this, SetDateAndTimeActivity::class.java)
            startActivity(intent)
        }
    }
}


