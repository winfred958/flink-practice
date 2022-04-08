package com.winfred.core.source.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * doc: https://platform-wiki.shuyun.com/pages/viewpage.action?pageId=39487003
 *
 * @author winfred
 */
@MockSourceName(name = "note_receipt_test")
@AllArgsConstructor
@NoArgsConstructor
public class NoteReceiptEntity implements NoteMock {

    private static final long serialVersionUID = 8713417573020650030L;

    @JsonProperty(value = "primary_key")
    @SerializedName(value = "primary_key")
    @Setter
    private String primary_key;
    @Getter
    @Setter
    private String receiver;
    @Getter
    @Setter
    private String error_code;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS",with = JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID, timezone = "Asia/Shanghai")
    @Getter
    @Setter
    private LocalDateTime send_time;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS",with = JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID, timezone = "Asia/Shanghai")
    @Getter
    @Setter
    private LocalDateTime receive_time;
    @Getter
    @Setter
    private Long bill_count;

    @Override
    public String getPrimaryKey() {
        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }
}
