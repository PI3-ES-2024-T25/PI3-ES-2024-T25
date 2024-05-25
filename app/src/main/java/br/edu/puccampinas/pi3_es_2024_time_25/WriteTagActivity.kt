package br.edu.puccampinas.pi3_es_2024_time_25

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityWriteTagBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.nfc.NdefRecord
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.FormatException
import android.os.Build
import android.widget.Toast
import java.io.IOException

class WriteTagActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteTagBinding
    private lateinit var firstCustomerPhoto: String
    private lateinit var secondCustomerPhoto: String
    private var hasManagerWriteOnTag: Boolean = false
    private lateinit var rentDocumentId: String
    private lateinit var numberOfCustomers: String
    private var nfcAdapter: NfcAdapter? = null
    private var tag: Tag? = null
    private var intentFiltersArray: Array<IntentFilter>? = null


    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWriteTagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC não está disponível", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        var pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE
        )

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        intentFiltersArray = arrayOf(ndef)

    }

    override fun onStart() {
        super.onStart()
        getIntentInfo()
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply { addCategory(Intent.CATEGORY_DEFAULT) },
        )

        val techListArray = arrayOf(
            arrayOf(android.nfc.tech.Ndef::class.java.name)
        )

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListArray)
        writeNFC(rentDocumentId)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
    }

    private fun getIntentInfo() {
        val uriString = intent.getStringExtra("IMAGE_URI")
        val secondUriString = intent.getStringExtra("SECOND_IMAGE_URI")
        numberOfCustomers = intent.getStringExtra("COUNTER").toString()
        rentDocumentId = intent.getStringExtra("RENT_DOCUMENT_ID").toString()

        if (numberOfCustomers == "oneOfTwo") {
            binding.btnFinishOrNextPerson.text = "Próxima pessoa"
            binding.btnFinishOrNextPerson.isEnabled = false
        } else {
            binding.btnFinishOrNextPerson.text = "Finalizar"
            binding.btnFinishOrNextPerson.isEnabled = false
        }

        if (uriString != null) {
            firstCustomerPhoto = uriString
        }
        if (secondUriString != null) {
            secondCustomerPhoto = secondUriString
        }
//        binding.logoLockngo.setOnClickListener {
//            activateButtonFinishAfterWriteOnTag()
//        }
        initializeFinishButton()
    }

    private fun initializeFinishButton() {
        binding.btnFinishOrNextPerson.setOnClickListener {
            when (numberOfCustomers) {
                "oneOfOne" -> {
                    saveImageOnStorage(firstCustomerPhoto)
                    updateRentWithCustomerImages(1)
                    Log.d("WriteTagActivity", "uma pessoa ${firstCustomerPhoto}")
                }

                "twoOfTwo" -> {
                    updateRentWithCustomerImages(2)
                    Log.d(
                        "WriteTagActivity",
                        " duas pessoas${firstCustomerPhoto} ${secondCustomerPhoto}"
                    )
                }

                else -> {
                    val intent = Intent(this, CameraPreviewActivity::class.java)
                    intent.putExtra("COUNTER", "twoOfTwo")
                    intent.putExtra("IMAGE_URI", firstCustomerPhoto)
                    intent.putExtra("RENT_DOCUMENT_ID", rentDocumentId)
                    startActivity(intent)
                }
            }
        }
    }

    private fun saveImageOnStorage(imageUri: String) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${imageUri.toUri().lastPathSegment}")
        val uploadTask = imageRef.putFile(Uri.parse(imageUri))

        uploadTask.addOnFailureListener {
            Log.e("WriteTagActivity", "Falha ao salvar a imagem no storage")
        }.addOnSuccessListener {

            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.d("WriteTagActivity", "IMG salva ${downloadUri}")
            }
        }
    }

    private fun updateRentWithCustomerImages(customers: Int) {
        val imagesList = if (customers == 1) listOf(firstCustomerPhoto.toUri().lastPathSegment)
        else listOf(
            firstCustomerPhoto.toUri().lastPathSegment, secondCustomerPhoto.toUri().lastPathSegment
        )

        firestore.collection("rents").document(rentDocumentId).update("images", imagesList)
            .addOnSuccessListener {
                Log.d("WriteTagActivity", "Imagens salvas no banco de dados")
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao salvar imagens no banco de dados")
            }

        firestore.collection("rents").document(rentDocumentId)
            .update("customers", customers.toString())
            .addOnSuccessListener {
                Log.d("WriteTagActivity", "Imagens salvas no banco de dados")
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao salvar imagens no banco de dados")
            }
        finishProcess()
    }

    private fun finishProcess() {
        var lockerName = ""

        firestore.collection("rents").document(rentDocumentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rentInfo = document.data
                    if (rentInfo != null) {
                        val lockerId = rentInfo["lockerId"].toString()
                        firestore.collection("lockers").document(lockerId).get()
                            .addOnSuccessListener {
                                val lockerInfo = it.data
                                if (lockerInfo != null) {
                                    lockerName = lockerInfo["name"].toString()
                                    val intent = Intent(this, ShowLockerNameActivity::class.java)
                                    intent.putExtra("LOCKER_NAME", lockerName)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                }
            }.addOnFailureListener {
                Log.e("WriteTagActivity", "Erro ao buscar informações da locação")
            }
    }

    private fun activateButtonFinishAfterWriteOnTag() {
        binding.btnFinishOrNextPerson.isEnabled = true
    }

    private fun writeNFC(message: String) {
        try {
            if (message.isEmpty()) {
                write(message, tag)
                Toast.makeText(this, "Dados da pulseira apagados com sucesso!", Toast.LENGTH_LONG)
                    .show()
            } else if (tag == null) {
                Toast.makeText(this, "Aproxime a tag NFC", Toast.LENGTH_LONG).show()
            } else {
                write(message, tag)
                //Toast.makeText(this, "Dados da pulseira escritos com sucesso!", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Falha ao escrever o NFC, tente novamente", Toast.LENGTH_LONG)
                .show()
        } catch (e: FormatException) {
            Toast.makeText(this, "Falha ao escrever o NFC, tente novamente", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun write(text: String, tag: Tag?) {
        val records = arrayOf(createRecord(text))
        val message = NdefMessage(records)

        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            ndef.writeNdefMessage(message)
            Toast.makeText(this, "NFC Escrita com sucesso", Toast.LENGTH_LONG).show()
            activateButtonFinishAfterWriteOnTag()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Falha ao escrever o NFC, tente novamente", Toast.LENGTH_LONG)
                .show()
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
        val textLength = textBytes.size
        val payload = ByteArray(1 + langLength + textLength)

        payload[0] = langLength.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

}