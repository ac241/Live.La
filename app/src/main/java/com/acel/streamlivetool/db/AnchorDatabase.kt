package com.acel.streamlivetool.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.acel.streamlivetool.bean.Anchor

@Database(entities = [Anchor::class], version = 1)
abstract class AnchorDatabase : RoomDatabase() {
    companion object {
        private lateinit var thisContext: Context
        private val INSTANCE: AnchorDatabase by lazy {
            Room.databaseBuilder(thisContext, AnchorDatabase::class.java, "anchor_database").build()
        }

        fun getInstance(context: Context): AnchorDatabase {
            thisContext = context
            return INSTANCE
        }
    }

    abstract fun getDao(): AnchorDao
}