package com.winfred.core.common

import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}

import org.apache.commons.lang3.StringUtils


/**
 *
 * @author kevin
 */
object ApmCommonUtils {

  private val timeZoneLosAngeles = TimeZone.getTimeZone("America/Los_Angeles")

  val date_format_str_mysql = "yyyy-MM-dd HH:mm:ss"
  val data_format_str_minutes = "yyyyMMddHHmm"
  val data_format_str_day = "yyyyMMdd"

  val threadLocal_mysql_df: ThreadLocal[SimpleDateFormat] = new ThreadLocal[SimpleDateFormat]()
  val threadLocal_minute_df: ThreadLocal[SimpleDateFormat] = new ThreadLocal[SimpleDateFormat]()
  val threadLocal_day_df: ThreadLocal[SimpleDateFormat] = new ThreadLocal[SimpleDateFormat]()


  def getMysqlDateFormat(): SimpleDateFormat = {
    var df: SimpleDateFormat = threadLocal_mysql_df.get()
    if (null == df) {
      df = new SimpleDateFormat(date_format_str_mysql)
      df.setTimeZone(timeZoneLosAngeles)
      threadLocal_mysql_df.set(df)
    }
    df
  }


  def getMinutesDateFormat(): SimpleDateFormat = {
    var df: SimpleDateFormat = threadLocal_minute_df.get()
    if (null == df) {
      df = new SimpleDateFormat(data_format_str_minutes)
      df.setTimeZone(timeZoneLosAngeles)
      threadLocal_minute_df.set(df)
    }
    df
  }

  def getDayDateFormat(): SimpleDateFormat = {
    var df: SimpleDateFormat = threadLocal_day_df.get()
    if (null == df) {
      df = new SimpleDateFormat(data_format_str_day)
      df.setTimeZone(timeZoneLosAngeles)
      threadLocal_day_df.set(df)
    }
    df
  }

  /**
   * Long => yyyy-MM-dd HH:mm:ss
   *
   * @param timestamp
   * @return
   */
  def getMySqlDateStr(timestamp: Long): String = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(timestamp)
    calendar.setTimeZone(timeZoneLosAngeles);
    getMysqlDateFormat().format(calendar.getTime)
  }

  /**
   * 时间处理规则, 五分钟分割
   *
   * @param timestamp
   * @return
   */
  def getTimeSizeKeyPerFiveMinutes(timestamp: Long): String = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(timestamp)
    calendar.setTimeZone(timeZoneLosAngeles);
    var minute = calendar.get(Calendar.MINUTE)
    if (minute % 5 == 0) {
      return getMinutesDateFormat().format(calendar.getTime)
    }
    while (minute % 5 != 0) {
      calendar.add(Calendar.MINUTE, 1)
      minute = minute + 1
    }
    getMinutesDateFormat().format(calendar.getTime)
  }

  /**
   * 时间处理规则, 精确到分钟
   *
   * @param timestamp
   * @return
   */
  def getTimeSizeKeyPerMinutes(timestamp: Long): String = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(timestamp)
    calendar.setTimeZone(timeZoneLosAngeles);
    getMinutesDateFormat().format(calendar.getTime)
  }

  /**
   * 时间处理, 天
   *
   * @param timestamp
   * @return
   */
  def getTimeSizeKeyPerDay(timestamp: Long): String = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(timestamp)
    calendar.setTimeZone(timeZoneLosAngeles);
    getDayDateFormat().format(calendar.getTime)
  }

  /**
   * any => Long
   *
   * @param any
   * @return
   */
  def getLongValue(any: Any): Long = {
    val str: String = String.valueOf(any)
    if (StringUtils.isNumeric(str)) {
      return str.toLong
    }
    0L
  }

  /**
   * yyyyMMddHHmm => Long
   *
   * @param dateStr
   * @return
   */
  def getTimestampFromDateMinute(dateStr: String): Long = {
    getMinutesDateFormat().parse(dateStr).getTime
  }

  /**
   * yyyyMMdd => Long
   *
   * @param dateStr
   * @return
   */
  def getTimestampFromDate(dateStr: String): Long = {
    getDayDateFormat().parse(dateStr).getTime
  }
}
