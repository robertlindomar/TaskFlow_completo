package com.prova.taskflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Toast

class RegistroCategoriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_categoria)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_principal_categoria)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val layoutNome = findViewById<TextInputLayout>(R.id.layout_entrada_nome_categoria)
        val campoNome = findViewById<TextInputEditText>(R.id.campo_nome_categoria)
        val botaoSalvar = findViewById<MaterialButton>(R.id.botao_salvar_categoria)
        val botaoVoltar = findViewById<MaterialButton>(R.id.botao_voltar_categoria)

        botaoSalvar.setOnClickListener {
            val nome = campoNome.text.toString().trim()
            if (nome.isEmpty()) {
                layoutNome.error = getString(R.string.erro_nome_categoria_vazio)
            } else {
                layoutNome.error = null
                // TODO: Salvar no Room Database
                Toast.makeText(this, "Categoria \"$nome\" salva!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        botaoVoltar.setOnClickListener {
            finish()
        }
    }
}
