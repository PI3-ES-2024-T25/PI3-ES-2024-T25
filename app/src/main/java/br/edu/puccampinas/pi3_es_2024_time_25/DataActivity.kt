package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityDataBinding
import com.google.gson.Gson


class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchAllocations()
    }

    private fun fetchAllocations() {
        firestore.collection("rents")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    val pessoas = document.getString("pessoas")
                    addButton(id, pessoas)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents", exception)
            }
    }

    private fun addButton(id: String, pessoas: String?) {
        val button = Button(this)
        button.text = id
        button.setOnClickListener {
            when (pessoas) {
                "1" -> startActivity(Intent(this, UmaPessoaAlocacaoActivity::class.java)
                    .putExtra("documentId", id))
                "2" -> startActivity(Intent(this, DuasPessoasAlocacao1Activity::class.java)
                    .putExtra("documentId", id))
                else -> Log.e("MainActivity", "Invalid value for 'pessoas'")
            }
        }
        binding.buttonContainer.addView(button)
    }
}