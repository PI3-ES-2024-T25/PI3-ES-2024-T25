package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    public override fun onStart() {
        super.onStart()
       val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MapsActivity::class.java))
            finish()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding()

        auth = Firebase.auth

        binding.registrarLogin.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }

        binding.esqueceuSenha.setOnClickListener{
            startActivity(Intent(this, RecoveryActivity::class.java))
        }



        binding.btnLogin.setOnClickListener {

            if (isFormFilledOut()) {
                auth.signInWithEmailAndPassword(binding.emailLogin.text.toString(), binding.senhaLogin.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val contaVerificada = auth.currentUser?.isEmailVerified
                            if (contaVerificada == true) {

                                Snackbar.make(binding.root, "Entrando...", Snackbar.LENGTH_SHORT).show()

                                startActivity(Intent(this, MapsActivity::class.java))
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
                val msg = warnUser()
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()

            }
        }

        binding.locationArmariosLogin.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun isFormFilledOut(): Boolean {
        return (binding.emailLogin.text.toString().isNotEmpty() && binding.senhaLogin.text.toString().isNotEmpty())
    }

    private fun warnUser(): String {
        var msg = "Digite sua senha"

        if(binding.emailLogin.text.toString().isEmpty()) {
            msg = "Digite seu e-mail"
        }
        return msg
    }

    private fun setupViewBinding(){
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
