package com.winfred.core.source.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
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
@MockSourceName(name = "note_send_test")
@AllArgsConstructor
@NoArgsConstructor
public class NoteSendEntity implements NoteMock {

    private static final long serialVersionUID = -5059240231248190358L;

    @JsonProperty(value = "primary_key")
    @SerializedName(value = "primary_key")
    @Setter
    private String primary_key;
    @Getter
    @Setter
    private String user_name;
    @Getter
    @Setter
    private String shop_key;
    @Getter
    @Setter
    @JsonProperty(value = "type")
    @SerializedName(value = "type")
    private String business_type;
    @Getter
    @Setter
    private String task_id;
    @Getter
    @Setter
    private String subtask_id;
    @Getter
    @Setter
    private String content;
    @Getter
    @Setter
    private String receiver;
    @Getter
    @Setter
    private String show_id;
    @Getter
    @Setter
    private String gateway_id;
    @Getter
    @Setter
    private String gateway_account;
    @Getter
    @Setter
    private Long charge_submit_num;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", with = JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID, timezone = "Asia/Shanghai")
    @Getter
    @Setter
    private String request_time;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", with = JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID, timezone = "Asia/Shanghai")
    @Getter
    @Setter
    private String send_time;
    @Getter
    @Setter
    private String full_name;
    @Getter
    @Setter
    @JsonProperty(value = "campid")
    @SerializedName(value = "campid")
    private String campaign_id;

    @Getter
    @Setter
    private String nodeid;

    @Override
    public String getPrimaryKey() {
        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }
}
