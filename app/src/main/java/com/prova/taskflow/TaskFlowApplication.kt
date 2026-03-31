package com.prova.taskflow

import android.app.Application
import com.prova.taskflow.data.db.AppDatabase
import com.prova.taskflow.data.repository.CategoriaRepository
import com.prova.taskflow.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskFlowApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val categoriaRepository: CategoriaRepository by lazy {
        CategoriaRepository(database.categoriaDao())
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    suspend fun limparBancoDados() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    override fun onCreate() {
        super.onCreate()

    }
}
