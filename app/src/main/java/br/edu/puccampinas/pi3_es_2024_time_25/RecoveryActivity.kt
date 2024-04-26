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
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRecoveryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class RecoveryActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewBinding()

        auth = FirebaseAuth.getInstance()

        binding.voltarRecovery.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnRecovery.setOnClickListener {

            if (binding.emailRecovery.text.toString().isNotEmpty()) {
                auth.sendPasswordResetEmail(binding.emailRecovery.text.toString())

                    .addOnSuccessListener {
                        showPopUp()
                    }

                    .addOnFailureListener {
                        Snackbar.make(findViewById(R.id.RecoveryActivity), "E-mail inválido.", Snackbar.LENGTH_SHORT).show()

                    }

            } else {
                Snackbar.make(findViewById(R.id.RecoveryActivity), "Digite seu e-mail.", Snackbar.LENGTH_SHORT).show()

            }
        }

    }

    private fun showPopUp() {
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

    private fun setupViewBinding(){
        binding = ActivityRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}






