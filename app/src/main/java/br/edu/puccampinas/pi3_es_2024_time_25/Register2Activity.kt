package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRegister2Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Register2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
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
        db = Firebase.firestore
        val packedAcc = intent.getStringExtra("packedUserInstance")

        voltar.setOnClickListener {
            startActivity(Intent(this, Register1Activity::class.java))
        }


        btn_registrar.setOnClickListener {

            val acc = completeUserInstance(packedAcc)
            sendConfirmPassToInstance(acc)

            if (Account.Validator(acc).isFormTwoValid()) {
                    acc.confirmPassword = ""
                    auth.createUserWithEmailAndPassword(acc.email, acc.senha)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                acc.uid = task.result.user?.uid
                                acc.senha = ""

                                db.collection("users").add(acc)
                                    .addOnSuccessListener {

                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnSuccessListener {
                                                    Snackbar.make(findViewById(R.id.Register2Activity),
                                                        "Te enviamos um e-mail para verificar sua conta. Você será redirecionado para o login...",
                                                        Snackbar.LENGTH_SHORT).show()

                                                CoroutineScope(Dispatchers.Main).launch {
                                                    delay(4000)

                                                    startActivity(Intent(this@Register2Activity, LoginActivity::class.java))
                                                    finish()
                                                }
                                                }
                                            }

                                    }


                            else {
                                val msg = Account.Validator(acc).exceptionHandler(task.exception)
                                Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show()
                            }
                        }


            }
        else {
                val msg = Account.Validator(acc).warnUser()
                Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show()
            }
        }


    }


    private fun completeUserInstance(packedAccount: String?): Account {

        val acc = unpackUserInstance(packedAccount)

        acc.email = email.text.toString()
        acc.senha = senha.text.toString()

        return acc
    }

    private fun sendConfirmPassToInstance(acc: Account) {

        acc.confirmPassword = confirmaSenha.text.toString()

    }

    private fun setupViewBinding(){
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun unpackUserInstance(accAsJson: String?): Account {
        return Gson().fromJson(accAsJson, Account::class.java)
    }


}

