package com.bendfiercely.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StretchSession::class, SessionStretch::class],
    version = 1,
    exportSchema = false
)
abstract class SessionDatabase : RoomDatabase() {
    
    abstract fun sessionDao(): SessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: SessionDatabase? = null
        
        fun getDatabase(context: Context): SessionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SessionDatabase::class.java,
                    "bend_fiercely_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

