package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityDataBinding
import com.google.firebase.firestore.FirebaseFirestore


class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var rentId: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun getIntentInfo() {
        rentId = intent.getStringExtra("RENT_ID").toString()
    }


}