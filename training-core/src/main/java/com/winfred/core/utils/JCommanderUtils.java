package com.winfred.core.utils;

import com.beust.jcommander.JCommander;

/**
 * @author winfred958
 */
public class JCommanderUtils {

  public static <T> T parseArgs(String[] args, Class<T> clazz) {
    try {
      final T t = clazz.newInstance();
      final JCommander commander = JCommander
          .newBuilder()
          .addObject(t)
          .build();
      commander.parse(args);
      return t;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
