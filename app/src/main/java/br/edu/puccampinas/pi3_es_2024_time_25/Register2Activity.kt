package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.lang.Exception

class Register2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var voltar: Button
    private lateinit var nome: String
    private lateinit var cpf: String
    private lateinit var dNascimento: String
    private lateinit var telefone: String
    private lateinit var email: AppCompatEditText
    private lateinit var senha: AppCompatEditText
    private lateinit var confirmaSenha: AppCompatEditText
    private lateinit var btn_registrar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)


        voltar = findViewById(R.id.voltar_registro2)
        email = findViewById(R.id.email_registro)
        senha = findViewById(R.id.senha_registro)
        confirmaSenha = findViewById(R.id.confirmaSenha_registro)
        btn_registrar = findViewById(R.id.btn_registro_2)
        auth = Firebase.auth
        db = Firebase.firestore

        val dadosForm1 = recebeDados()
        nome = dadosForm1?.get(0).toString()
        cpf = dadosForm1?.get(1).toString()
        dNascimento = dadosForm1?.get(2).toString()
        telefone = dadosForm1?.get(3).toString()

        voltar.setOnClickListener {
            startActivity(Intent(this, Register1Activity::class.java))
        }



        btn_registrar.setOnClickListener {
            if (preencheuForm2() && confereSenha() && tamanhoSenha()) {
                    val account = criaUsuario(null)
                    auth.createUserWithEmailAndPassword(account.email, account.senha)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                account.uid = task.result.user?.uid
                                account.senha = ""

                                db.collection("users").add(account)
                                    .addOnSuccessListener {
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnSuccessListener {
                                                Snackbar.make(findViewById(R.id.Register2Activity), "Registro realizado!Te enviamos um e-mail para verificar sua conta.", Snackbar.LENGTH_SHORT).show()
//                                                startActivity(Intent(this, LoginActivity::class.java))
//                                                finish()
                                            }

                                    }


                            }
                            else {
                                val msg = trataExcecao(task.exception)
                                Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show()
                            }
                        }


            }
        else {
                val msg = avisaUsuario()
                Snackbar.make(
                    findViewById(R.id.Register2Activity),
                    msg,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }


    }


    private fun criaUsuario(uid: String?): Account {
        val a = Account(
            uid,
            nome,
            cpf,
            dNascimento,
            telefone,
            email.text.toString(),
            confirmaSenha.text.toString()
        )
        val confirmacao = a.senha
        a.senha = senha.text.toString()

        return  a // NAO TA PRONTO
    }


    private fun recebeDados(): Array<String>? {
        return intent.getStringArrayExtra("vetorDados")
    }

    private fun preencheuForm2(): Boolean {
        return (email.text.toString().isNotEmpty() && senha.text.toString()
            .isNotEmpty() && confirmaSenha.text.toString().isNotEmpty())

    }

    private fun tamanhoSenha(): Boolean {
        return senha.text.toString().length >= 8
    }

    private fun confereSenha(): Boolean {
        return (senha.text.toString() == confirmaSenha.text.toString())
    }

    private fun avisaUsuario(): String {
        return when {
            !preencheuForm2() -> "Preencha todos os campos."
            !confereSenha() -> "As senhas digitadas não são iguais"
            else -> "A senha deve ter no mínimo 8 dígitos."
        }
    }

    private fun trataExcecao(e: Exception?): String {
        return when(e) {
            is FirebaseAuthUserCollisionException -> "O e-mail inserido já foi registrado."
            is FirebaseNetworkException -> return "Sem conexão com a Internet."
            else -> return "Ocorreu um erro. Contate o suporte."
        }
    }
}

