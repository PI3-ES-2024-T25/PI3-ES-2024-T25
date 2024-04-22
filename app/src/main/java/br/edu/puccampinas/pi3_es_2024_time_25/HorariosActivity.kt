package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class HorariosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var textoHorarios: TextView
    private lateinit var btnTempo1: AppCompatButton
    private lateinit var btnTempo2: AppCompatButton
    private lateinit var btnTempo3: AppCompatButton
    private lateinit var btnTempo4: AppCompatButton
    private lateinit var btnTempo5: AppCompatButton
    private lateinit var confirmarLocacao: AppCompatButton
    private lateinit var voltarTempo: Button

    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horarios)

        textoHorarios = findViewById(R.id.texto_horarios)
        confirmarLocacao = findViewById(R.id.Confirmar_locacao)
        btnTempo1 = findViewById(R.id.btn_tempo_1)
        btnTempo2 = findViewById(R.id.btn_tempo_2)
        btnTempo3 = findViewById(R.id.btn_tempo_3)
        btnTempo4 = findViewById(R.id.btn_tempo_4)
        btnTempo5 = findViewById(R.id.btn_tempo_5)
        voltarTempo = findViewById(R.id.voltar_tempo)

        voltarTempo.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        val buttons = listOf(btnTempo1, btnTempo2, btnTempo3, btnTempo4, btnTempo5)

        buttons.forEach { button ->
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
                    textoHorarios.text = "Horário selecionado: ${button.text}"


                    confirmarLocacao.isEnabled = true
                    confirmarLocacao.setBackgroundResource(R.drawable.selected_button)
                }
            }
        }

        confirmarLocacao.setOnClickListener {
            if (!confirmarLocacao.isEnabled) {
                val msg = "Por favor, selecione um horário de locação."
                Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show()
            } else {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setTitle("Atenção!")
                alertBuilder.setMessage("Será creditado do seu cartão o valor da locação. Deseja continuar?")
                alertBuilder.setPositiveButton("Sim") { dialog, which ->
                    startActivity(Intent(this, Register2Activity::class.java))
                    verificarDisponibilidadeLocker()
                }
                alertBuilder.setNegativeButton("Não") { dialog, which ->
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
                if (document.exists()) {
                    val lockerAvailable = document.getBoolean("lockerAvailable") ?: false

                    if (lockerAvailable) {

                        startActivity(Intent(this, Register2Activity::class.java))
                        finish()
                    } else {

                        Snackbar.make(
                            confirmarLocacao,
                            "Armário não está disponivel",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener { exception ->

                Snackbar.make(
                    confirmarLocacao,
                    "Erro ao buscar disponibilidade do armário.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }
}


