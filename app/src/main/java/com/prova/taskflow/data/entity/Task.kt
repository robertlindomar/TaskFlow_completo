package com.prova.taskflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tasks")
data class Task (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descricao: String,
    val categoriaId: Long,
    val prioridade: Int,
    val dataHora: Long,
    val concluida: Boolean = false
)


