package com.winfred.core.entity.log;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author winfred
 */
public class EventBody implements Serializable {

  private static final long serialVersionUID = -7588044156994131696L;

  @Getter
  @Setter
  @JSONField(name = "event_name")
  private String eventName;

  @Getter
  @Setter
  @JSONField(name = "event_type")
  private String eventType;
  /**
   * 业务相关参数
   */

  @Getter
  @Setter
  private List<Parameter> parameters;

  @Setter
  private Utm utm;

  @Data
  @AllArgsConstructor
  public static class Parameter {
    private String name;
    private String value;
  }


  public Utm getUtm() {
    if (this.utm == null) {
      this.utm = new Utm();
    }
    return utm;
  }
}
