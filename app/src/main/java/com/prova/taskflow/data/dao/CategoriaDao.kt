package com.prova.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prova.taskflow.data.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert
    fun insert(categoria: Categoria): Long

    @Update
    fun update(categoria: Categoria)

    @Delete
    fun delete(categoria: Categoria)

    @Query("SELECT id FROM categorias WHERE nome = :nome")
    fun getByName(nome: String): Long?

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    fun getAllFlow(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    fun getAllSync(): List<Categoria>
}
