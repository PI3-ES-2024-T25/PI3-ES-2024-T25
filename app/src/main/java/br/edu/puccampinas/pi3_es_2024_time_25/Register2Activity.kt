package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView

class Register2Activity : AppCompatActivity() {

    lateinit var voltar: AppCompatImageButton
    lateinit var email: AppCompatEditText
    lateinit var senha: AppCompatEditText
    lateinit var confirmaSenha: AppCompatEditText
    lateinit var btn_registrar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        voltar = findViewById(R.id.voltar_registro2)
        email = findViewById(R.id.email_registro)
        senha = findViewById(R.id.senha_registro)
        confirmaSenha = findViewById(R.id.confirmaSenha_registro)
        btn_registrar = findViewById(R.id.btn_registro_2)


        voltar.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }

    }


}