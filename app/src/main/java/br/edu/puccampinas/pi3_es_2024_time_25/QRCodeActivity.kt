package br.edu.puccampinas.pi3_es_2024_time_25

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class QRCodeActivity : AppCompatActivity() {
    private var db : FirebaseFirestore = Firebase.firestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = FirebaseFirestore.getInstance()


    }

    fun storeQRCodeFirestore(idMask : String){

        val data = hashMapOf(
            "idMask" to idMask
        )


        db.collection("qr_codes")
            .add(data)
            .addOnSuccessListener { println("Código do QR Code armazenado com sucesso") }
            .addOnFailureListener{ e -> println("Falha ao adicionar o código do QR Code : $e") }
    }

    fun getAllQRCodeDataFirestore() {
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


    fun getOneQRCodeFirestore(idMask: String){

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

    suspend fun storeQRCodeLocal(context: Context, idMask: String){
        withContext(Dispatchers.IO){
            val sharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("idMask", idMask)
            editor.apply()
            }
        }

    suspend fun getQRCodeLocal(context: Context) : String? {
        return withContext(Dispatchers.IO){
            val sharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
            sharedPreferences.getString("idMask", null)
        }
    }

    suspend fun deleteQRCodeLocal(context: Context){
        withContext(Dispatchers.IO){
            val sharedPreferences = context.getSharedPreferences("MySharedPreferences",Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("idMask")
            editor.apply()
        }
    }
}