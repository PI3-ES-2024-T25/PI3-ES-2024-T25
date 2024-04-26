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
            val acc = startUserInstance()
            val packedAcc = packUserInstance(acc)
            if (Account.Validator(acc).isFormOneValid()) {
                startActivity(Intent(this, Register2Activity::class.java)
                        .putExtra("packedUserInstance", packedAcc)
                )

            }
            else {
                val msg = Account.Validator(acc).warnUser()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun startUserInstance(): Account {

        return Account(
            binding.nomeRegistro.text.toString(),
            binding.CPFRegistro.text.toString(),
            binding.dataNascimentoRegistro.text.toString(),
            binding.telefoneRegistro.text.toString(),
            "",
            "",)

    }

    private fun setupViewBinding(){
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun packUserInstance(acc: Account): String {
        return Gson().toJson(acc)
    }
}