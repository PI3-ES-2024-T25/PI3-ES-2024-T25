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
        }

        btnContinuar.setOnClickListener {
            if (preencheuCampos() && camposValidos()) {
                startActivity(Intent(this, Register2Activity::class.java))
                finish()
            } else {
                val msg = avisaUsuario()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun preencheuCampos(): Boolean {
        return (nomeCompleto.text.toString().isNotEmpty() && CPF.text.toString().isNotEmpty()
                && dataNasc.text.toString().isNotEmpty() && telefone.text.toString().isNotEmpty())
    }


    private fun camposValidos(): Boolean {
        return (CPF.isDone && dataNasc.isDone && telefone.isDone)
    }

    private fun avisaUsuario(): String {
        var msg = ""
        if (!preencheuCampos()) {
            msg = "Preencha todos os campos."
        } else {
            val listaCampos = listOf<MaskEditText>(CPF, dataNasc, telefone)
            val listaMsg = listOf<String>(
                "CPF inválido.",
                "Data de nascimento inválida.",
                "Telefone inválido."
            )
            for ((i, campo) in listaCampos.withIndex()) {
                if (!campo.isDone) {
                    msg = listaMsg[i]
                    break

                }
            }
        }
        return msg
    }
}