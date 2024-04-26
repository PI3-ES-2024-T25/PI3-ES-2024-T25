package br.edu.puccampinas.pi3_es_2024_time_25

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRentalOptionsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date

//Atividade para seleção de opções de locação
class RentalOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRentalOptionsBinding
    private lateinit var options: List<RentalOption> // Lista de opções de locação
    private lateinit var bd: FirebaseFirestore
    private lateinit var unit: Unit //Objeto que representa uma unidade de locação

    //Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentalOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = FirebaseFirestore.getInstance()
        // Ação ao clicar no botão de retorno
        binding.returnRentalOptions.setOnClickListener {
            startActivity(
                Intent(
                    this, LoginActivity::class.java
                )
            ) // Ação ao clicar no botão de retorno
        }

        // pegando informações passadas pela intent
        getUnitFromIntent()

        // Inicializa o RecyclerView e passa o callback para atualizar a cor do botão de confirmação
        initRecyclerView { option ->
            updateConfirmButton(option) // Atualiza a cor ou fundo do botão de confirmação
        }

        // Confirma a locação após verificar se um horário foi selecionado
        confirmLocation()
    }

    // Recebe os dados vindo da intent
    private fun getUnitFromIntent() {
        val unitJson = intent.getStringExtra("unit")
        val gson = Gson()
        unit = gson.fromJson(unitJson, Unit::class.java)
        options = unit.rentalOptions // Obtém as opções de locação da unidade
    }


    //Inicializa o RecyclerView com a lista de opções
    private fun initRecyclerView(onOptionSelected: (RentalOption) -> kotlin.Unit) {
        val adapter =
            RadioButtonAdapter(options, onOptionSelected) // Passa o callback para o adaptador
        binding.rvRentalOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvRentalOptions.addItemDecoration(MarginItemDecoration(1))
        binding.rvRentalOptions.adapter = adapter
    }

    //Atualiza a cor ou fundo do botão de confirmação
    private fun updateConfirmButton(option: RentalOption) {
        if (option != null) {
            // Altera o fundo do botão para um drawable diferente
            println("option: $option")
            binding.btnRentalOptions.setBackgroundResource(R.drawable.selected_btn)
            binding.btnRentalOptions.setTextColor(getColor(R.color.white))
        }
    }

    //Classe interna para armazenar informações de locação
    data class RentInfo(
        val unit: Unit,
        val rentalOption: RentalOption,
        val uid: String,
        val startDate: String,
    )

    //Método para alugar um armário
    private fun rentLocker(rentInfo: RentInfo) { //Adiciona um documento ao banco de dados rents
        bd.collection("rents").add(rentInfo).addOnSuccessListener { document ->
            val rentId = document.id
            val intent = Intent(this, QRCodeGeneratorActivity::class.java)

            data class QrCodeData(val rentId: String, val managerName: String)

            val qrCodeData = QrCodeData(rentId, unit.manager.name)
            val gson = Gson()
            intent.putExtra("rentData", gson.toJson(qrCodeData))
            startActivity(intent)
            finish()
        }.addOnFailureListener { //Se falhar, exibe uma mensagem de erro
            Snackbar.make(
                binding.root,
                "Erro ao realizar locação. Tente novamente.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    // Função para exibir um aviso após confirmar a locação
    @SuppressLint("ResourceType")
    private fun showConfirmationDialog() {
        // Usa o tema personalizado ao criar o AlertDialog
        val alertDialog =
            AlertDialog.Builder(this, R.style.CustomAlertDialogTheme).setTitle("Atenção!")
                .setMessage("Será creditado do seu cartão o caução no valor de uma diária, que será reembolsado. Deseja continuar?")
                .setPositiveButton("Sim") { _, _ ->
                    val selectedOption =
                        (binding.rvRentalOptions.adapter as RadioButtonAdapter).selectedOption
                    val rentInfo = RentInfo(
                        unit,
                        selectedOption!!,
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    )
                    rentLocker(rentInfo)
                }.setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }.create()

        alertDialog.show() // Mostra o diálogo estilizado
    }

    //Confirma a locação após verificar se um horário foi selecionado
    private fun confirmLocation() {
        binding.btnRentalOptions.setOnClickListener {

            val selectedOption =
                (binding.rvRentalOptions.adapter as RadioButtonAdapter).selectedOption
            if (selectedOption != null) {
                // Mostra o aviso de confirmação com opções "Sim" ou "Não"
                showConfirmationDialog()

            } else {
                Snackbar.make(
                    binding.root, "Selecione um horário antes de confirmar.", Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    //Classe para adicionar decoração ao RecyclerView para espaçamento entre itens
    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            with(outRect) {
                top = spaceHeight
                left = spaceHeight
                right = spaceHeight
                bottom = spaceHeight
            }
        }
    }
}









