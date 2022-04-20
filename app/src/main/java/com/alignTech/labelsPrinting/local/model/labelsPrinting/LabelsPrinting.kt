package com.alignTech.labelsPrinting.local.model.labelsPrinting

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

@Entity(tableName = "LabelsPrintingTable" , indices = [Index(value = ["barCode"], unique = true)])
@Parcelize
data class LabelsPrinting(
    @PrimaryKey(autoGenerate = true)
    var localId: Int? = null,
    var barCode: String? = null,
    var nameProduct: String? = null,
    var price: Double? = 0.0,
    @Ignore
    var isSelected: Boolean = false
):Parcelable
