package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityContinuarSegundaFotoBinding

class ContinuarSegundaFotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContinuarSegundaFotoBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector

    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContinuarSegundaFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        binding.voltar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
                Log.e("ContinuarSegundaFoto", "Falha ao abrir a câmera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startImagePreviewActivity(photoUri: String) {
        val intent = Intent(this, SalvarFotoActivity::class.java)
        intent.putExtra("IMAGE_URI", photoUri)
        intent.putExtra("NUM_PEOPLE", 1)  // Apenas uma pessoa para finalizar a sessão de fotos
        startActivity(intent)
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "foto_${System.currentTimeMillis()}.jpg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val photoUri = file.toUri()
                        Log.i("ContinuarSegundaFoto", "Imagem salva em $photoUri")
                        startImagePreviewActivity(photoUri.toString())
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao salvar a imagem", Toast.LENGTH_LONG).show()
                        Log.e("ContinuarSegundaFoto", "Erro ao salvar foto $exception")
                    }
                }
            )
        }
    }
}

