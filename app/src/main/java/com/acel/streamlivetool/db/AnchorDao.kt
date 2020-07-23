package com.acel.streamlivetool.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.acel.streamlivetool.bean.Anchor

@Dao
interface AnchorDao {
    @Insert
    fun insertAnchor(anchor: Anchor)

    @Delete
    fun deleteAnchor(anchor: Anchor)

    @Update
    fun updateAnchor(anchor: Anchor)

    @Query("SELECT * FROM Anchor ORDER BY ID ")
    fun getAllAnchors(): LiveData<MutableList<Anchor>>

    @Query("DELETE FROM Anchor")
    fun deleteAllAnchors()

}