package br.edu.puccampinas.pi3_es_2024_time_25

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityQrcodeGeneratorBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


class QRCodeGeneratorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeGeneratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout usando view binding
        binding = ActivityQrcodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Habilita a visualização de conteúdo na borda da tela
        enableEdgeToEdge()

        // Obtém os dados de locação passados para a activity
        val gsonData = intent.getStringExtra("rentData")
        // Converte os dados JSON para um objeto QrCodeData usando Gson
        val rentData = Gson().fromJson(gsonData, QrCodeData::class.java)
        // Verifica se os dados de aluguel não são nulos
        if (rentData != null) {
            // Chama a função de gerar QRCode, passando como parâmetro o ID do aluguel
            generateQRCode(rentData.rentId)
            // Atualiza o texto na TextView com o nome do gerente
            val managerNameText = getString(R.string.manager_name, rentData.managerName)
            binding.textView2.text = managerNameText
        }

        // Configura o clique do botão "voltarQrCode" para finalizar a Activity
        binding.voltarQrCode.setOnClickListener {
            finish()
        }
    }

    // Classe de dados que representa os dados do QRCode
    data class QrCodeData(val rentId: String, val managerName: String)


    // Função que recebe uma string para gerar o QRCode
    private fun generateQRCode(token: String) {
        // Inicializa um escritor de QRCode
        val qrCodeWriter = QRCodeWriter()
        try {
            // Tenta codificar o token em um BitMatrix usando o formato QR_CODE, com 512 de largura e altura
            val bitMatrix: BitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 512, 512)

            // Obtém a largura e a altura do BitMatrix
            val width = bitMatrix.width
            val height = bitMatrix.height
            // Cria um Bitmap para armazenar o QRCode
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // Preenche o Bitmap com os pixels correspondentes ao BitMatrix
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }
            // Encontra o ImageView no layout e define o Bitmap gerado como sua imagem
            val imageViewQRCode = findViewById<ImageView>(R.id.qr_code)
            imageViewQRCode.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}