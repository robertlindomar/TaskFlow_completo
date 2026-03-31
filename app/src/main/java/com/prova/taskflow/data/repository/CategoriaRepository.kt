package com.prova.taskflow.data.repository

import com.prova.taskflow.data.dao.CategoriaDao
import com.prova.taskflow.data.entity.Categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoriaRepository(
    private val categoriaDao: CategoriaDao) {


    suspend fun insert(categoria: Categoria): Long = withContext(Dispatchers.IO) {
        categoriaDao.insert(categoria)
    }

    suspend fun update(categoria: Categoria) = withContext(Dispatchers.IO) {
        categoriaDao.update(categoria)
    }

    suspend fun delete(categoria: Categoria) = withContext(Dispatchers.IO) {
        categoriaDao.delete(categoria)
    }

    suspend fun getByName(nome: String): Long? = withContext(Dispatchers.IO) {
        categoriaDao.getByName(nome)
    }

    

    fun getAllFlow(): Flow<List<Categoria>> = categoriaDao.getAllFlow()

    fun getAllSync(): List<Categoria> = categoriaDao.getAllSync()


}
