package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class HorariosActivity : AppCompatActivity() {

    private lateinit var textoHorarios: TextView
    private lateinit var btnTempo1: Button
    private lateinit var btnTempo2: Button
    private lateinit var btnTempo3: Button
    private lateinit var btnTempo4: Button
    private lateinit var btnTempo5: Button
    private lateinit var confirmarLocacao: Button
    private lateinit var voltarTempo: Button

    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horarios)

        textoHorarios = findViewById(R.id.texto_horarios)
        btnTempo1 = findViewById(R.id.btn_tempo_1)
        btnTempo2 = findViewById(R.id.btn_tempo_2)
        btnTempo3 = findViewById(R.id.btn_tempo_3)
        btnTempo4 = findViewById(R.id.btn_tempo_4)
        btnTempo5 = findViewById(R.id.btn_tempo_5)
        confirmarLocacao = findViewById(R.id.Confirmar_locacao)
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

                    selectedButton?.setBackgroundResource(R.color.white)
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

                startActivity(Intent(this, Register2Activity::class.java))
                finish()
            }
        }
    }
}

