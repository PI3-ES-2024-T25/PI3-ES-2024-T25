package br.edu.puccampinas.pi3_es_2024_time_25

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.pi3_es_2024_time_25.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    // Declaração de propriedades
    private lateinit var botaoSair: AppCompatButton
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityMainBinding

    // Método chamado quando a Activity é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita a visualização de conteúdo na borda da tela
        enableEdgeToEdge()

        // Chama a função que configura o view binding
        setupViewBinding()

        // Inicialização das propriedades
        botaoSair = findViewById(R.id.btn_signOut)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Verifica se o usuário está autenticado
        if(user==null) {
            // Se não estiver autenticado, redireciona para a tela de login e finaliza esta activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Configura o clique do botão "botaoSair" para realizar o logout e redirecionar para a tela de login
        botaoSair.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Configura um Listener para aplicar as margens correspondentes às barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Função para configurar o view binding
    private fun setupViewBinding(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}



