package br.edu.puccampinas.pi3_es_2024_time_25

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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

            if (binding.emailRecovery.text.toString().isNotEmpty()) { // se o campo de email nao estiver vazio
                auth.sendPasswordResetEmail(binding.emailRecovery.text.toString()) // enviar email de recuperaçao de senha

                    .addOnSuccessListener {
                        showPopUp() // caso seja executado com sucesso, abrir o popUp informando ao usuário que ele receberá um email
                    }

                    .addOnFailureListener {
                        Snackbar.make(findViewById(R.id.RecoveryActivity), "E-mail inválido.", Snackbar.LENGTH_SHORT).show()

                    }

            } else { // caso o campo de email esteja vazio
                Snackbar.make(findViewById(R.id.RecoveryActivity), "Digite seu e-mail.", Snackbar.LENGTH_SHORT).show()

            }
        }

    }

    private fun showPopUp() { // funçao que cria e exibe um popup
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

    private fun setupViewBinding(){ // inicia o viewbinding
        binding = ActivityRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}






