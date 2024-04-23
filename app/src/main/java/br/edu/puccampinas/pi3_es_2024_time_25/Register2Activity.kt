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
    private lateinit var packedAcc: String
    private lateinit var binding: ActivityRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding()

        auth = Firebase.auth
        db = Firebase.firestore
        packedAcc = intent.getStringExtra("packedUserInstance").toString()

        binding.voltarRegistro2.setOnClickListener {
            startActivity(Intent(this, Register1Activity::class.java))
        }


        binding.btnRegistro2.setOnClickListener {

            val acc = completeUserInstance(packedAcc)
            sendConfirmPassToInstance(acc)

            if (Account.Validator(acc).isFormTwoValid()) {
                    acc.confirmPassword = ""
                    auth.createUserWithEmailAndPassword(acc.email, acc.senha)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                val docTag = task.result.user?.uid
                                acc.senha = ""

                                db.collection("users").document(docTag!!).set(acc)
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

        acc.email = binding.emailRegistro.text.toString()
        acc.senha = binding.senhaRegistro.text.toString()

        return acc
    }

    private fun sendConfirmPassToInstance(acc: Account) {

        acc.confirmPassword = binding.confirmaSenhaRegistro.text.toString()

    }

    private fun setupViewBinding(){
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun unpackUserInstance(accAsJson: String?): Account {
        return Gson().fromJson(accAsJson, Account::class.java)
    }


}

