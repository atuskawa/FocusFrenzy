package com.example.focusfrenzy

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.focusfrenzy.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("FocusFrenzyPrefs", Context.MODE_PRIVATE)

        // Load saved value or default to 25
        val savedFocusTime = sharedPref.getInt("focus_duration", 25)
        binding.sbFocus.progress = savedFocusTime
        binding.tvFocusVal.text = "$savedFocusTime min"

        binding.sbFocus.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualProgress = if (progress < 1) 1 else progress
                if (progress < 1) {
                    seekBar?.progress = 1
                }

                binding.tvFocusVal.text = "$actualProgress min"
                sharedPref.edit().putInt("focus_duration", actualProgress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnBack.setOnClickListener {
            val finalValue = sharedPref.getInt("focus_duration", 25)
            finish()
        }
    }
}