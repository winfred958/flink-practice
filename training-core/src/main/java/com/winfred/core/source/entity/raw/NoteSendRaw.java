package com.winfred.core.source.entity.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.winfred.core.annotation.MockSourceName;
import com.winfred.core.source.entity.NoteMock;
import lombok.AllArgsConstructor;
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

    public String getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getShop_key() {
        return shop_key;
    }

    public void setShop_key(String shop_key) {
        this.shop_key = shop_key;
    }

    public String getBusiness_type() {
        return business_type;
    }

    public void setBusiness_type(String business_type) {
        this.business_type = business_type;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getSubtask_id() {
        return subtask_id;
    }

    public void setSubtask_id(String subtask_id) {
        this.subtask_id = subtask_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getShow_id() {
        return show_id;
    }

    public void setShow_id(String show_id) {
        this.show_id = show_id;
    }

    public String getGateway_id() {
        return gateway_id;
    }

    public void setGateway_id(String gateway_id) {
        this.gateway_id = gateway_id;
    }

    public String getGateway_account() {
        return gateway_account;
    }

    public void setGateway_account(String gateway_account) {
        this.gateway_account = gateway_account;
    }

    public String getMobile_type() {
        return mobile_type;
    }

    public void setMobile_type(String mobile_type) {
        this.mobile_type = mobile_type;
    }

    public Long getCharge_submit_num() {
        return charge_submit_num;
    }

    public void setCharge_submit_num(Long charge_submit_num) {
        this.charge_submit_num = charge_submit_num;
    }

    public Map<String, String> getExt_json() {
        return ext_json;
    }

    public void setExt_json(Map<String, String> ext_json) {
        this.ext_json = ext_json;
    }

    public LocalDateTime getBusiness_request_time() {
        return business_request_time;
    }

    public void setBusiness_request_time(LocalDateTime business_request_time) {
        this.business_request_time = business_request_time;
    }

    public LocalDateTime getChannel_send_time() {
        return channel_send_time;
    }

    public void setChannel_send_time(LocalDateTime channel_send_time) {
        this.channel_send_time = channel_send_time;
    }

    public LocalDateTime getSubmit_system_time() {
        return submit_system_time;
    }


    public void setSubmit_system_time(LocalDateTime submit_system_time) {
        this.submit_system_time = submit_system_time;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }
}
