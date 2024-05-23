package br.edu.puccampinas.pi3_es_2024_time_25

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityCameraBinding


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnOpenCamera.setOnClickListener {
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        }
    }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showChoiceMenu()
            } else {
                Snackbar.make(binding.root, "Você não concedeu permissão para usar a câmera", Snackbar.LENGTH_INDEFINITE).show()
            }
        }

    private fun showChoiceMenu() {
        val options = arrayOf("Uma pessoa", "Duas pessoas")
        android.app.AlertDialog.Builder(this)
            .setTitle("Quantas pessoas acessarão o armário?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> abrirCameraUmaPessoa()
                    1 -> abrirCameraDuasPessoas()
                }
            }
            .show()
    }

    private fun abrirCameraUmaPessoa() {
        val intentCameraPreview = Intent(this, CameraPreviewActivity::class.java)
        intentCameraPreview.putExtra("NUM_PEOPLE", 1)
        startActivity(intentCameraPreview)
    }

    private fun abrirCameraDuasPessoas() {
        val intentCameraPreview = Intent(this, CameraPreviewActivity::class.java)
        intentCameraPreview.putExtra("NUM_PEOPLE", 2)
        startActivity(intentCameraPreview)
    }
}