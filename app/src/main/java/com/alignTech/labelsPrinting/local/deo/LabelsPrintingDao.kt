package com.alignTech.labelsPrinting.local.deo

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting

@Dao
interface LabelsPrintingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewLabel(Label: LabelsPrinting): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(Labels: List<LabelsPrinting>): List<Long>

    @Query("SELECT * FROM LabelsPrintingTable ")
    fun getAllLabels(): List<LabelsPrinting>?

    @Query("SELECT * FROM LabelsPrintingTable  ORDER BY localId ASC")
    fun getAllPagingLabels(): PagingSource<Int, LabelsPrinting>

    @Query("SELECT * FROM LabelsPrintingTable  ORDER BY localId DESC") // من الكبير للصغير
    fun getAllLabelsLiveDataDESC(): LiveData<List<LabelsPrinting>>

    @Query("SELECT * FROM LabelsPrintingTable  ORDER BY localId ASC") // من الصغير للكبير
    fun getAllLabelsLiveDataASC(): LiveData<List<LabelsPrinting>>

    @Query("SELECT COUNT(localId) FROM LabelsPrintingTable ")
    fun getAllLabelsCountAsLiveData(): LiveData<Int>

    @Query("SELECT * FROM LabelsPrintingTable WHERE barCode = (:barCode)")
    fun getParentLabelById(barCode: String): LabelsPrinting?

    @Query("SELECT * FROM LabelsPrintingTable WHERE barCode = (:barCode)")
    fun getLabelByIdLiveData(barCode: String): LiveData<LabelsPrinting>

    @Query("DELETE FROM LabelsPrintingTable WHERE localId = (:localId)")
    fun deleteLabel(localId: Int): Int

    @Query("DELETE FROM LabelsPrintingTable")
    fun clearTable()


}