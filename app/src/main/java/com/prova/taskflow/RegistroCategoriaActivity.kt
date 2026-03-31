package com.prova.taskflow

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.prova.taskflow.data.entity.Categoria
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegistroCategoriaActivity : AppCompatActivity() {
    private val categoriaRepository get() = (application as TaskFlowApplication).categoriaRepository
    private var categoriaEditando: Categoria? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_categoria)



        val layoutNome = findViewById<TextInputLayout>(R.id.layout_entrada_nome_categoria)
        val campoNome = findViewById<TextInputEditText>(R.id.campo_nome_categoria)
        val botaoSalvar = findViewById<MaterialButton>(R.id.botao_salvar_categoria)
        val botaoVoltar = findViewById<MaterialButton>(R.id.botao_voltar_categoria)
        val containerCategorias = findViewById<LinearLayout>(R.id.container_categorias)


        // Configura o layout para respeitar as áreas de sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_principal_categoria)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Carrega a lista de categorias do banco e observa mudanças
        lifecycleScope.launch {
            categoriaRepository.getAllFlow().collect { categorias ->
                atualizarLista(categorias, containerCategorias, campoNome, botaoSalvar)
            }
        }


        botaoSalvar.setOnClickListener {
            val nome = campoNome.text.toString().trim()
            if (nome.isEmpty()) {
                layoutNome.error = getString(R.string.erro_nome_categoria_vazio)
            } else {
                layoutNome.error = null
                lifecycleScope.launch {
                    try {
                        if (categoriaEditando != null) {
                            categoriaRepository.update(categoriaEditando!!.copy(nome = nome))
                            Toast.makeText(this@RegistroCategoriaActivity, R.string.categoria_atualizada_sucesso, Toast.LENGTH_SHORT).show()
                        } else {
                            categoriaRepository.insert(Categoria(nome = nome))
                            Toast.makeText(this@RegistroCategoriaActivity, getString(R.string.categoria_salva_sucesso, nome), Toast.LENGTH_SHORT).show()
                        }
                        categoriaEditando = null
                        campoNome.text?.clear()
                        botaoSalvar.setText(R.string.botao_salvar_categoria)
                    } catch (e: Exception) {
                        layoutNome.error = e.message ?: "Erro"
                    }
                }
            }
        }

        botaoVoltar.setOnClickListener { finish() }
    }

    private fun atualizarLista(
        categorias: List<Categoria>,
        containerCategorias: LinearLayout,
        campoNome: TextInputEditText,
        botaoSalvar: MaterialButton
    ) {


        containerCategorias.removeAllViews()
        for (categoria in categorias) {
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_categoria, containerCategorias, false)
            itemView.findViewById<TextView>(R.id.texto_nome_categoria).text = categoria.nome

            // Configura os botões de editar e excluir para cada categoria
            itemView.findViewById<MaterialButton>(R.id.botao_editar).setOnClickListener {
                categoriaEditando = categoria
                campoNome.setText(categoria.nome)
                campoNome.setSelection(categoria.nome.length)
                botaoSalvar.setText(R.string.botao_atualizar_categoria)
            }

            // Configura o botão de excluir para cada categoria
            itemView.findViewById<MaterialButton>(R.id.botao_excluir).setOnClickListener {
                AlertDialog.Builder(this)  // Cria um diálogo de confirmação para a exclusão da categoria
                    .setMessage(getString(R.string.confirmar_exclusao, categoria.nome)) // Exibe uma mensagem de confirmação para o usuário
                    .setPositiveButton(R.string.sim) { _, _ -> // Se o usuário confirmar a exclusão, executa a operação de exclusão
                        lifecycleScope.launch {
                            try {
                                categoriaRepository.delete(categoria)
                                Toast.makeText(this@RegistroCategoriaActivity, R.string.categoria_excluida_sucesso, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@RegistroCategoriaActivity, e.message ?: "Erro ao excluir", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton(R.string.nao, null)
                    .show()
            }
            containerCategorias.addView(itemView)
        }
    }
}
