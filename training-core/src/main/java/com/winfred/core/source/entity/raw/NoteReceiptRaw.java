package com.winfred.core.source.entity.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.SerializedName;
import com.winfred.core.annotation.MockSourceName;
import com.winfred.core.source.entity.NoteMock;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
public class NoteReceiptRaw implements NoteMock {

    private static final long serialVersionUID = 8713417573020650030L;

    @JsonProperty(value = "primary_key")
    @SerializedName(value = "primary_key")
    private String primary_key;

    private String sp_result;

    private LocalDateTime sp_send_time;

    private LocalDateTime channel_receive_time;

    private LocalDateTime receive_system_time;

    @Override
    public String getPrimaryKey() {
        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }

    public String getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }

    public String getSp_result() {
        return sp_result;
    }

    public void setSp_result(String sp_result) {
        this.sp_result = sp_result;
    }

    public LocalDateTime getSp_send_time() {
        return sp_send_time;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setSp_send_time(LocalDateTime sp_send_time) {
        this.sp_send_time = sp_send_time;
    }

    @JsonSerialize
    public LocalDateTime getChannel_receive_time() {
        return channel_receive_time;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setChannel_receive_time(LocalDateTime channel_receive_time) {
        this.channel_receive_time = channel_receive_time;
    }


    @JsonSerialize
    public LocalDateTime getReceive_system_time() {
        return receive_system_time;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setReceive_system_time(LocalDateTime receive_system_time) {
        this.receive_system_time = receive_system_time;
    }
}
