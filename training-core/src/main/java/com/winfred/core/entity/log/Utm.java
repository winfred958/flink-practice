package com.winfred.core.entity.log;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author winfred958
 */
@Data
public class Utm implements Serializable {
  private static final long serialVersionUID = -5238592026075790986L;

  @JSONField(name = "utm_channel")
  private String utmChannel;
  @JSONField(name = "utm_source")
  private String utmSource;
  @JSONField(name = "utm_medium")
  private String utmMedium;
  @JSONField(name = "utm_campaign")
  private String utmCampaign;
  @JSONField(name = "utm_term")
  private String utmTerm;
  @JSONField(name = "utm_content")
  private String utmContent;
  @JSONField(name = "utm_visittime")
  private String utmVisittime;
}
