package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRegister2Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Register2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var voltar: Button
    private lateinit var email: AppCompatEditText
    private lateinit var senha: AppCompatEditText
    private lateinit var confirmaSenha: AppCompatEditText
    private lateinit var btn_registrar: AppCompatButton
    private lateinit var binding: ActivityRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewBinding()

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
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    baseContext,
                                    "Registro realizado. Favor verificar seu e-mail!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(
                            baseContext,
                            "Não foi possível realizar o registro.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }

    }

    private fun setupViewBinding(){
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}