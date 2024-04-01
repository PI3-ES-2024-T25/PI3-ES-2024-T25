package br.edu.puccampinas.pi3_es_2024_time_25

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.FirebaseAuth

class RecoveryActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var voltar: AppCompatImageButton
    private lateinit var email: AppCompatEditText
    private lateinit var btnRecuperar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        auth = FirebaseAuth.getInstance()
        voltar = findViewById(R.id.voltar_recovery)
        email = findViewById(R.id.email_recovery)
        btnRecuperar = findViewById(R.id.btn_recovery)

        voltar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRecuperar.setOnClickListener {

            auth.sendPasswordResetEmail(email.text.toString())

                .addOnSuccessListener {
                    abrirpopUp()
                }

                .addOnFailureListener {
                    Toast.makeText(baseContext, "E-mail inválido.", Toast.LENGTH_SHORT).show()

                }

        }
    }

    private fun abrirpopUp() {
        val popUp = Dialog(this)
        popUp.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popUp.setContentView(R.layout.modal_recovery)

        val btnOk: Button = popUp.findViewById(R.id.btn_ok_recovery)

        btnOk.setOnClickListener {
            popUp.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        popUp.show()
    }
}






