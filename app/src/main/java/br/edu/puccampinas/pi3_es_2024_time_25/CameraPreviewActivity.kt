package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.common.util.concurrent.ListenableFuture
import androidx.camera.core.Preview
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityCameraPreviewBinding

class CameraPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private lateinit var numberOfCustomers: String
    private lateinit var rentDocumentId: String
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        numberOfCustomers = intent.getStringExtra("COUNTER").toString()
        rentDocumentId = intent.getStringExtra("RENT_DOCUMENT_ID").toString()
        imageUri = Uri.parse(intent.getStringExtra("IMAGE_URI").toString())
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        if (numberOfCustomers == "twoOfTwo") {
            binding.btnReturn.visibility = View.GONE
        }
        binding.btnReturn.setOnClickListener {
            finish()
        }
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Falha ao abrir a câmera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startImagePreviewActivity(photoUri: Uri) {
        val intent = Intent(this, ConfirmPhotoActivity::class.java)
        if (numberOfCustomers == "twoOfTwo") {
            intent.putExtra("SECOND_IMAGE_URI", photoUri.toString())
            intent.putExtra("IMAGE_URI", imageUri.toString())
        } else {
            intent.putExtra("IMAGE_URI", photoUri.toString())
        }
        intent.putExtra("COUNTER", numberOfCustomers)
        intent.putExtra("RENT_DOCUMENT_ID", rentDocumentId)
        startActivity(intent)
        finish()
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(externalMediaDirs.first(), fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val photoUri = file.toUri()
                        startImagePreviewActivity(photoUri)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            binding.root.context,
                            "Erro ao salvar a imagem",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("CameraPreview", "Erro ao salvar foto $exception")
                    }
                }
            )
        }
    }
}