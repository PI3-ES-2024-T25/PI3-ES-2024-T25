package br.edu.puccampinas.pi3_es_2024_time_25

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.AddCreditCardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AddCreditCardActivity : AppCompatActivity() {
    private lateinit var binding: AddCreditCardBinding
    private var auth: FirebaseAuth = Firebase.auth
    private var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddCreditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        binding.btnAddCartao.setOnClickListener {
            val card = createCardInstance()
            if (CreditCard.Validator(card).isFormValid()) {
                db.collection("users").document(auth.currentUser!!.getUid()).collection("credit_cards")
                    .add(card)
                    .addOnSuccessListener {
                        Snackbar.make(
                            findViewById(R.id.AddCreditCardActivity),
                            "Cartão cadastrado com sucesso!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Log.i("teste: ", "deu errado")
                        Snackbar.make(
                            findViewById(R.id.AddCreditCardActivity),
                            "Erro inesperado. Contate o suporte.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
            } else {
                val msg = CreditCard.Validator(card).warnUser()
                Snackbar.make(findViewById(R.id.AddCreditCardActivity), msg, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        binding.voltarAddCard.setOnClickListener {
            finish()
        }
    }

    private fun createCardInstance(): CreditCard {
        return CreditCard(
            binding.etNumeroCartao.text.toString(),
            binding.etNomeTitular.text.toString(),
            binding.etVencimento.text.toString(),
            binding.etCVV.text.toString()
        )
    }
}