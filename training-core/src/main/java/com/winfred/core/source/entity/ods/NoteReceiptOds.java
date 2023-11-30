package com.winfred.core.source.entity.ods;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
import com.winfred.core.source.entity.NoteMock;
import com.winfred.core.source.entity.base.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * doc: https://platform-wiki.shuyun.com/pages/viewpage.action?pageId=39487003
 *
 * @author winfred
 */
@MockSourceName(name = "note_receipt_test")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NoteReceiptOds implements NoteMock {

  private static final long serialVersionUID = -1731119476212623361L;

  @JsonProperty(value = "primary_key")
  @SerializedName(value = "primary_key")
  private String primary_key;

  private String sp_result;
  private Long sp_charge_submit_num;

  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private String sp_send_time;

  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private String channel_receive_time;

  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private String receive_system_time;

  private String dt;

  @Override
  public String getPrimaryKey() {
    if (null == this.primary_key) {
      this.primary_key = UUID.randomUUID().toString();
    }
    return primary_key;
  }
}
