package br.edu.puccampinas.pi3_es_2024_time_25

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityCameraPreviewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class CameraPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBinding
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnSavePhoto: MaterialButton
    private lateinit var photoFile: File
    private val storage by lazy { FirebaseStorage.getInstance() }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageCaptureExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeCameraProvider()
        initializeTakePhotoButton()
        initializeSavePhotoButton()
    }

    private fun initializeCameraProvider() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imageCaptureExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initializeTakePhotoButton() {
        btnTakePhoto = binding.btnTakePhoto
        btnTakePhoto.isEnabled = false
        btnTakePhoto.setOnClickListener {
            takePhoto()
            blinkPreview()
        }
    }

    private fun initializeSavePhotoButton() {
        btnSavePhoto = binding.btnSavePhoto
        btnSavePhoto.setOnClickListener {
            uploadImageToFirebase(photoFile)
        }
    }

    override fun onStart() {
        super.onStart()
        startCamera()
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            try {
                initializeCamera()
            } catch (e: Exception) {
                Log.e("CameraPreviewActivity", "Error starting camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initializeCamera() {
        imageCapture = ImageCapture.Builder().build()
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        }
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        btnTakePhoto.isEnabled = true
    }

    private fun takePhoto() {
        imageCapture.let { imageCapture ->
            photoFile = createImageFile()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d(
                            "CameraPreviewActivity",
                            "Photo capture succeeded: ${photoFile.absolutePath}"
                        )
                        btnTakePhoto.visibility = View.GONE
                        btnSavePhoto.visibility = View.VISIBLE
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreviewActivity", "Photo capture failed", exception)
                    }
                })
        }
    }

    private fun createImageFile(): File {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        return File(externalMediaDirs.first(), fileName)
    }

    private fun blinkPreview() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    private fun uploadImageToFirebase(imageFile: File) {
        val storageRef = storage.getReference("images")
        val imageRef = storageRef.child(imageFile.name)
        val uploadTask = imageRef.putFile(imageFile.toUri())
        uploadTask.addOnSuccessListener {
            Log.d("CameraPreviewActivity", "Image uploaded successfully")
            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            btnSavePhoto.visibility = View.GONE
            btnTakePhoto.visibility = View.VISIBLE
        }.addOnFailureListener {
            Log.e("CameraPreviewActivity", "Image upload failed", it)
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageCaptureExecutor.shutdown()
    }

    override fun onStop() {
        super.onStop()
        cameraProviderFuture.get().unbindAll()
    }
}
