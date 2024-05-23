package br.edu.puccampinas.pi3_es_2024_time_25

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityManagerMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.experimental.and

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainBinding
    private lateinit var btnScanQrCode: Button
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var scanActivityResultLauncher: ActivityResultLauncher<Intent>
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityManagerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // verifica se há disponibilidade do nfc adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC não está disponível", Toast.LENGTH_LONG).show()
            finish()
            return
        }

    }


    override fun onStart() {
        super.onStart()
        initializeScanQrCodeButton()
        initializeSignoutButton()
    }

    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            readNFC(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun readNFC(intent: Intent) {
        val messages =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java
                )
            } else {
                @Suppress("DEPRECATION") intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }

        messages?.also {
            val ndefMessage = it[0] as NdefMessage
            val ndefRecord = ndefMessage.records[0]

            // tirar o prefixo de idioma
            val payload = ndefRecord.payload
            val textEncoding =
                if ((payload[0] and 128.toByte()) == 0.toByte()) "UTF-8" else "UTF-16"
            val languageCodeLength = (payload[0] and 51).toInt()
            val text = String(
                payload,
                languageCodeLength + 1,
                payload.size - languageCodeLength - 1,
                charset(textEncoding)
            )
            // o conteúdo que está escrito na tag NFC fica armazenado nessa variável text
            // coloquei um text view para demonstrar que o conteúdo da tag foi lido corretamente.

            binding.textNFC.text = text
        }

    }

    private fun initializeSignoutButton() {
        // inicializa o botão de sair
        val btnSignout = binding.btnSignout
        btnSignout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initializeScanQrCodeButton() {
        // inicializa o scanActivityResultLauncher para abrir a câmera e escanear o qr code e obter o resultado
        scanActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val scanResult = data?.getStringExtra("SCAN_RESULT")
                if (scanResult != null) {
                    verifyQrcodeScanResult(scanResult)
                }
            }
        }
        // inicializa o botão de scan do qr code
        btnScanQrCode = binding.btnScanQrcode
        btnScanQrCode.setOnClickListener {
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openQrcodeScannerPreview() {
        // inicializa a intent para abrir a câmera e escanear o qr code
        val intent = Intent(this, QrcodeScannerPreviewActivity::class.java)
        scanActivityResultLauncher.launch(intent)
    }

    // inicializa o cameraProviderResult para solicitar permissão de uso da câmera
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openQrcodeScannerPreview()
            } else {
                Snackbar.make(binding.root, "Permiss", Snackbar.LENGTH_SHORT).show()
            }
        }

    // verifica se o qr code escaneado é válido
    private fun verifyQrcodeScanResult(qrcodeScanResult: String) {
        firestore.collection("rents").document(qrcodeScanResult).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    showChoiceMenu()
                } else {
                    AlertDialog.Builder(this).setTitle("Erro")
                        .setMessage("QR code inválido, por favor verifique-o e tente novamente.")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao verificar o qr code!", Toast.LENGTH_LONG).show()
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