package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton

class Register1Activity : AppCompatActivity() {

    lateinit var voltar: AppCompatImageButton
    lateinit var nomeCompleto: AppCompatEditText
    lateinit var CPF: AppCompatEditText
    lateinit var dataNasc: AppCompatEditText
    lateinit var telefone: AppCompatEditText
    lateinit var btnContinuar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register1);

        voltar = findViewById(R.id.voltar_registro1)
        nomeCompleto = findViewById(R.id.nome_registro)
        CPF = findViewById(R.id.CPF_registro)
        dataNasc = findViewById(R.id.dataNascimento_registro)
        telefone = findViewById(R.id.telefone_registro)
        btnContinuar = findViewById(R.id.btn_registro_1)

        voltar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnContinuar.setOnClickListener {
            startActivity(Intent(this, Register2Activity::class.java))
        }


    }
}