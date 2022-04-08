package com.winfred.core.source.entity;

import com.winfred.core.annotation.MockSourceName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author winfred
 */
@MockSourceName(name = "qa_order_test")
public class OrderEntity implements OrderJoinMock {

    @Setter
    private String orderId;

    @Setter
    private Long serverTime;

    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private String orderStatus;
    @Getter
    @Setter
    private String payStatus;
    @Getter
    @Setter
    private String shipStatus;
    @Getter
    @Setter
    private String payType;

    @Override
    public String getOrderId() {
        if (null == orderId) {
            this.orderId = UUID.randomUUID().toString();
        }
        return orderId;
    }

    public Long getServerTime() {
        if (null == serverTime) {
            this.serverTime = System.currentTimeMillis();
        }
        return serverTime;
    }
}
