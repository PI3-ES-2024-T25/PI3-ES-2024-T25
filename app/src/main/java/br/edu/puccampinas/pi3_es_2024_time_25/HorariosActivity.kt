package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class HorariosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var textoHorarios: TextView
    private lateinit var confirmarLocacao: AppCompatButton
    private lateinit var voltarTempo: Button

    private var selectedButton: AppCompatButton? = null
    private lateinit var buttonMap: Map<AppCompatButton, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horarios)

        textoHorarios = findViewById(R.id.texto_horarios)
        confirmarLocacao = findViewById(R.id.Confirmar_locacao)
        voltarTempo = findViewById(R.id.voltar_tempo)

        voltarTempo.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        // Lista de horários disponíveis para locação
        val horarios = listOf(
            "30 minutos - R$ 30,00",
            "1 hora - R$ 50,00",
            "2 horas - R$ 100,00",
            "4 horas - R$ 150,00",
            "Até as 18 horas - R$ 300,00"
        )

        // Associa os horários aos botões
        buttonMap = mapOf(
            findViewById<AppCompatButton>(R.id.btn_tempo_1) to horarios[0],
            findViewById<AppCompatButton>(R.id.btn_tempo_2) to horarios[1],
            findViewById<AppCompatButton>(R.id.btn_tempo_3) to horarios[2],
            findViewById<AppCompatButton>(R.id.btn_tempo_4) to horarios[3],
            findViewById<AppCompatButton>(R.id.btn_tempo_5) to horarios[4]
        )

        buttonMap.forEach { (button, horario) ->
            button.text = horario
            button.setOnClickListener {
                if (selectedButton == button) {
                    button.setBackgroundResource(R.drawable.primary_background_btn)
                    selectedButton = null
                    textoHorarios.text = "Nenhum horário selecionado"
                    confirmarLocacao.isEnabled = false
                    confirmarLocacao.setBackgroundResource(R.drawable.unselected_button)
                } else {
                    selectedButton?.setBackgroundResource(R.drawable.primary_background_btn)
                    selectedButton = button
                    button.setBackgroundResource(R.drawable.selected_button)
                    textoHorarios.text = "Horário selecionado: $horario"
                    confirmarLocacao.isEnabled = true
                    confirmarLocacao.setBackgroundResource(R.drawable.selected_button)
                }
            }
        }

        confirmarLocacao.setOnClickListener {
            if (!confirmarLocacao.isEnabled) {
                Snackbar.make(it, "Por favor, selecione um horário de locação.", Snackbar.LENGTH_SHORT).show()
            } else {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setTitle("Atenção!")
                alertBuilder.setMessage("Será creditado do seu cartão o valor da locação. Deseja continuar?")
                alertBuilder.setPositiveButton("Sim") { _, _ ->
                    startActivity(Intent(this, Register2Activity::class.java))
                    verificarDisponibilidadeLocker()
                }
                alertBuilder.setNegativeButton("Não") { dialog, _ ->
                    dialog.dismiss()
                }
                alertBuilder.show()
            }
        }
    }

    private fun verificarDisponibilidadeLocker() {
        db.collection("rental_units")
            .document("lockers")
            .get()
            .addOnSuccessListener { document ->
                val lockerAvailable = document.getBoolean("lockerAvailable") ?: false
                if (lockerAvailable) {
                    startActivity(Intent(this, Register2Activity::class.java))
                    finish()
                } else {
                    Snackbar.make(
                        confirmarLocacao,
                        "Armário não está disponível.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Snackbar.make(
                    confirmarLocacao,
                    "Erro ao buscar disponibilidade do armário.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }
}



