package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.AddCreditCardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddCreditCardActivity : AppCompatActivity() {
    private lateinit var binding: AddCreditCardBinding
    private var auth: FirebaseAuth = Firebase.auth
    private var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewBinding()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()



        binding.btnAddCartao.setOnClickListener {
            val card = createCardInstance() // cria uma instancia de cartao de credito
            if (CreditCard.Validator(card).isFormValid()) { // chama as funções de validaçao do formulario
                db.collection("users").document(auth.uid.toString()).collection("creditCard").add(card) // adiciona o cartao de credito numa coleçao dentro do documento do usuario
                    .addOnSuccessListener {
                        Snackbar.make(findViewById(R.id.AddCreditCardActivity), "Cartão cadastrado com sucesso! Você será redirecionado...", Snackbar.LENGTH_SHORT).show()

                        CoroutineScope(Dispatchers.Main).launch { // coroutine utilizada para o delay nao travar a main thread
                            delay(4000) // delay entre a exibiçao da snackbar e a troca de tela
                            startActivity(Intent(this@AddCreditCardActivity, MapsActivity::class.java))
                            finish()
                        }
                    }

                    .addOnFailureListener {// msg de falha caso de errado
                        Snackbar.make(findViewById(R.id.AddCreditCardActivity), "Erro inesperado. Contate o suporte.", Snackbar.LENGTH_SHORT).show()
                    }
            }
            else {
                val msg = CreditCard.Validator(card).warnUser() // caso nao passe dos validadores de formulario, chamar a funçao que encontra a msg correta
                Snackbar.make(findViewById(R.id.AddCreditCardActivity), msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.voltarAddCard.setOnClickListener {// botao de voltar
            finish()
        }
    }

    private fun createCardInstance(): CreditCard { // funçao que instancia um cartao de credito
        return CreditCard(
            binding.etNumeroCartao.text.toString(),
            binding.etNomeTitular.text.toString(),
            binding.etVencimento.text.toString(),
            binding.etCVV.text.toString()
        )
    }

    private fun setupViewBinding(){ // setando o viewBinding
        binding = AddCreditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}