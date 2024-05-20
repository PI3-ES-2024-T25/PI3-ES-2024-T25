package br.edu.puccampinas.pi3_es_2024_time_25

import android.nfc.NfcAdapter
import android.content.Intent
import android.nfc.NdefMessage
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityManagerMainBinding
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlin.experimental.and

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainBinding
    private lateinit var btnScanQrCode: Button
    private var nfcAdapter: NfcAdapter? = null
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
    }

    override fun onResume(){
        super.onResume()
        if(NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action){
            readNFC(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun readNFC(intent: Intent){
        val messages = if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }

        messages?.also{
            val ndefMessage = it[0] as NdefMessage
            val ndefRecord = ndefMessage.records[0]

            // tirar o prefixo de idioma
            val payload = ndefRecord.payload
            val textEncoding = if((payload[0] and 128.toByte()) == 0.toByte()) "UTF-8" else "UTF-16"
            val languageCodeLength = (payload[0] and 51).toInt()
            val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, charset(textEncoding))
            // o conteúdo que está escrito na tag NFC fica armazenado nessa variável text
            // coloquei um text view para demonstrar que o conteúdo da tag foi lido corretamente.

            binding.textNFC.text = text
        }

    }


    private fun initializeScanQrCodeButton() {
        btnScanQrCode = binding.btnScanQrcode
        val gmsScannerOptions = configureScannerOption()
        val instance = getBarcodeScannerInstance(gmsScannerOptions)
        btnScanQrCode.setOnClickListener {
            initiateScanner(instance, onSuccess = { barcode ->
                Toast.makeText(this, "Scanned: ${barcode.displayValue}", Toast.LENGTH_SHORT).show()
            }, onCancel = {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
            }, onFailure = { e ->
                Log.e("ManagerMainActivity", "Error: ${e.message}")
            })
        }
    }

    private fun configureScannerOption(): GmsBarcodeScannerOptions {
        return GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE
        ).build()
    }

    private fun getBarcodeScannerInstance(gmsBarcodeScannerOptions: GmsBarcodeScannerOptions): GmsBarcodeScanner {
        return GmsBarcodeScanning.getClient(this, gmsBarcodeScannerOptions)
    }

    private fun initiateScanner(
        gmsBarcodeScanner: GmsBarcodeScanner,
        onSuccess: (Barcode) -> kotlin.Unit,
        onCancel: () -> kotlin.Unit,
        onFailure: (Exception) -> Int
    ) {
        gmsBarcodeScanner.startScan().addOnSuccessListener { barcode ->
            // Task completed successfully
            val result = barcode.rawValue
            Log.d("ManagerMainActivity", "initiateScanner: $result")
            // check the value - URL, TEXT, etc.
            when (barcode.valueType) {
                Barcode.TYPE_URL -> {
                    Log.d("ManagerMainActivity", "initiateScanner: ${barcode.valueType}")
                }

                else -> {
                    Log.d("ManagerMainActivity", "initiateScanner: ${barcode.valueType}")
                }
            }
            // Display valu
            Log.d("ManagerMainActivity", "initiateScanner: Display value: ${barcode.displayValue}")
            // Formate - FORMAT_AZTEC, etc.
            Log.d("ManagerMainActivity", "initiateScanner: Format: ${barcode.format}")
            onSuccess(barcode)
        }.addOnCanceledListener {
            // Task canceled by the user
            onCancel()
        }.addOnFailureListener { e ->
            // Task failed with an exception
            onFailure(e)
        }

    }
}