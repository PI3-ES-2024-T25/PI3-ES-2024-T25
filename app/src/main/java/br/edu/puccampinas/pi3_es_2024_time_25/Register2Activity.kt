package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Register2Activity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
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
        auth = Firebase.auth

        voltar.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }



        btn_registrar.setOnClickListener{
            auth.createUserWithEmailAndPassword(email.text.toString(), senha.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("status", "função de criar conta com e-mail e senha executada com sucesso")
                        Toast.makeText(
                            baseContext,
                            "Registro realizado com sucesso!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()

                    } else {
                        Log.w("status", "função de criar conta com e-mail e senha falhou", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Não foi possível realizar o registro.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }

    }


}