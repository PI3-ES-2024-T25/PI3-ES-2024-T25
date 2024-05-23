package br.edu.puccampinas.pi3_es_2024_time_25


import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class SalvarFotoActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnSavePhoto: AppCompatButton
    private lateinit var imageUri: Uri
    private var numPeople: Int = 1
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_salvar_foto)

        imageView = findViewById(R.id.imageView)
        btnSavePhoto = findViewById(R.id.btnSavePhoto)
        progressBar = findViewById(R.id.progressBar)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val uriString = intent.getStringExtra("IMAGE_URI")
        numPeople = intent.getIntExtra("NUM_PEOPLE", 1)

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            imageView.setImageURI(imageUri)
        }

        btnSavePhoto.setOnClickListener {
            progressBar.visibility = View.VISIBLE // Mostrar o ProgressBar
            uploadPhotoToFirebase(imageUri)
        }
    }

    private fun uploadPhotoToFirebase(fileUri: Uri) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}.jpg")

        val uploadTask = storageReference.putFile(fileUri)
        uploadTask.addOnSuccessListener {
            progressBar.visibility = View.GONE // Esconder o ProgressBar
            Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show()
            if (numPeople == 2) {
                val intent = Intent(this, CameraPreview2Activity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE // Esconder o ProgressBar
            Toast.makeText(this, "Erro ao salvar a foto", Toast.LENGTH_SHORT).show()
        }
    }

}
