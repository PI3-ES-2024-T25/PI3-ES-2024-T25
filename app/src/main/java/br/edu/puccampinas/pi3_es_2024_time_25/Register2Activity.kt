package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRegister2Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Register2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var packedAcc: String
    private lateinit var binding: ActivityRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding() //inicializando viewBinding

        auth = Firebase.auth // instanciando o Firebase Authenticator
        db = Firebase.firestore // instanciando o Firebase Firestore
        packedAcc = intent.getStringExtra("packedUserInstance").toString() // Recebendo a instância de Account da Activity de Registro 1

        binding.voltarRegistro2.setOnClickListener {
            startActivity(Intent(this, Register1Activity::class.java))
        }


        binding.btnRegistro2.setOnClickListener {
            val acc = completeUserInstance(packedAcc) // instancia uma Account
            sendConfirmPassToInstance(acc) // envia o valor da senha de confirmação para dentro da instância
            println("Email ${acc.email} || Senha ${acc.senha} || Confirmaçao ${acc.confirmPassword}")
            if (Account.Validator(acc).isFormTwoValid()) { // chama as funções de validação

                createAccount(acc) // chama a função que inicia e finaliza (caso não ocorra nenhum erro) os procedimentos de criação da conta

            }

            else { // caso não tenha passado pelos validadores

            val msg = Account.Validator(acc).warnUser() // variável recebe uma mensagem para mostrar ao usuário através de uma função
            Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show() // exibe a mensagem ao usuário
            }
    }


    }

    private fun createAccount(acc: Account) { // função que inicia o fluxo de criação da conta do usuário

        auth.createUserWithEmailAndPassword(acc.email, acc.senha) // função do Firebase para criar conta
            .addOnCompleteListener(this) { task -> // adicionando um listener que nos informará o status após a conclusão do processo
                if (task.isSuccessful) { // se a criação da conta for bem sucedida
                    acc.senha = "" // atualiza os campos de senha para não irem ao banco
                    acc.confirmPassword = "" // atualiza os campos de senha para não irem ao banco
                    sendEmail(acc) // inicia a função de enviar e-mail de confirmação ao usuário
                    println("Conta criada para o e-mail ${acc.email}")
                }
                else { // caso a conta não seja criada
                    println("Erro ao criar conta: ${task.exception}")
                    val msg = Account.Validator(acc).exceptionHandler(task.exception) // recebe mensagem vindo da função que trata exceções
                    Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show() // exibe a msg ao usuário
                }

            }
    }


    private fun sendEmail(acc: Account) {
        auth.currentUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    saveUserData(acc)
                    println("E-mail de confimação enviado para ${acc.email}")
                }
                else {
                    println("Erro ao enviar e-mail de confirmação: ${task.exception}")
                    val msg = Account.Validator(acc).exceptionHandler(task.exception)
                    Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show()
                }
            }
    }


    private fun saveUserData(acc: Account) {
        db.collection("users").document(auth.currentUser!!.uid).set(acc)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    println("Dados de ${acc.nome} salvos.")
                    Snackbar.make(findViewById(R.id.Register2Activity), "Te enviamos um e-mail para verificar sua conta. Você será redirecionado para o login...", Snackbar.LENGTH_SHORT).show()

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(4000)
                        startActivity(Intent(this@Register2Activity, LoginActivity::class.java))
                        finish()
                    }
                }
                else {
                    println("Erro ao salvar os dados no Firestore: ${task.exception} ")
                    val msg = Account.Validator(acc).exceptionHandler(task.exception)
                    Snackbar.make(findViewById(R.id.Register2Activity), msg, Snackbar.LENGTH_SHORT).show()

                }
            }
    }

    private fun completeUserInstance(packedAccount: String?): Account {

        val acc = unpackUserInstance(packedAccount)

        acc.email = binding.emailRegistro.text.toString()
        acc.senha = binding.senhaRegistro.text.toString()

        return acc
    }

    private fun sendConfirmPassToInstance(acc: Account) {

        acc.confirmPassword = binding.confirmaSenhaRegistro.text.toString()

    }

    private fun setupViewBinding(){
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun unpackUserInstance(accAsJson: String?): Account {
        return Gson().fromJson(accAsJson, Account::class.java)
    }


}

