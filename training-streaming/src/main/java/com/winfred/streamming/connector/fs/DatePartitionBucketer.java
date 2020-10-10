package com.winfred.streamming.connector.fs;

import org.apache.flink.streaming.connectors.fs.Clock;
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer;
import org.apache.hadoop.fs.Path;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DatePartitionBucketer<T> implements Bucketer<T> {

  private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd";

  private TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");

  private SimpleDateFormat simpleDateFormat;

  public DatePartitionBucketer() {
    simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT_STRING);
    simpleDateFormat.setTimeZone(timeZone);
  }

  @Override
  public Path getBucketPath(Clock clock, Path basePath, T element) {

    long timeMillis = getTimeMillis(clock, element);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);
    calendar.setTimeZone(timeZone);
    String childPath = "dt=" + simpleDateFormat.format(calendar.getTime());
    return new Path(basePath + "/" + childPath);
  }

  private long getTimeMillis(Clock clock, T element) {
    long timeMillis = clock.currentTimeMillis();
    if (null == element) {
      return timeMillis;
    }
    try {
      Field field = element.getClass().getDeclaredField("server_time");
      field.setAccessible(true);
      timeMillis = Long.valueOf(String.valueOf(field.get(element)));
    } catch (NoSuchFieldException e) {
    } catch (IllegalAccessException e) {
    } catch (NumberFormatException e) {
    }
    return timeMillis;
  }
}
