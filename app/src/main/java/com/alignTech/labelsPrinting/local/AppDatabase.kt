package com.alignTech.labelsPrinting.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alignTech.labelsPrinting.local.deo.LabelsPrintingDao
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting
import com.alignTech.labelsPrinting.local.typeConverters.DateTypeConverter

@Database(
    version = 2 ,
    exportSchema = false,
    entities = [LabelsPrinting::class],
)
@TypeConverters(
    value = [DateTypeConverter::class]
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun labelsPrintingDao(): LabelsPrintingDao

}
