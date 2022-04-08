package com.winfred.core.source.entity;

import com.winfred.core.annotation.MockSourceName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author winfred
 */
@MockSourceName(name = "qa_order_item_test")
public class OrderItemEntity implements OrderJoinMock {

    @Setter
    private String id;
    @Setter
    private Long serverTime;

    @Getter
    @Setter
    private String orderId;
    @Getter
    @Setter
    private String sku;
    @Getter
    @Setter
    private Integer quantity;
    @Getter
    @Setter
    private Double purchasePrice;
    @Getter
    @Setter
    private Double retailPrice;
    @Getter
    @Setter
    private Boolean isGift;

    public String getId() {
        if (null == id) {
            this.id = UUID.randomUUID().toString();
        }
        return id;
    }

    public Long getServerTime() {
        if (null == serverTime) {
            this.serverTime = System.currentTimeMillis();
        }
        return serverTime;
    }
}
