package com.winfred.core.source;

import cn.hutool.crypto.digest.MD5;
import com.winfred.core.source.entity.OrderEntity;
import com.winfred.core.source.entity.OrderItemEntity;
import com.winfred.core.source.entity.OrderJoinMock;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author winfred
 */
public class JoinDataMockSource extends RichParallelSourceFunction<OrderJoinMock> {

    private long intervalMillisecondMin;
    private long intervalMillisecondMax;

    private volatile boolean isRun;

    public JoinDataMockSource(long intervalMillisecondMin, long intervalMillisecondMax) {
        this.intervalMillisecondMin = intervalMillisecondMin;
        this.intervalMillisecondMax = intervalMillisecondMax;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        isRun = true;
    }

    @Override
    public void run(SourceContext<OrderJoinMock> ctx) throws Exception {
        Object lock = ctx.getCheckpointLock();
        while (isRun) {
            synchronized (lock) {
                // mock order
                final OrderEntity order = mockOrder();
                ctx.collect(order);
                // mock order items
                final List<OrderItemEntity> orderItemEntities = mockOrderItems(order, 1, 10);
                orderItemEntities
                    .forEach(ctx::collect);
            }
            Thread.sleep(RandomUtils.nextLong(intervalMillisecondMin, intervalMillisecondMax));
        }
    }

    @Override
    public void cancel() {
        isRun = false;
    }

    private OrderEntity mockOrder() {
        final OrderEntity order = new OrderEntity();
        final String userId = MD5.create().digestHex(String.valueOf(RandomUtils.nextLong(800000000, 900000000L)), StandardCharsets.UTF_8);
        order.setUserId(userId);

        order.setOrderStatus("PAID");
        order.setPayStatus("PAID");
        order.setShipStatus("WAIT_PROCESSING_WAREHOUSE");

        order.setPayType("ZFB");
        return order;
    }

    private List<OrderItemEntity> mockOrderItems(OrderEntity order, int min, int max) {
        final int size = RandomUtils.nextInt(min, max);
        List<OrderItemEntity> result = new ArrayList<>(size);
        final String orderId = order.getOrderId();
        for (int i = 0; i < size; i++) {
            result.add(mockOrderItem(orderId));
        }
        return result;
    }

    private OrderItemEntity mockOrderItem(String orderId) {
        final OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderId(orderId);

        itemEntity.setQuantity(RandomUtils.nextInt(1, 20));
        final double purchasePrice = RandomUtils.nextDouble(10, 100);
        itemEntity.setPurchasePrice(purchasePrice);
        itemEntity.setRetailPrice(RandomUtils.nextDouble(purchasePrice * 1.2, purchasePrice * 1.8));

        itemEntity.setIsGift(false);

        final String sku = MD5.create().digestHex(String.valueOf(RandomUtils.nextLong(50000000, 100000000L)), StandardCharsets.UTF_8);
        itemEntity.setSku(sku);
        return itemEntity;
    }
}
