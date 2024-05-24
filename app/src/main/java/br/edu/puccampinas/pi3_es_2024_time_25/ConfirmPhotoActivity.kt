package br.edu.puccampinas.pi3_es_2024_time_25


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityConfirmPhotoBinding


class ConfirmPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmPhotoBinding
    private lateinit var imageView: ImageView
    private lateinit var btnSavePhoto: AppCompatButton
    private lateinit var imageUri: Uri
    private lateinit var imageUri2: Uri
    private lateinit var numberOfCustomers: String
    private lateinit var progressBar: ProgressBar
    private lateinit var rentDocumentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityConfirmPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.imageView
        btnSavePhoto = binding.btnSavePhoto
        progressBar = binding.progressBar

        val uriString = intent.getStringExtra("IMAGE_URI")
        val secondUriString = intent.getStringExtra("SECOND_IMAGE_URI")
        numberOfCustomers = intent.getStringExtra("COUNTER").toString()
        rentDocumentId = intent.getStringExtra("RENT_DOCUMENT_ID").toString()

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            imageView.setImageURI(imageUri)
        }

        btnSavePhoto.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val intent = Intent(this, WriteTagActivity::class.java)
            if (numberOfCustomers != "twoOfTwo") {
                intent.putExtra("IMAGE_URI", imageUri.toString())
            } else {
                intent.putExtra("IMAGE_URI", imageUri.toString())
                intent.putExtra("SECOND_IMAGE_URI", secondUriString.toString())
            }
            intent.putExtra("COUNTER", numberOfCustomers)
            intent.putExtra("RENT_DOCUMENT_ID", rentDocumentId)
            startActivity(intent)
            finish()
        }

        binding.btnReturn.setOnClickListener {
            finish()
        }
    }


}
