package com.winfred.core.source.entity.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.winfred.core.annotation.MockSourceName;
import com.winfred.core.source.entity.NoteMock;
import com.winfred.core.source.entity.base.LocalDateTimeDeserializer;
import com.winfred.core.source.entity.base.LocalDateTimeJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * doc: https://platform-wiki.shuyun.com/pages/viewpage.action?pageId=39487003
 *
 * @author winfred
 */
@MockSourceName(name = "note_send_test")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteSendRaw implements NoteMock {

    private static final long serialVersionUID = -5059240231248190358L;

    /**
     * 唯一标识ID
     * <p>
     * 用来匹配的唯一ID：
     * 普通短息：rel_report_key
     * rel_report_key生成规则："发送通道_发送通道账号_messageId"
     * 其他：message_id+receiver
     */
    @JsonProperty(value = "primary_key")
    private String primary_key;
    /**
     * 租户名
     * <p>
     * 例：qiushi6
     */
    private String user_name;
    private String shop_key;

    /**
     * 业务类型
     * <p>
     * 例：TBSMS、TBSMSSINGLE、TBKSMS、SMS、SMSDYNAMIC、EDM、EDMDYNAMIC
     */
    @JsonProperty(value = "type")
    private String business_type;
    private String task_id;
    private String subtask_id;
    private String content;
    private String receiver;
    private String show_id;
    private String gateway_id;

    /**
     * 发送通道账号
     * <p>
     * 例：6SDK-YXX-6688-JCUSO
     */
    private String gateway_account;

    /**
     * 运营商类型
     * <p>
     * 例：lt、dx、yd、other
     */
    private String mobile_type;

    private Long charge_submit_num;

    /**
     * 扩展字段
     * <p>
     * 自定义json转为字符串格式，eg:{"full_name":"123", "campid":"1", "nodeid":"1"}
     */
    private Map<String, String> ext_json;

    /**
     * java: OffsetDateTime
     * flink sql: TIMESTAMP(9) WITH TIME ZONE
     */
    @JsonProperty(value = "business_request_time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeJsonSerializer.class)
    private LocalDateTime business_request_time;

    /**
     * java: OffsetDateTime
     * flink sql: TIMESTAMP(9) WITH TIME ZONE
     */
    @JsonProperty(value = "channel_send_time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeJsonSerializer.class)
    private LocalDateTime channel_send_time;
    /**
     * java: OffsetDateTime
     * flink sql: TIMESTAMP(9) WITH TIME ZONE
     */
    @JsonProperty(value = "submit_system_time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeJsonSerializer.class)
    private LocalDateTime submit_system_time = LocalDateTime.now();

    private String dt;

    @Override
    public String getPrimaryKey() {
        if (null == this.primary_key) {
            this.primary_key = UUID.randomUUID().toString();
        }
        return primary_key;
    }
}
