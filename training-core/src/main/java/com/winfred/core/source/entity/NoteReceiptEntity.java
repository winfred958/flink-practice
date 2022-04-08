package com.winfred.core.source.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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
    @Getter
    @Setter
    private LocalDateTime send_time;
    @Getter
    @Setter
    private LocalDateTime receive_time;
    @Getter
    @Setter
    private Long bill_count;
    @Getter
    @Setter
    private String system_time;

    @Override
    public String getPrimaryKey() {
        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }
}
