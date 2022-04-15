package com.winfred.core.source.entity.ods;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
import com.winfred.core.source.entity.NoteMock;
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
public class NoteReceiptOds implements NoteMock {

    @JsonProperty(value = "primary_key")
    @SerializedName(value = "primary_key")
    @Setter
    private String primary_key;

    @Getter
    @Setter
    private String sp_result;

    @Getter
    @Setter
    private Long sp_send_time;

    @Getter
    @Setter
    private Long channel_receive_time;

    @Getter
    @Setter
    private Long receive_system_time;

    @Override
    public String getPrimaryKey() {

        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }
}
