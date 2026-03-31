package com.prova.taskflow

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.prova.taskflow.data.entity.Categoria
import com.prova.taskflow.data.entity.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private enum class FiltroTarefa { AFazer, Concluidas }

    private val filtroTarefas = MutableStateFlow(FiltroTarefa.AFazer)

    private val categoriaRepository get() = (application as TaskFlowApplication).categoriaRepository
    private val taskRepository get() = (application as TaskFlowApplication).taskRepository

    private fun app() = application as TaskFlowApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.chip_nova_tarefa).setOnClickListener {
            startActivity(Intent(this, RegistroTaskActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.chip_categoria).setOnClickListener {
            startActivity(Intent(this, RegistroCategoriaActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.botao_dados_ficticios).setOnClickListener {
            lifecycleScope.launch {
                runCatching {
                    popularDadosFicticios()
                }.onSuccess {
                    Toast.makeText(this@MainActivity, R.string.toast_dados_ficticios_ok, Toast.LENGTH_SHORT)
                        .show()
                }.onFailure { e ->
                    Toast.makeText(
                        this@MainActivity,
                        e.message ?: getString(R.string.erro_generico),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        findViewById<MaterialButton>(R.id.botao_limpar_banco).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_limpar_banco_titulo)
                .setMessage(R.string.confirmar_limpar_banco_mensagem)
                .setPositiveButton(R.string.sim) { _, _ ->
                    lifecycleScope.launch {
                        runCatching {
                            app().limparBancoDados()
                        }.onSuccess {
                            Toast.makeText(this@MainActivity, R.string.toast_banco_limpo, Toast.LENGTH_SHORT)
                                .show()
                        }.onFailure { e ->
                            Toast.makeText(
                                this@MainActivity,
                                e.message ?: getString(R.string.erro_generico),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton(R.string.nao, null)
                .show()
        }

        val grupoFiltro = findViewById<MaterialButtonToggleGroup>(R.id.barra_filtro_tarefas)
        grupoFiltro.check(R.id.botao_filtro_a_fazer)
        grupoFiltro.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            filtroTarefas.value = when (checkedId) {
                R.id.botao_filtro_concluidas -> FiltroTarefa.Concluidas
                else -> FiltroTarefa.AFazer
            }
        }

        val containerLista = findViewById<LinearLayout>(R.id.lista_tarefas)
        val scrollLista = findViewById<View>(R.id.scroll_lista_tarefas)
        val textoEstadoVazio = findViewById<TextView>(R.id.texto_estado_vazio)
        val inflater = LayoutInflater.from(this)
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    taskRepository.getAllFlow(),
                    categoriaRepository.getAllFlow(),
                    filtroTarefas
                ) { todasTarefas, categorias, filtro ->
                    val nomesPorId = categorias.associate { it.id to it.nome }
                    val tarefasFiltradas = when (filtro) {
                        FiltroTarefa.AFazer -> todasTarefas.filter { !it.concluida }
                        FiltroTarefa.Concluidas -> todasTarefas.filter { it.concluida }
                    }
                    Triple(todasTarefas, tarefasFiltradas, nomesPorId) to filtro
                }.collect { (triple, filtro) ->
                    val (todasTarefas, tarefasFiltradas, nomesPorId) = triple
                    val vazio = tarefasFiltradas.isEmpty()
                    textoEstadoVazio.visibility = if (vazio) View.VISIBLE else View.GONE
                    scrollLista.visibility = if (vazio) View.GONE else View.VISIBLE

                    textoEstadoVazio.text = when {
                        todasTarefas.isEmpty() -> getString(R.string.mensagem_estado_vazio)
                        filtro == FiltroTarefa.AFazer -> getString(R.string.mensagem_nenhuma_a_fazer)
                        else -> getString(R.string.mensagem_nenhuma_concluida)
                    }

                    containerLista.removeAllViews()
                    for (task in tarefasFiltradas) {
                        val item = inflater.inflate(R.layout.item_tarefa, containerLista, false)
                        ligarItemTarefa(item, task, nomesPorId, formatoData)
                        containerLista.addView(item)
                    }
                }
            }
        }
    }

    private suspend fun popularDadosFicticios() = withContext(Dispatchers.IO) {
        var categorias = categoriaRepository.getAllSync()
        if (categorias.isEmpty()) {
            listOf("Trabalho", "Estudo", "Pessoal").forEach { nome ->
                categoriaRepository.insert(Categoria(nome = nome))
            }
            categorias = categoriaRepository.getAllSync()
        }
        val idsCategoria = categorias.map { it.id }
        if (idsCategoria.isEmpty()) return@withContext

        val agora = System.currentTimeMillis()
        val umDia = 86_400_000L
        val amostras = listOf(
            "Revisar documentação" to "Ler capítulos 3 e 4 até sexta.",
            "Daily stand-up" to "Participar da reunião às 9h.",
            "Prova de Android" to "Revisar Room e Coroutines.",
            "Mercado" to "Comprar leite e pão.",
            "Academia" to "Treino de pernas.",
            "Ligar para o cliente" to "Confirmar entrega do projeto.",
            "Relatório mensal" to "Enviar até o dia 30.",
            "Backup do projeto" to "Compactar e enviar para nuvem.",
            "Ler artigo Room" to "Documentação oficial Android.",
            "Planejar fim de semana" to "Checar previsão do tempo."
        )
        amostras.forEachIndexed { i, par ->
            taskRepository.insert(
                Task(
                    titulo = par.first,
                    descricao = par.second,
                    categoriaId = idsCategoria[i % idsCategoria.size],
                    prioridade = i % 3,
                    dataHora = agora + (i + 1) * umDia,
                    concluida = i % 4 == 0
                )
            )
        }
    }

    private fun ligarItemTarefa(
        item: View,
        task: Task,
        nomesPorId: Map<Long, String>,
        formatoData: SimpleDateFormat
    ) {
        val titulo = item.findViewById<TextView>(R.id.texto_titulo_tarefa)
        val descricao = item.findViewById<TextView>(R.id.texto_descricao_tarefa)
        val meta = item.findViewById<TextView>(R.id.texto_meta_tarefa)
        val badgeConcluida = item.findViewById<TextView>(R.id.texto_badge_concluida)
        val botaoConcluir = item.findViewById<MaterialButton>(R.id.botao_concluir_tarefa)
        val botaoExcluir = item.findViewById<MaterialButton>(R.id.botao_excluir_tarefa)

        titulo.text = task.titulo
        descricao.text = task.descricao
        val nomeCat = nomesPorId[task.categoriaId] ?: getString(R.string.dica_categoria)
        val prioridadeStr = when (task.prioridade) {
            0 -> getString(R.string.prioridade_baixa)
            2 -> getString(R.string.prioridade_alta)
            else -> getString(R.string.prioridade_media)
        }
        val corPrioridade = ContextCompat.getColor(
            item.context,
            when (task.prioridade) {
                0 -> R.color.prioridade_baixa
                2 -> R.color.prioridade_alta
                else -> R.color.prioridade_media
            }
        )
        val dataStr = formatoData.format(Date(task.dataHora))
        meta.text = SpannableStringBuilder().apply {
            append(nomeCat)
            append(" · ")
            val inicioPrioridade = length
            append(prioridadeStr)
            setSpan(
                ForegroundColorSpan(corPrioridade),
                inicioPrioridade,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append(" · ")
            append(dataStr)
        }

        botaoExcluir.setOnClickListener {
            lifecycleScope.launch { taskRepository.delete(task) }
        }

        if (task.concluida) {
            titulo.paintFlags = titulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            descricao.paintFlags = descricao.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            badgeConcluida.visibility = View.VISIBLE
            botaoConcluir.visibility = View.GONE
            botaoConcluir.setOnClickListener(null)
        } else {
            titulo.paintFlags = titulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            descricao.paintFlags = descricao.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            badgeConcluida.visibility = View.GONE
            botaoConcluir.visibility = View.VISIBLE
            botaoConcluir.setOnClickListener {
                lifecycleScope.launch {
                    taskRepository.update(task.copy(concluida = true))
                }
            }
        }
    }
}
