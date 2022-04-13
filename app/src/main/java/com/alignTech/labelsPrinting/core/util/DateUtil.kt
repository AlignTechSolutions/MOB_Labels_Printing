@file:Suppress("DEPRECATION")

package com.alignTech.labelsPrinting.core.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtil {
  private const val MILLIS_IN_SECOND: Long = 1000
  private const val MILLIS_IN_MINUTE: Long = 60 * MILLIS_IN_SECOND
  private const val MILLIS_IN_HOUR: Long = 60 * MILLIS_IN_MINUTE
  private const val MILLIS_IN_DAY: Long = 24 * MILLIS_IN_HOUR

  const val timeFormatAM_PM: String = "hh:mm:ss a"
  const val monthDayDateFormat: String = "d MMM"
  const val SlashShortDateFormat: String = "dd/MM/yyyy"
  const val dashLongDateFormatWithMs: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  const val dashLongDateFormat: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  const val dashLongDateTimeFormat: String = "dd/MM/yyyy hh:mm:ss a"
  const val dashLongDateTimeFormat2: String = "dd/MM/yyyy hh:mm a"
  const val fullDateFormat: String = "EEEE, d MMM 'at' hh:mm aaa"
  const val dayMonthDateFormat: String = "dd/MM/yyyy"
  const val dayMonthDateDashFormat: String = "yyyy-MM-dd"
  const val MonthYearDateFormat: String = "MM/yy"
  const val YearMonthDateFormat: String = "yy/MM"
  const val SERVER_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss a"
  const val IMAGE_DATE_FORMAT = "yyyy-MM-dd-hh-mm-ss-SSS"


  fun getDateString(dateString: String, dateFormat: String): String {
    return formatSelectedDate(dateFormat, dateString)
  }

  fun formatSelectedDate(dateFormat: String, dateString: String): String {
    val df = SimpleDateFormat(dateFormat, Locale.US)
    val newFormat = SimpleDateFormat(SlashShortDateFormat, Locale.US)
    return newFormat.format(df.parse(dateString)!!)
  }

  fun formatCalendar(calendar: Calendar, format: String): String {
    val simpleDateFormat = SimpleDateFormat(format, Locale.US)
    simpleDateFormat.timeZone
    return simpleDateFormat.format(calendar.time)
  }

  private fun getStringFromLongIfNot0(long: Long, value: String): String {
    return if (long > 0)
      long.toString() + value
    else ""
  }

  fun getCurrentCalendar(): Calendar {
    return Calendar.getInstance()
  }

//  fun getWeekDay():Int {
//    Date().day
//  }

  fun addYearsToCurrentDate(years: Int): Date {
    return getCurrentCalendar().apply { add(Calendar.YEAR, years) }.time
  }

  fun formatDate(date: Date?, format: String): String {
    val calendar = Calendar.getInstance()
    date?.let {
      calendar.time = date
    }
    return formatCalendar(calendar, format)
  }

  fun getAPIFormat(date: Date?): String {
    return formatDate(date, dashLongDateFormatWithMs)
  }

  fun getDateFromString(dateString: String?, dateFormat: String): Date {
    return try {
      SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString.orEmpty()) ?: Date()
    } catch (e: java.lang.Exception) {
      Date()
    }

  }

  fun getDateFromServerFormat(dateString: String?): Date {
    return getDateFromString(dateString, dashLongDateFormatWithMs)
  }

  fun getDateFromMilliseconds(milliseconds: Long?): String {
    milliseconds?.let {
      var dateCalendar = getCurrentCalendar()
      dateCalendar.timeInMillis = it
      return formatCalendar(dateCalendar, dayMonthDateFormat)
    }
    return ""
  }

  fun getCurrentDateTime(): Date? {
    val dateTime = Date(System.currentTimeMillis())
    dateTime.seconds = 0
    return dateTime
  }

  fun formatSelectedDate(currentDateFormat: String, newDateFormat: String, dateString: String): String {
    val df = SimpleDateFormat(currentDateFormat, Locale.US)
    val newFormat = SimpleDateFormat(newDateFormat, Locale.US)
    var date = Date()
    try {
      date = df.parse(dateString)
    } catch (e: Exception) {
    }
    return newFormat.format(date)
  }

  fun getDurationFromTime(value: Long, day: String, hour: String): String {
    var mutableValue = value
    val days = TimeUnit.MILLISECONDS.toDays(mutableValue)
    mutableValue %= DateUtils.DAY_IN_MILLIS
    val hours = TimeUnit.MILLISECONDS.toHours(mutableValue)
    return getStringFromLongIfNot0(days, " $day ") + getStringFromLongIfNot0(hours, " $hour")
  }

  fun getMonthYearDateFormat(stringDate: String?): String? {
    return stringDate?.let {
      formatSelectedDate(
        dashLongDateFormatWithMs, MonthYearDateFormat,
        it
      )
    }
  }

  fun getDaysDifference(startDate: Long?, endDate: Long?): Long {
    if (startDate != null && endDate != null)
      return TimeUnit.MILLISECONDS.toDays((endDate - startDate))
    return 0
  }

  fun isDateTimeAfterByDateFormat(currentDate: Date?, targetDate: Date?): Boolean {
    var isAfter = true
    currentDate?.let {
      targetDate?.let {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = currentDate
        val targetCalendar = Calendar.getInstance()
        targetCalendar.time = targetDate
        isAfter = isDateTimeAfterByCalendarFormat(currentCalendar, targetCalendar)
      }
    }
    return isAfter
  }

  fun isDateAfterByDateFormat(currentDate: Date?, targetDate: Date?): Boolean {
    var isAfter = true
    currentDate?.let {
      targetDate?.let {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = currentDate
        val targetCalendar = Calendar.getInstance()
        targetCalendar.time = targetDate
        isAfter = isDateAfterByCalendarFormat(currentCalendar, targetCalendar)
      }
    }
    return isAfter
  }

  fun isTimeAfterByDateFormat(currentDate: Date?, targetDate: Date?): Boolean {
    var isAfter = true
    currentDate?.let {
      targetDate?.let {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = currentDate
        val targetCalendar = Calendar.getInstance()
        targetCalendar.time = targetDate
        isAfter = isDateAfterByCalendarFormat(currentCalendar, targetCalendar)
      }
    }
    return isAfter
  }

  fun isTimeAfterByCalendarFormat(currentDate: Calendar?, targetDate: Calendar?): Boolean {
    targetDate?.let { target ->
      currentDate?.let { current ->
        val currentHour: Int = current[Calendar.HOUR_OF_DAY]
        val currentMinute: Int = current[Calendar.MINUTE]
        val targetHour: Int = target[Calendar.HOUR_OF_DAY]
        val targetMinute: Int = target[Calendar.MINUTE]
        return (currentHour > targetHour || (currentHour == targetHour && currentMinute > targetMinute))
      }
    }
    return true
  }

  fun isDateAfterByCalendarFormat(currentDate: Calendar?, targetDate: Calendar?): Boolean {
    targetDate?.let {
      currentDate?.let { current ->
        val currentYear: Int = current[Calendar.YEAR]
        val currentMonth: Int = current[Calendar.MONTH]
        val currentDay: Int = current[Calendar.DAY_OF_MONTH]
        val targetYear: Int = current[Calendar.YEAR]
        val targetMonth: Int = current[Calendar.MONTH]
        val targetDay: Int = current[Calendar.DAY_OF_MONTH]
        return (currentYear > targetYear || currentMonth > targetMonth || currentDay > targetDay)
      }
    }
    return true
  }

  fun isDateTimeAfterByCalendarFormat(currentDate: Calendar?, targetDate: Calendar?): Boolean {
    targetDate?.let { target ->
      currentDate?.let { current ->
        val currentYear: Int = current[Calendar.YEAR]
        val currentMonth: Int = current[Calendar.MONTH]
        val currentDay: Int = current[Calendar.DAY_OF_MONTH]
        val currentHour: Int = current[Calendar.HOUR_OF_DAY]
        val currentMinute: Int = current[Calendar.MINUTE]
        val targetHour: Int = target[Calendar.HOUR_OF_DAY]
        val targetMinute: Int = target[Calendar.MINUTE]
        val targetYear: Int = current[Calendar.YEAR]
        val targetMonth: Int = current[Calendar.MONTH]
        val targetDay: Int = current[Calendar.DAY_OF_MONTH]
        return (currentYear > targetYear || currentMonth > targetMonth || currentDay > targetDay || currentHour > targetHour || (currentHour == targetHour && currentMinute > targetMinute))
      }
    }
    return true
  }

  fun getCalendarFromDate(value: Date?): Calendar? {
    value?.let {
      val calendar = Calendar.getInstance()
      calendar.time = value
      return calendar
    }
    return null
  }

  fun getDateBefore(numberOfDays: Int, date: Date): Date {
    return Date(date.time - (1000 * 60 * 60 * 24 * numberOfDays))
  }

}