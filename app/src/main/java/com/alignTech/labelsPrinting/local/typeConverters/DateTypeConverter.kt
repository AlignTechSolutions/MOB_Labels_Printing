package com.alignTech.labelsPrinting.local.typeConverters

import androidx.room.TypeConverter
import java.util.*

class DateTypeConverter {
    @TypeConverter
    fun fromDateToString(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun fromStringToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}