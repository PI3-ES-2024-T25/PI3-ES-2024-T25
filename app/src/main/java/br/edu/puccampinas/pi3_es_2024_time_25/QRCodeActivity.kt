package br.edu.puccampinas.pi3_es_2024_time_25

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class QRCodeActivity : AppCompatActivity() {
    private var db : FirebaseFirestore = Firebase.firestore
    private var auth : FirebaseAuth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }


    fun storeQRCode(idMask : String){

        val data = hashMapOf(
            "idMask" to idMask
        )


        db.collection("qr_codes")
            .add(data)
            .addOnSuccessListener { documentReference -> println("Código do QR Code armazenado com sucesso") }
            .addOnFailureListener{ e -> println("Falha ao adicionar o código do QR Code : $e") }
    }

    fun getAllQRCodeData() {
        val qrCodesCollectionData = db.collection("qr_codes")

        qrCodesCollectionData.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach { document ->
                        val data = document.data
                        println("Dados do documento:")
                        data.forEach { (key, value) ->
                            println("$key: $value")
                        }
                    }
                } else {
                    println("Nenhum documento foi encontrado")
                }
            }
            .addOnFailureListener { e -> println("Erro ao recuperar dados do banco: $e") }
    }


    fun getOneQRCode(idMask: String){

        val oneQRCodeData = db.collection("qr_codes").whereEqualTo("idMask", idMask)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Verificar se há documentos correspondentes à consulta
                if (!querySnapshot.isEmpty) {
                    // Iterar sobre os documentos retornados
                    for (document in querySnapshot.documents) {
                        // Obter os dados de cada documento
                        val data = document.data
                        println("Dados do documento:")
                        if (data != null) {
                            for ((key, value) in data) {
                                println("$key: $value")
                            }
                        }
                    }
                } else {
                    println("Nenhum documento encontrado com o ID Mask: $idMask")
                }
            }
            .addOnFailureListener { e ->
                // Falha ao realizar a consulta
                println("Erro ao consultar documentos: $e")
            }
    }
}