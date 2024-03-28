package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    lateinit var email: AppCompatEditText
    lateinit var senha: AppCompatEditText
    //lateinit var esqueceuSenha
    lateinit var btn_login: AppCompatButton
    lateinit var criarConta: AppCompatTextView
    //lateinit var localArmarios
    lateinit var auth: FirebaseAuth

    // função comentada pq nao estava funcionando direito. arrumar
//    public override fun onStart() {
//        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // se o usuario ja estiver logado, redireciona ele pra dentro do app
//            //ainda nao tem a pagina de dentro do app, entao ta indo pra main
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        email = findViewById(R.id.email_login)
        senha = findViewById(R.id.senha_login)
        btn_login = findViewById(R.id.btn_login)
        criarConta = findViewById(R.id.registrar_login)

        criarConta.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }

        btn_login.setOnClickListener{
            auth.signInWithEmailAndPassword(email.text.toString(), senha.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Login bem-sucedido!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        //ainda nao tem a pagina de dentro do app, entao o login ta indo pra main
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    } else {

                        Toast.makeText(
                            baseContext,
                            "E-mail ou senha incorreto(s).",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}
