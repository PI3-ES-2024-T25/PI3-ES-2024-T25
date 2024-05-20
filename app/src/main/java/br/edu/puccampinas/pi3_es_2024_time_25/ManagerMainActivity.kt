package br.edu.puccampinas.pi3_es_2024_time_25

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

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainBinding
    private lateinit var btnScanQrCode: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityManagerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initializeScanQrCodeButton()
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