package com.winfred.streamming.kafka;

import com.alibaba.fastjson2.JSON;
import com.beust.jcommander.Parameter;
import com.winfred.core.entity.log.EventEntity;
import com.winfred.core.entity.log.EventHeader;
import com.winfred.core.sink.FlinkKafkaSink;
import com.winfred.core.source.DataMockSource;
import com.winfred.core.utils.JCommanderUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author winfred958
 */
public class KafkaMockSourceJ {

  public static void main(String[] args) throws Exception {
    final ArgsParameter argsPar = JCommanderUtils.parseArgs(new String[]{"--name", "xxx", "--help"}, ArgsParameter.class);
    final String name = argsPar.getName();
    final Boolean help = argsPar.getHelp();

    final StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    final DataStreamSource<EventEntity> dataStreamSource = executionEnvironment.addSource(new DataMockSource(100, 200));

    dataStreamSource
        .map(entity -> {
          final String uuid = entity.getUuid();
          entity.setUuid(uuid + ":test");
          return entity;
        })
        .filter(entity -> {
          final EventHeader header = entity.getHeader();
          final String visitorId = header.getVisitorId();
          return !StringUtils.isBlank(visitorId);
        })
        .map(value -> {
          return JSON.toJSONString(value);
        })
        .sinkTo(FlinkKafkaSink.getKafkaSink("test"));
    executionEnvironment.execute("");
  }

  @Getter
  @Setter
  public static class ArgsParameter {

    @Parameter(names = {"-n", "--name"}, description = "name")
    private String name;

    @Parameter(names = {"-h", "--help"}, description = "help", help = true)
    private Boolean help;
  }
}
