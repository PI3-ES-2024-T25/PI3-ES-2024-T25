package br.edu.puccampinas.pi3_es_2024_time_25

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
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
import java.io.IOException
import kotlin.experimental.and

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainBinding
    private lateinit var btnScanQrCode: Button
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var scanActivityResultLauncher: ActivityResultLauncher<Intent>
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var tag: Tag? = null

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
        botaoWriteNfcTeste()
    }

    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
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

    private fun botaoWriteNfcTeste(){
        val btnWriteNFC = binding.btnWriteNFC
        btnWriteNFC.setOnClickListener {
            val message = "oi" // passa pra essa variável o que você quer escrever na tag, se colocar "", vai apagar o que está escrito na tag
            writeNFC(message)
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
                Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
            }
        }

    // verifica se o qr code escaneado é válido
    private fun verifyQrcodeScanResult(qrcodeScanResult: String) {
        firestore.collection("rents").document(qrcodeScanResult).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    AlertDialog.Builder(this).setTitle("Sucesso!")
                        .setMessage("Sua locação foi encontrada!")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
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


    private fun writeNFC(message:String) {
        try {
            if (message.isEmpty()) {
                write(message, tag)
                Toast.makeText(this, "Mensagem apagada com sucesso!", Toast.LENGTH_LONG).show()
            } else if (tag == null) {
                Toast.makeText(this, "Tag não encontrada", Toast.LENGTH_LONG).show()
            } else {
                write(message, tag)
                Toast.makeText(this, "Mensagem escrita com sucesso", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(this, "Erro ao escrever NFC", Toast.LENGTH_LONG).show()
        } catch (e: FormatException){
            Toast.makeText(this, "Erro ao escrever NFC", Toast.LENGTH_LONG).show()
        }
    }

    private fun write(text: String, tag: Tag?) {
        val records = arrayOf(createRecord(text))
        val message = NdefMessage(records)

        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            ndef.writeNdefMessage(message)
            Toast.makeText(this, "Mensagem escrita com sucesso", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao escrever NFC", Toast.LENGTH_LONG).show()
        } finally {
            try {
                ndef.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun createRecord(text: String): NdefRecord {
        val lang = "en"
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray(charset("US-ASCII"))
        val langLength = langBytes.size
        val textLengh = textBytes.size
        val payload = ByteArray(1 + langLength + textLengh)

        payload[0] = langLength.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLengh)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

}