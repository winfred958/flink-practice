package com.winfred.core.entity.log;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author winfred
 */
public class EventEntity implements Serializable {
  private static final long serialVersionUID = 7258184194640553341L;

  @Setter
  private String uuid;

  @Setter
  @JSONField(name = "server_time")
  private Long serverTime;

  @Getter
  @Setter
  private String source;

  @Setter
  private EventHeader header;

  @Setter
  private EventBody body;

  public String getUuid() {
    if (StringUtils.isBlank(this.uuid)) {
      this.uuid = UUID.randomUUID().toString();
    }
    return uuid;
  }

  public Long getServerTime() {
    if (this.serverTime == null) {
      this.serverTime = System.currentTimeMillis();
    }
    return serverTime;
  }

  public EventHeader getHeader() {
    if (this.header == null) {
      this.header = new EventHeader();
    }
    return header;
  }

  public EventBody getBody() {
    if (this.body == null) {
      this.body = new EventBody();
    }
    return body;
  }
}
