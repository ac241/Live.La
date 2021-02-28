package com.acel.streamlivetool.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor

@Database(entities = [Anchor::class], version = 1, exportSchema = false)
abstract class AnchorDatabase : RoomDatabase() {
    companion object {
        private val INSTANCE: AnchorDatabase by lazy {
            Room.databaseBuilder(
                MyApplication.application,
                AnchorDatabase::class.java,
                "anchor_database"
            )
                .build()
        }

        fun getInstance(): AnchorDatabase {
            return INSTANCE
        }
    }

    abstract fun getDao(): AnchorDao
}