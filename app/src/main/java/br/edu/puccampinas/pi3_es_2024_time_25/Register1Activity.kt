package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.material.snackbar.Snackbar
import com.santalu.maskara.widget.MaskEditText
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class Register1Activity : AppCompatActivity() {

    lateinit var voltar: Button
    lateinit var nomeCompleto: AppCompatEditText
    lateinit var CPF: MaskEditText
    lateinit var dataNasc: MaskEditText
    lateinit var telefone: MaskEditText
    lateinit var btnContinuar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register1);

        voltar = findViewById(R.id.voltar_registro1)
        nomeCompleto = findViewById(R.id.nome_registro)
        CPF = findViewById(R.id.CPF_registro)
        dataNasc = findViewById(R.id.dataNascimento_registro)
        telefone = findViewById(R.id.telefone_registro)
        btnContinuar = findViewById(R.id.btn_registro_1)

        voltar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }

        btnContinuar.setOnClickListener {
            if (preencheuCampos() && camposValidos()) {
                val dados = empacotaDados()
                startActivity(Intent(this, Register2Activity::class.java)
                    .putExtra("vetorDados", dados))


            } else {
                val msg = avisaUsuario()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT).show()
            }
        }

    }


    private fun preencheuCampos(): Boolean {
        return (nomeCompleto.text.toString().isNotEmpty() && CPF.text.toString().isNotEmpty()
                && dataNasc.text.toString().isNotEmpty() && telefone.text.toString().isNotEmpty())
    }

    private fun maiorIdade(): Boolean {
        val dataAtual = LocalDate.now()
        val nascimentoUserString = dataNasc.text.toString()
        val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val nascimentoUser = LocalDate.parse(nascimentoUserString, formatoData)

        val idade = Period.between(nascimentoUser, dataAtual).years

        return idade>=18
    }


    private fun camposValidos(): Boolean {
        return (CPF.isDone && dataNasc.isDone && maiorIdade() && telefone.isDone)
    }

    private fun avisaUsuario(): String {
        return when {
            !preencheuCampos() -> "Preencha todos os campos."
            !CPF.isDone -> "CPF inválido."
            !dataNasc.isDone -> "Data de nascimento inválida."
            !maiorIdade() -> "Você deve ter mais que 18 anos para criar uma conta."
            else -> "Telefone inválido."
        }
    }

    private fun empacotaDados(): Array<String> {
        return arrayOf(nomeCompleto.text.toString(), CPF.text.toString(), dataNasc.text.toString(), telefone.text.toString())
    }
    }