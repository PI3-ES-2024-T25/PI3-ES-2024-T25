package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityLoginBinding
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
    private lateinit var binding: ActivityLoginBinding

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
        setupViewBinding()

        auth = Firebase.auth
        email = binding.emailLogin as AppCompatEditText
        senha = binding.senhaLogin as AppCompatEditText
        btn_login = binding.btnLogin as AppCompatButton
        criarConta = binding.registrarLogin as AppCompatTextView
        esqueceuSenha = binding.esqueceuSenha as AppCompatTextView
        localArmarios = binding.locationArmariosLogin as AppCompatTextView

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

                                Snackbar.make(binding.root, "Entrando...", Snackbar.LENGTH_SHORT).show()

                                //ainda nao tem a pagina de dentro do app, entao o login ta indo pra main
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Snackbar.make(binding.root, "Sua conta não foi verificada. Cheque seu e-mail.", Snackbar.LENGTH_SHORT).show()

                            }
                        } else {
                            Snackbar.make(binding.root, "E-mail ou senha inválidos.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }

            else {
                val msg = campoFaltando()
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()

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

    private fun setupViewBinding(){
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
