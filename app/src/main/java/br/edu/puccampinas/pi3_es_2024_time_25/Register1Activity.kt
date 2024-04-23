package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRegister1Binding
import com.google.android.material.snackbar.Snackbar
import com.santalu.maskara.widget.MaskEditText

class Register1Activity : AppCompatActivity() {

    lateinit var voltar: Button
    lateinit var nomeCompleto: AppCompatEditText
    lateinit var CPF: MaskEditText
    lateinit var dataNasc: MaskEditText
    lateinit var telefone: MaskEditText
    lateinit var btnContinuar: AppCompatButton
    lateinit var binding: ActivityRegister1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewBinding()

        voltar = findViewById(R.id.return_rental_options)
        nomeCompleto = findViewById(R.id.nome_registro)
        CPF = findViewById(R.id.CPF_registro)
        dataNasc = findViewById(R.id.dataNascimento_registro)
        telefone = findViewById(R.id.telefone_registro)
        btnContinuar = findViewById(R.id.btn_registro_1)

        voltar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnContinuar.setOnClickListener {
            if (preencheuCampos()) {
                startActivity(Intent(this, Register2Activity::class.java))
                finish()
            }
            else {
                val msg = campoFaltando()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun preencheuCampos(): Boolean {
        return (nomeCompleto.text.toString().isNotEmpty() && CPF.text.toString().isNotEmpty()
                && dataNasc.text.toString().isNotEmpty() && telefone.text.toString().isNotEmpty())
    }

    private fun campoFaltando(): String {
        val msg: String
        if (nomeCompleto.text.toString().isEmpty()) {
            msg = "Digite seu nome"
            return msg
        }
        if (CPF.text.toString().isEmpty()) {
            msg = "Digite seu CPF"
            return msg
        }
        if(dataNasc.text.toString().isEmpty()) {
            msg = "Digite sua data de nascimento"
            return msg
        }
        else {
            msg = "Digite seu telefone"
            return msg
        }
    }
    private fun setupViewBinding(){
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}