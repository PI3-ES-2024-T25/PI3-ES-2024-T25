import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.pi3_es_2024_time_25.Option
import br.edu.puccampinas.pi3_es_2024_time_25.R

class RadioButtonAdapter(private val options: List<Option>) :
    RecyclerView.Adapter<RadioButtonAdapter.ViewHolder>() {

    var selectedOption: Option? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)

        init {
            radioButton.setOnClickListener {
                selectedOption = options[adapterPosition]
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rb_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.radioButton.text = option.name
        holder.radioButton.isChecked = option == selectedOption
    }

    override fun getItemCount() = options.size
}