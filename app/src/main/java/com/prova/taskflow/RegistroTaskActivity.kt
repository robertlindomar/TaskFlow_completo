package com.prova.taskflow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistroTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_task)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_principal_cadastro)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botaoCancelarTarefa = findViewById<MaterialButton>(R.id.botao_cancelar_tarefa)
        val chipVoltar = findViewById<MaterialButton>(R.id.chip_voltar)
        val chipCategoriaCadastro = findViewById<MaterialButton>(R.id.chip_categoria_cadastro)
        val botaoSalvar = findViewById<MaterialButton>(R.id.botao_salvar_tarefa)

        val layoutTitulo = findViewById<TextInputLayout>(R.id.layout_entrada_titulo)
        val campoTitulo = findViewById<TextInputEditText>(R.id.campo_titulo)
        val campoDescricao = findViewById<TextInputEditText>(R.id.campo_descricao)
        val dropdownCategoria = findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_categoria)
        val grupoPrioridade = findViewById<RadioGroup>(R.id.grupo_prioridade)
        val campoData = findViewById<TextInputEditText>(R.id.campo_data_limite)

        // Dropdown de categorias
        //Carrega o array de categorias de arrays.xml.
        val categorias = resources.getStringArray(R.array.task_categories)
        //Cria um adaptador que liga essas strings ao layout padrão de dropdown.
        val adapterCategoria = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        //Associa o adaptador ao campo para que as categorias apareçam ao abrir o dropdown.
        dropdownCategoria.setAdapter(adapterCategoria)

        // DatePicker para data limite
        campoData.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    calendario.set(ano, mes, dia)
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    campoData.setText(formato.format(calendario.time))
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        botaoSalvar.setOnClickListener {
            val titulo = campoTitulo.text.toString().trim()

            if (titulo.isEmpty()) {
                layoutTitulo.error = getString(R.string.erro_titulo_tarefa_vazio)
                return@setOnClickListener
            }

            layoutTitulo.error = null

            val descricao = campoDescricao.text.toString().trim()
            val categoria = dropdownCategoria.text.toString().trim()

            val prioridade = when (grupoPrioridade.checkedRadioButtonId) {
                R.id.opcao_prioridade_baixa -> "Baixa"
                R.id.opcao_prioridade_alta -> "Alta"
                else -> "Média"
            }
            val dataLimite = campoData.text.toString().trim()

            // TODO: Salvar no Room Database
            Toast.makeText(this, R.string.tarefa_salva_sucesso, Toast.LENGTH_SHORT).show()
            finish()
        }

        botaoCancelarTarefa.setOnClickListener { finish() }

        chipVoltar.setOnClickListener { finish() }

        chipCategoriaCadastro.setOnClickListener {
            startActivity(Intent(this, RegistroCategoriaActivity::class.java))
        }
    }
}
