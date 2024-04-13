package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.santalu.maskara.widget.MaskEditText

class Register1Activity : AppCompatActivity() {

    private lateinit var voltar: Button
    private lateinit var nomeCompleto: AppCompatEditText
    private lateinit var CPF: MaskEditText
    private lateinit var dataNasc: MaskEditText
    private lateinit var telefone: MaskEditText
    private lateinit var btnContinuar: AppCompatButton

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
            val acc = startUserInstance(null)
            val packedAcc = packUserInstance(acc)
            if (Account.Validator(acc).isFormOneValid()) {
                startActivity(
                    Intent(this, Register2Activity::class.java)
                        .putExtra("packedUserInstance", packedAcc)
                )

            }
            else {
                val msg = Account.Validator(acc).warnUser()
                Snackbar.make(findViewById(R.id.Register1Activity), msg, Snackbar.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun startUserInstance(uid: String?): Account {

        return Account(uid,
            nomeCompleto.text.toString(),
            CPF.text.toString(),
            dataNasc.text.toString(),
            telefone.text.toString(),
            "",
            "",)

    }

    private fun packUserInstance(acc: Account): String {
        return Gson().toJson(acc)
    }
}



