package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView

class LoginActivity : AppCompatActivity() {

    lateinit var email: AppCompatEditText
    lateinit var senha: AppCompatEditText
    //lateinit var esqueceuSenha
    lateinit var btn_login: AppCompatButton
    lateinit var criarConta: AppCompatTextView
    //lateinit var localArmarios
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email_login)
        senha = findViewById(R.id.senha_login)
        btn_login = findViewById(R.id.btn_login)
        criarConta = findViewById(R.id.registrar_login)

        criarConta.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }
    }
}
