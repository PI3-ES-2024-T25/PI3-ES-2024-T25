package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityWriteTagBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class WriteTagActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteTagBinding
    private lateinit var firstCustomerPhoto: String
    private lateinit var secondCustomerPhoto: String
    private var hasManagerWriteOnTag: Boolean = false
    private lateinit var rentDocumentId: String
    private lateinit var numberOfCustomers: String

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWriteTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        getIntentInfo()
    }

    private fun getIntentInfo() {
        val uriString = intent.getStringExtra("IMAGE_URI")
        val secondUriString = intent.getStringExtra("SECOND_IMAGE_URI")
        numberOfCustomers = intent.getStringExtra("COUNTER").toString()
        rentDocumentId = intent.getStringExtra("RENT_DOCUMENT_ID").toString()

        if (numberOfCustomers == "oneOfTwo") {
            binding.btnFinishOrNextPerson.text = "Próxima pessoa"
            binding.btnFinishOrNextPerson.isEnabled = false
        } else {
            binding.btnFinishOrNextPerson.text = "Finalizar"
            binding.btnFinishOrNextPerson.isEnabled = false
        }

        if (uriString != null) {
            firstCustomerPhoto = uriString
        }
        if (secondUriString != null) {
            secondCustomerPhoto = secondUriString
        }
        // TODO("Apagar isto foi somente para teste de trava do botão")
        binding.logoLockngo.setOnClickListener {
            activateButtonFinishAfterWriteOnTag()
        }
        initializeFinishButton()
    }

    private fun initializeFinishButton() {
        binding.btnFinishOrNextPerson.setOnClickListener {
            when (numberOfCustomers) {
                "oneOfOne" -> {
                    saveImageOnStorage(firstCustomerPhoto)
                    updateRentWithCustomerImages(1)
                    Log.d("WriteTagActivity", "uma pessoa ${firstCustomerPhoto}")
                }

                "twoOfTwo" -> {
                    updateRentWithCustomerImages(2)
                    Log.d(
                        "WriteTagActivity",
                        " duas pessoas${firstCustomerPhoto} ${secondCustomerPhoto}"
                    )
                }

                else -> {
                    val intent = Intent(this, CameraPreviewActivity::class.java)
                    intent.putExtra("COUNTER", "twoOfTwo")
                    intent.putExtra("IMAGE_URI", firstCustomerPhoto)
                    intent.putExtra("RENT_DOCUMENT_ID", rentDocumentId)
                    startActivity(intent)
                }
            }
        }
    }

    private fun saveImageOnStorage(imageUri: String) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${imageUri.toUri().lastPathSegment}")
        val uploadTask = imageRef.putFile(Uri.parse(imageUri))

        uploadTask.addOnFailureListener {
            Log.e("WriteTagActivity", "Falha ao salvar a imagem no storage")
        }.addOnSuccessListener {

            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.d("WriteTagActivity", "IMG salva ${downloadUri}")
            }
        }
    }

    private fun updateRentWithCustomerImages(customers: Int) {
        val imagesList = if (customers == 1) listOf(firstCustomerPhoto.toUri().lastPathSegment)
        else listOf(
            firstCustomerPhoto.toUri().lastPathSegment, secondCustomerPhoto.toUri().lastPathSegment
        )

        firestore.collection("rents").document(rentDocumentId).update("images", imagesList)
            .addOnSuccessListener {
                Log.d("WriteTagActivity", "Imagens salvas no banco de dados")
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao salvar imagens no banco de dados")
            }

        firestore.collection("rents").document(rentDocumentId).update("customers", customers)
            .addOnSuccessListener {
                Log.d("WriteTagActivity", "Imagens salvas no banco de dados")
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao salvar imagens no banco de dados")
            }
        finishProcess()
    }

    private fun finishProcess() {
        var lockerName = ""

        firestore.collection("rents").document(rentDocumentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rentInfo = document.data
                    if (rentInfo != null) {
                        val lockerId = rentInfo["lockerId"].toString()
                        firestore.collection("lockers").document(lockerId).get()
                            .addOnSuccessListener {
                                val lockerInfo = it.data
                                if (lockerInfo != null) {
                                    lockerName = lockerInfo["name"].toString()
                                    val intent = Intent(this, ShowLockerNameActivity::class.java)
                                    intent.putExtra("LOCKER_NAME", lockerName)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                }
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao buscar informações da locação")
            }


    }

    private fun activateButtonFinishAfterWriteOnTag() {
        binding.btnFinishOrNextPerson.isEnabled = true
    }

}