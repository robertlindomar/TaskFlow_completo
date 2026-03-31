package com.prova.taskflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prova.taskflow.data.dao.CategoriaDao
import com.prova.taskflow.data.dao.TaskDao
import com.prova.taskflow.data.entity.Categoria
import com.prova.taskflow.data.entity.Task

@Database(
    entities = [Categoria::class, Task::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao

    abstract fun taskDao(): TaskDao

    companion object {
        private const val DATABASE_NAME = "taskflow.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
