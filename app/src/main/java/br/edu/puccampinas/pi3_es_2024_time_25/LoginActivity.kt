package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: AppCompatEditText
    private lateinit var senha: AppCompatEditText
    private lateinit var esqueceuSenha: AppCompatTextView
    private lateinit var btn_login: AppCompatButton
    private lateinit var criarConta: AppCompatTextView
    private lateinit var localArmarios : TextView
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
       val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            //ainda nao tem a pagina de dentro do app, entao ta indo pra main
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        email = findViewById(R.id.email_login)
        senha = findViewById(R.id.senha_login)
        btn_login = findViewById(R.id.btn_login)
        criarConta = findViewById(R.id.registrar_login)
        esqueceuSenha = findViewById(R.id.esqueceu_senha)
        localArmarios = findViewById(R.id.location_armarios_login)

        criarConta.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }

        esqueceuSenha.setOnClickListener{
            startActivity(Intent(this, RecoveryActivity::class.java))
        }



        btn_login.setOnClickListener {

            if (preencheuCampos()) {
                auth.signInWithEmailAndPassword(email.text.toString(), senha.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val contaVerificada = auth.currentUser?.isEmailVerified
                            if (contaVerificada == true) {

                                Snackbar.make(findViewById(R.id.LoginActivity), "Entrando...", Snackbar.LENGTH_SHORT).show()

                                //ainda nao tem a pagina de dentro do app, entao o login ta indo pra main
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Snackbar.make(findViewById(R.id.LoginActivity), "Sua conta não foi verificada. Cheque seu e-mail.", Snackbar.LENGTH_SHORT).show()

                            }
                        } else {
                            Snackbar.make(findViewById(R.id.LoginActivity), "E-mail ou senha inválidos.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }

            else {
                val msg = campoFaltando()
                Snackbar.make(findViewById(R.id.LoginActivity), msg, Snackbar.LENGTH_SHORT).show()

            }
        }

        localArmarios.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun preencheuCampos(): Boolean {
        return (email.text.toString().isNotEmpty() && senha.text.toString().isNotEmpty())
    }

    private fun campoFaltando(): String {
        var msg = "Digite sua senha"

        if(email.text.toString().isEmpty()) {
            msg = "Digite seu e-mail"
        }
        return msg
    }



}
