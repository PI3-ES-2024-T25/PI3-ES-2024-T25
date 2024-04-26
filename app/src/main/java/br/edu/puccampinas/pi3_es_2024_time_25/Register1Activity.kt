package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRegister1Binding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class Register1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegister1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding()
        
        binding.voltarRegistro1.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }

        binding.btnRegistro1.setOnClickListener {
            val acc = startUserInstance() // inicia uma instancia de conta
            val packedAcc = packUserInstance(acc) // transforma essa instancia em json para envia-la à outra activity de registro, onde será completa
            if (Account.Validator(acc).isFormOneValid()) { // chama o validador de formulario
                startActivity(Intent(this, Register2Activity::class.java)
                        .putExtra("packedUserInstance", packedAcc) // caso seja valido, trocar de activity e enviar o json
                )

            }
            else { // caso nao seja valido, exibir a respectiva msg
                val msg = Account.Validator(acc).warnUser()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun startUserInstance(): Account { // inicia a instancia do usuario com os campos presentes na tela desta activity

        return Account(
            binding.nomeRegistro.text.toString(),
            binding.CPFRegistro.text.toString(),
            binding.dataNascimentoRegistro.text.toString(),
            binding.telefoneRegistro.text.toString(),
            "",
            "",)

    }

    private fun setupViewBinding(){ // inicia o viewbinding
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun packUserInstance(acc: Account): String { // transforma a instancia iniciada em json
        return Gson().toJson(acc)
    }
}