package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityCameraBinding
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var btnOpenCamera: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initializeOpenCameraButton()
    }

    private fun initializeOpenCameraButton() {
        btnOpenCamera = binding.btnOpenCamera
        btnOpenCamera.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                cameraAndStorageProviderResult.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                cameraProviderResult.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCameraPreview() {
        val intent = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intent)
    }

    private val cameraAndStorageProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                openCameraPreview()
            } else {
                Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
            }
        }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCameraPreview()
            } else {
                Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
            }
        }
}