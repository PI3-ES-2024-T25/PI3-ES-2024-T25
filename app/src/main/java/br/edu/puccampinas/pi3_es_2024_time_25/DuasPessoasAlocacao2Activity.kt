package br.edu.puccampinas.pi3_es_2024_time_25

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityDuasPessoasAlocacao2Binding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DuasPessoasAlocacao2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityDuasPessoasAlocacao2Binding
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDuasPessoasAlocacao2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val documentId = intent.getStringExtra("documentId")
        if (!documentId.isNullOrEmpty()) {
            fetchAndDisplayDocument(documentId)
        } else {
            Toast.makeText(this, "Document ID not found", Toast.LENGTH_SHORT).show()
        }

        binding.openMomentarilyButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Lembrete")
            builder.setMessage("Não esqueça de pedir para o cliente fechar o armário novamente.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        binding.closeAllocationButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Lembrete")
            builder.setMessage("Informações removidas com sucesso!.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun fetchAndDisplayDocument(documentId: String) {
        firestore.collection("rents").document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    val images = data?.get("images") as? ArrayList<*>
                    if (images != null && images.size >= 2) {
                        val imageName = images[1].toString()
                        displayImage(imageName)
                    } else {
                        Toast.makeText(this, "Second image not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun displayImage(imageName: String) {
        val storageRef = storage.reference.child("images/$imageName")
        val localFile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                // Usar Coroutines para chamar a função compressImage de forma assíncrona
                GlobalScope.launch(Dispatchers.IO) {
                    val compressedBitmap = compressImage(localFile)
                    // Exibir a imagem comprimida no thread principal
                    launch(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(compressedBitmap)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compressImage(file: File): Bitmap {
        // Decodificar a imagem do arquivo para um bitmap
        val options = BitmapFactory.Options()
        options.inSampleSize = 4 // Fator de escala para reduzir a qualidade da imagem
        var bitmap = BitmapFactory.decodeFile(file.path, options)

        // Corrigir a rotação da imagem
        val exif = androidx.exifinterface.media.ExifInterface(file.path)
        val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 1)
        val rotationAngle = when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationAngle.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        return bitmap
    }
}