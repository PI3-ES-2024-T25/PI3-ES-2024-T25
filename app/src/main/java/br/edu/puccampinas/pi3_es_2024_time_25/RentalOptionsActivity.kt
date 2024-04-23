package br.edu.puccampinas.pi3_es_2024_time_25

import RadioButtonAdapter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityRentalOptionsBinding

data class Option(val id: String, val name: String, val description: String, val price: Double)
class RentalOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRentalOptionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRentalOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val options = createRandomOptions()
        val adapter = RadioButtonAdapter(options)
        binding.rvRentalOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        val itemDecoration = MarginItemDecoration(16) // Use a quantidade de espaço que você deseja
        binding.rvRentalOptions.addItemDecoration(itemDecoration)
        binding.rvRentalOptions.adapter = adapter
    }

    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
        ) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceHeight
                }
                left = spaceHeight
                right = spaceHeight
                bottom = spaceHeight
            }
        }
    }

    private fun createRandomOptions(): List<Option> {
        return listOf(
            Option("1", "Option 1", "Description 1", 10.0),
            Option("2", "Option 2", "Description 2", 20.0),
            Option("3", "Option 3", "Description 3", 30.0),
            Option("4", "Option 4", "Description 4", 40.0),
            Option("5", "Option 5", "Description 5", 50.0),
            Option("6", "Option 6", "Description 6", 60.0),
            Option("7", "Option 7", "Description 7", 70.0),
            Option("8", "Option 8", "Description 8", 80.0),
            Option("9", "Option 9", "Description 9", 90.0),
            Option("10", "Option 10", "Description 10", 100.0)
        )
    }
}

