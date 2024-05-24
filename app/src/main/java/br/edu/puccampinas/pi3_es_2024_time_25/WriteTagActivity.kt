package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        if (uriString != null) {
            firstCustomerPhoto = uriString
        }
        if (secondUriString != null) {
            secondCustomerPhoto = secondUriString
        }

        initializeFinishButton()
    }

    private fun initializeFinishButton() {
        binding.btnFinishOrNextPerson.setOnClickListener {
            when (numberOfCustomers) {
                "oneOfOne" -> {
                    Toast.makeText(this, "Foto de uma pessoa", Toast.LENGTH_SHORT).show()
                    saveImageOnStorage(firstCustomerPhoto)
                }

                "twoOfTwo" -> {
                    Toast.makeText(
                        this, "2 ${firstCustomerPhoto} ${secondCustomerPhoto}", Toast.LENGTH_SHORT
                    ).show()
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

}