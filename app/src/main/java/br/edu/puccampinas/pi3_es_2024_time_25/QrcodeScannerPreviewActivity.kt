package br.edu.puccampinas.pi3_es_2024_time_25

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityQrcodeScannerPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutionException

class QrcodeScannerPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeScannerPreviewBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var qrcodeScanResult: String
    private val tag: String = "QrcodeScannerPreview"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrcodeScannerPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeCameraProvider()
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeCameraProvider() {
        qrcodeScanResult = ""
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProvider = cameraProviderFuture.get()
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    }

    override fun onStart() {
        super.onStart()
        setupScanner()
    }

    private fun setupScanner() {
        cameraProvider.unbindAll()
        cameraProviderFuture.addListener({
            try {
                processScan()
            } catch (e: ExecutionException) {
                Log.d(tag, "An error occurred: $e")
            } catch (e: InterruptedException) {
                Log.d(tag, "An error occurred: $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processScan() {
        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this)
        ) { imageProxy: ImageProxy ->
            val image =
                InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder().build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes: List<Barcode> -> processResult(barcodes) }
                .addOnFailureListener { e: Exception ->
                    // Task failed with an exception
                    Toast.makeText(
                        this, "Failed to scan.", Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }.addOnCompleteListener(
                    ContextCompat.getMainExecutor(this)
                ) { imageProxy.close() }
        }

        preview.setSurfaceProvider(binding.scannerPreviewView.surfaceProvider)
        cameraProvider.bindToLifecycle(
            this, cameraSelector, imageAnalysis, preview
        )
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            sendRequiredData(barcodes[0])
            cameraProvider.unbindAll()
        }
    }

    private fun sendRequiredData(barcode: Barcode) {
        val data = Intent()
        qrcodeScanResult = barcode.displayValue.toString()
        data.putExtra("SCAN_RESULT", qrcodeScanResult)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

}