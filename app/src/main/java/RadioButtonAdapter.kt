package br.edu.puccampinas.pi3_es_2024_time_25

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale
import kotlin.Unit

//Adaptador para um RecyclerView que exibe uma lista de opções com RadioButtons
class RadioButtonAdapter(
    private val options: List<RentalOption>,
    private val onOptionSelected: (RentalOption) -> Unit // Callback para notificar a seleção
) : RecyclerView.Adapter<RadioButtonAdapter.ViewHolder>() {

    // Armazena a opção selecionada atualmente
    var selectedOption: RentalOption? = null


    //Classe ViewHolder para manter referências aos elementos da interface
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton =
            view.findViewById(R.id.radioButton) // Referência ao RadioButton no layout


        //Inicializa o ViewHolder e configura o OnClickListener para o RadioButton
        init {
            radioButton.setOnClickListener {
                selectedOption = options[adapterPosition] // Atualiza a opção selecionada
                notifyDataSetChanged() // Atualiza a interface para refletir a nova seleção
                onOptionSelected(selectedOption!!) // Chama o callback para notificar a seleção
            }
        }
    }


    //Cria um novo ViewHolder para o RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rb_adapter, parent, false) // Infla o layout
        return ViewHolder(view)
    }

    //Converte um número de minutos para um texto que representa uma quantidade de tempo em horas e minutos
    private fun formatMinutesToText(minutes: Int): String {

        if (minutes == 600) return "Até às 18:00"

        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours == 1 && remainingMinutes > 0 -> "$hours hora e $remainingMinutes minutos"
            hours == 1 -> "$hours hora"
            hours > 0 && remainingMinutes > 0 -> "$hours horas e $remainingMinutes minutos"
            hours > 0 -> "$hours horas"
            else -> "$remainingMinutes minutos"
        }
    }

    //Liga os dados do modelo ao ViewHolder para a posição especificada
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position] // Obtém a opção para a posição especificada
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        holder.radioButton.text =
            "${formatMinutesToText(option.time)} - ${format.format(option.price)}" // Define o texto do RadioButton
        holder.radioButton.isChecked =
            option == selectedOption // Marca o RadioButton se for a opção selecionada
    }


    //Retorna o número total de itens no RecyclerView
    override fun getItemCount() = options.size
}


