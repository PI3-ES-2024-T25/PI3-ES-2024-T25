package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityDataBinding


class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var rentId: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentInfo()
        getRentInfo()
    }

    private fun getIntentInfo() {
        rentId = intent.getStringExtra("RENT_ID").toString()
    }

    private fun getRentInfo() {
        firestore.collection("rents").document(rentId)
            .get()
            .addOnSuccessListener { result ->
                val data = result.data
                if (data != null) {
                    chooseActivityPeople(rentId, data["customers"].toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents", exception)
            }
    }

    private fun chooseActivityPeople(id: String, people: String?) {
        val button = Button(this)
        button.text = id
        button.setOnClickListener {
            when (people) {
                "1" -> startActivity(
                    Intent(this, UmaPessoaAlocacaoActivity::class.java)
                        .putExtra("documentId", id)
                )

                "2" -> startActivity(
                    Intent(this, DuasPessoasAlocacao1Activity::class.java)
                        .putExtra("documentId", id)
                )

                else -> Log.e("MainActivity", "Invalid value for 'pessoas'")
            }
        }
        binding.buttonContainer.addView(button)
    }
}