package br.edu.puccampinas.pi3_es_2024_time_25

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityShowLockerNameBinding

class ShowLockerNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowLockerNameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_locker_name)
        binding = ActivityShowLockerNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLockerNameFromIntent()
        initializeButton()
    }

    @SuppressLint("SetTextI18n")
    private fun getLockerNameFromIntent() {
        val lockerName = intent.getStringExtra("LOCKER_NAME")
        binding.txtLockerName.text = "Armário: $lockerName"
    }

    private fun initializeButton() {
        binding.btnReturnHome.setOnClickListener {
            startActivity(Intent(this, ManagerMainActivity::class.java))
            finish()
        }
    }
}