package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class LoginActivity : AppCompatActivity() {
  
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityLoginBinding

    public override fun onStart() {
        super.onStart()
        if (auth.currentUser!=null) {
            goToAccount()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding()

        auth = Firebase.auth
        db = Firebase.firestore

        binding.registrarLogin.setOnClickListener{
            startActivity(Intent(this, Register1Activity::class.java))
        }

        binding.esqueceuSenha.setOnClickListener{
            startActivity(Intent(this, RecoveryActivity::class.java))
        }



        binding.btnLogin.setOnClickListener {

            if (isFormFilledOut()) { // valida se os campos de login estao preenchidos
                auth.signInWithEmailAndPassword(binding.emailLogin.text.toString(), binding.senhaLogin.text.toString()) // realizar o login
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            goToAccount()

                        }
                        else { // se o login nao for realizado com os campos preenchidos, informar que os campos sao invalidos
                            Snackbar.make(
                                binding.root,
                                "E-mail ou senha inválidos.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            else { // caso nao passe dos validadores, exibir a msg correta ao usuario
                val msg = warnUser()
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()

            }
        }
        binding.locationArmariosLogin.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun isFormFilledOut(): Boolean { // verifica se o formulario esta preenchido
        return (binding.emailLogin.text.toString().isNotEmpty() && binding.senhaLogin.text.toString().isNotEmpty())
    }

    private fun warnUser(): String { // encontra a msg correta para o usuario em caso de erro
        var msg = "Digite sua senha"

        if(binding.emailLogin.text.toString().isEmpty()) {
            msg = "Digite seu e-mail"
        }
        return msg
    }

    private fun goToAccount() {
        db.collection("managers").document(auth.uid.toString()).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {

                    startActivity(Intent(this, ManagerMainActivity::class.java))
                }

                else {
                    val contaVerificada = auth.currentUser?.isEmailVerified

                    if (contaVerificada == true) { // caso a conta do usuario seja verificada, prosseguir

                        Snackbar.make(binding.root, "Entrando...", Snackbar.LENGTH_SHORT).show()

                        startActivity(Intent(this, MapsActivity::class.java))
                        finish()
                    }

                    else {
                        Snackbar.make(
                            binding.root,
                            "Sua conta não foi verificada. Cheque seu e-mail.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }



    private fun setupViewBinding() { // inicia o viewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
