package com.prova.taskflow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.prova.taskflow.data.entity.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistroTaskActivity : AppCompatActivity() {
    private val categoriaRepository get() = (application as TaskFlowApplication).categoriaRepository
    private val taskRepository get() = (application as TaskFlowApplication).taskRepository

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

        val layoutDescricao = findViewById<TextInputLayout>(R.id.layout_entrada_descricao)
        val campoDescricao = findViewById<TextInputEditText>(R.id.campo_descricao)

        val layoutCategoria = findViewById<TextInputLayout>(R.id.layout_entrada_categoria)
        val dropdownCategoria = findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_categoria)

        val layoutPrioridade = findViewById<TextInputLayout>(R.id.layout_entrada_prioridade)
        val grupoPrioridade = findViewById<RadioGroup>(R.id.grupo_prioridade)

        val layoutData = findViewById<TextInputLayout>(R.id.layout_entrada_data_limite)
        val campoData = findViewById<TextInputEditText>(R.id.campo_data_limite)



        // Carregar categorias do banco de dados e configurar o dropdown
        lifecycleScope.launch {
            val lista = withContext(Dispatchers.IO) {
                categoriaRepository.getAllSync()
            }
            val nomes = lista.map { it.nome }
            val adapterCategoria = ArrayAdapter(
                this@RegistroTaskActivity,
                android.R.layout.simple_dropdown_item_1line,
                nomes
            )
            dropdownCategoria.setAdapter(adapterCategoria)
        }



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
            if (descricao.isEmpty()) {
                layoutDescricao.error = getString(R.string.erro_descricao_tarefa_vazio)
                return@setOnClickListener
            }
            layoutDescricao.error = null

            val nomeCategoria = dropdownCategoria.text.toString().trim()
            if (nomeCategoria.isEmpty()) {
                layoutCategoria.error = getString(R.string.erro_categoria_tarefa_vazio)
                return@setOnClickListener
            }
            layoutCategoria.error = null

            if (grupoPrioridade.checkedRadioButtonId == View.NO_ID) {
                layoutPrioridade.error = getString(R.string.erro_prioridade_nao_selecionada)
                return@setOnClickListener
            }
            layoutPrioridade.error = null

            val codigoPrioridade = when (grupoPrioridade.checkedRadioButtonId) {
                R.id.opcao_prioridade_baixa -> 0
                R.id.opcao_prioridade_media -> 1
                R.id.opcao_prioridade_alta -> 2
                else -> 1
            }

            val dataLimite = campoData.text.toString().trim()
            if (dataLimite.isEmpty()) {
                layoutData.error = getString(R.string.erro_data_limite_tarefa_vazio)
                return@setOnClickListener
            }
            layoutData.error = null

            val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dataHora = formatoData.parse(dataLimite)?.time
            if (dataHora == null) {
                layoutData.error = getString(R.string.erro_data_invalida)
                return@setOnClickListener
            }
            layoutData.error = null

            lifecycleScope.launch {
                val categoriaId = withContext(Dispatchers.IO) {
                    categoriaRepository.getByName(nomeCategoria)
                }
                if (categoriaId == null) {
                    layoutCategoria.error = getString(R.string.erro_categoria_tarefa_inexistente)
                    return@launch
                }
                layoutCategoria.error = null

                try {
                    taskRepository.insert(
                        Task(
                            titulo = titulo,
                            descricao = descricao,
                            categoriaId = categoriaId,
                            prioridade = codigoPrioridade,
                            dataHora = dataHora
                        )
                    )
                    Toast.makeText(this@RegistroTaskActivity, R.string.tarefa_salva_sucesso, Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@RegistroTaskActivity, e.message ?: "Erro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        botaoCancelarTarefa.setOnClickListener { finish() }

        chipVoltar.setOnClickListener { finish() }

        chipCategoriaCadastro.setOnClickListener {
            startActivity(Intent(this, RegistroCategoriaActivity::class.java))
        }
    }
}
