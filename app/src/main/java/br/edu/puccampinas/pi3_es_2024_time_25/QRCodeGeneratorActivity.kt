package br.edu.puccampinas.pi3_es_2024_time_25

import android.graphics.Bitmap
//import android.graphics.Color
import android.os.Bundle
//import android.widget.Button
import android.widget.ImageView
//import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.core.graphics.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


class QRCodeGeneratorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode_generator)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.QRCodeGeneratorActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun gerarToken(size: Int = 20): String {
        val caracteres = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..size).map { caracteres.random() }.joinToString("")
    }


    fun generateQRCodeOnClick(view: android.view.View ){
        val tokenToEncode = gerarToken()
        generateQRCode(tokenToEncode)
    }

    private fun generateQRCode(token: String){
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 512, 512)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width){
                for (y in 0 until height){
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            val imageViewQRCode = findViewById<ImageView>(R.id.qr_code)
            imageViewQRCode.setImageBitmap(bitmap)

        } catch (e: WriterException){
            e.printStackTrace()
        }
    }
}