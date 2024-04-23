package br.edu.puccampinas.pi3_es_2024_time_25

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRentalOptionsBinding
import com.google.android.material.snackbar.Snackbar

data class Option(val id: String, val name: String, val description: String, val price: Double)


//Atividade para seleção de opções de locação
class RentalOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRentalOptionsBinding
    private lateinit var btnRentalOptions: AppCompatButton
    private lateinit var returnRentalOptions: Button

    //Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentalOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnRentalOptions = binding.btnRentalOptions as AppCompatButton

        returnRentalOptions = findViewById(R.id.return_rental_options)

        returnRentalOptions.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) // Ação ao clicar no botão de retorno
        }

        // Inicializa o RecyclerView e passa o callback para atualizar a cor do botão de confirmação
        initRecyclerView { option ->
            updateConfirmButton(option) // Atualiza a cor ou fundo do botão de confirmação
        }

        btnRentalOptions.setOnClickListener {
            confirmLocation()
        }

    }

    //Inicializa o RecyclerView com a lista de opções
    private fun initRecyclerView(onOptionSelected: (Option) -> kotlin.Unit) {
        val options = createRandomOptions()
        val adapter = RadioButtonAdapter(options, onOptionSelected) // Passa o callback para o adaptador
        binding.rvRentalOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvRentalOptions.addItemDecoration(MarginItemDecoration(16))
        binding.rvRentalOptions.adapter = adapter
    }

    private fun updateConfirmButton(option: Option) {
        if (option != null) {
            // Altera o fundo do botão para um drawable diferente
            btnRentalOptions.setBackgroundResource(R.drawable.selected_btn)
        }
    }


    // Função para exibir um aviso após confirmar a locação
    @SuppressLint("ResourceType")
    private fun showConfirmationDialog() {
        // Usa o tema personalizado ao criar o AlertDialog
        val alertDialog = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
            .setTitle("Atenção!")
            .setMessage("Será creditado do seu cartão o caução no valor de uma diária, que será reembolsado. Deseja continuar?")
            .setPositiveButton("Sim") { dialog, which ->
                val intent = Intent(this, QRCodeGeneratorActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não") { dialog, which -> dialog.dismiss() }
            .create()

        alertDialog.show() // Mostra o diálogo estilizado
    }



    //Confirma a locação após verificar se um horário foi selecionado
    private fun confirmLocation() {
        val selectedOption = (binding.rvRentalOptions.adapter as RadioButtonAdapter).selectedOption
        if (selectedOption != null) {
            // Mostra o aviso de confirmação com opções "Sim" ou "Não"
            showConfirmationDialog()

        } else {
            Snackbar.make(binding.root, "Selecione um horário antes de confirmar.", Snackbar.LENGTH_SHORT).show()
        }
    }


    //Cria uma lista de opções de locação
    private fun createRandomOptions(): List<Option> {
        return listOf(
            Option("1", "30 minutos - R$ 30,00", "Description 1", 10.0),
            Option("2", "1 hora - R$ 50,00", "Description 2", 20.0),
            Option("3", "2 horas - R$ 100,00", "Description 3", 30.0),
            Option("4", "4 horas - R$ 150,00", "Description 4", 40.0),
            Option("5", "Até as 18 horas - R$ 300,00", "Description 5", 50.0),
        )
    }

    //Classe para adicionar decoração ao RecyclerView para espaçamento entre itens
    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
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









