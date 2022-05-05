package com.winfred.core.source;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.winfred.core.source.entity.NoteMock;
import com.winfred.core.source.entity.raw.NoteReceiptRaw;
import com.winfred.core.source.entity.raw.NoteSendRaw;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author winfred
 */
public class NoteMessageMockSource extends RichParallelSourceFunction<NoteMock> {

    private long intervalMillisecondMin;
    private long intervalMillisecondMax;

    private volatile boolean isRun;

    private static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public NoteMessageMockSource(long intervalMillisecondMin, long intervalMillisecondMax) {
        this.intervalMillisecondMin = intervalMillisecondMin;
        this.intervalMillisecondMax = intervalMillisecondMax;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        isRun = true;
    }

    @Override
    public void run(SourceContext<NoteMock> ctx) throws Exception {
        Object lock = ctx.getCheckpointLock();
        while (isRun) {
            synchronized (lock) {
                final int size = RandomUtils.nextInt(100, 200);
                mockNoteInfo(size)
                    .forEach(ctx::collect);
            }
            Thread.sleep(RandomUtils.nextLong(intervalMillisecondMin, intervalMillisecondMax));
        }
    }

    @Override
    public void cancel() {
        isRun = false;
    }

    private List<NoteMock> mockNoteInfo(int size) {
        List<NoteMock> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            // mock 短信发送
            final NoteSendRaw send = getSendEntity();
            final String primaryKey = send.getPrimaryKey();
            final LocalDateTime channelSendTime = send.getChannel_send_time();

            // 随机发送重复数据, 千分之一重复
            final int sign = RandomUtils.nextInt(0, 1000);
            for (int repeat = 1; repeat <= RandomUtils.nextInt(1, 3); repeat++) {
                result.add(send);
                if (sign != 1) {
                    break;
                }
            }

            // mock 发送短信的回执
            final NoteReceiptRaw receipt = getReceipt(primaryKey, channelSendTime);
            // 随机发送重复数据
            for (int repeat = 1; repeat <= RandomUtils.nextInt(1, 3); repeat++) {
                result.add(receipt);
                if (sign != 1) {
                    break;
                }
            }
            result.add(receipt);
        }
        return result;
    }

    private NoteSendRaw getSendEntity() {
        final NoteSendRaw send = new NoteSendRaw();
        send.setUser_name("qiushi6");

        final String shopKey = MD5.create().digestHex(String.valueOf(RandomUtils.nextInt(5000, 7000)), StandardCharsets.UTF_8);
        send.setShop_key(shopKey);
        send.setBusiness_type("xxx");
        send.setSubtask_id(UUID.randomUUID().toString());
        send.setContent(UUID.randomUUID().toString());

        final String receiver = MD5.create().digestHex(String.valueOf(RandomUtils.nextLong(50000000, 90000000)), StandardCharsets.UTF_8);
        send.setReceiver(receiver);

        final String showId = MD5.create().digestHex(String.valueOf(RandomUtils.nextInt(200, 500)), StandardCharsets.UTF_8);
        send.setShow_id(showId);

        final String gatewayId = MD5.create().digestHex(String.valueOf(RandomUtils.nextInt(1, 100)), StandardCharsets.UTF_8);
        send.setGateway_id(gatewayId);

        send.setGateway_account("XXXX-XXX-XXXX-XXXXX");

        send.setCharge_submit_num(RandomUtils.nextLong(1, 2000));

        final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());


        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        send.setBusiness_request_time(localDateTime);

        LocalDateTime sendTime = LocalDateTime.now();
        final int timeRand = RandomUtil.randomInt(0, 1000);
        if (timeRand < 1) {
            sendTime = LocalDateTime.now().minusDays(RandomUtils.nextLong(4, 7));
        }
        if (timeRand < 10) {
            sendTime = LocalDateTime.now().minusDays(RandomUtils.nextLong(0, 4));
        }

        send.setChannel_send_time(sendTime);

        send.setExt_json(JSON.toJSONString(send));
        return send;
    }

    private NoteReceiptRaw getReceipt(String primaryKey, final LocalDateTime channelSendTime) {
        final NoteReceiptRaw receipt = new NoteReceiptRaw();
        receipt.setPrimary_key(primaryKey);
        receipt.setSp_result(String.valueOf(RandomUtils.nextBoolean()));
        receipt.setSp_charge_submit_num(RandomUtils.nextLong(0, 999L));

        final LocalDateTime spSendTime = LocalDateTime.from(channelSendTime).minusHours(RandomUtils.nextLong(0, 24));
        receipt.setSp_send_time(spSendTime);
        receipt.setChannel_receive_time(spSendTime.plusMinutes(1));
        receipt.setReceive_system_time(LocalDateTime.now());

        return receipt;
    }
}
