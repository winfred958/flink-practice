package com.winfred.streamming.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.winfred.streamming.entity.log.EventBody;
import com.winfred.streamming.entity.log.EventEntity;
import com.winfred.streamming.entity.log.EventHeader;
import com.winfred.streamming.entity.log.SimpleEventEntity;
import com.winfred.streamming.entity.user.UserInfo;
import com.winfred.streamming.entity.user.UserRole;
import com.winfred.streamming.mock.MockUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.time.ZoneId;
import java.util.*;

public class TestSource extends RichParallelSourceFunction<String> {
  
  private String SESSION_ID = UUID.randomUUID().toString();
  
  private String VISITOR_ID = UUID.randomUUID().toString();
  
  private volatile boolean isRun;
  
  private int intervalMillisecondMin;
  private int intervalMillisecondMax;
  
  /**
   * mock 数据
   *
   * @param intervalMillisecondMin
   */
  public TestSource(int intervalMillisecondMin, int intervalMillisecondMax) {
    this.intervalMillisecondMin = intervalMillisecondMin;
    this.intervalMillisecondMax = intervalMillisecondMax;
    
    /**
     * session_id 3 秒一换
     */
    new Timer()
        .scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            SESSION_ID = UUID.randomUUID().toString();
          }
        }, 0, 3 * 1000);
    
    /**
     * visitor_id 10 秒一换
     */
    new Timer()
        .scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            VISITOR_ID = UUID.randomUUID().toString();
          }
        }, 0, 10 * 1000);
    
  }
  
  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    isRun = true;
  }
  
  @Override
  public void run(SourceContext<String> ctx) throws Exception {
  
    long aLong = RandomUtils.nextLong(intervalMillisecondMin, intervalMillisecondMax);
    while (isRun) {
      buildDataList(true)
          .stream()
          .map(entity -> {
            return JSON.toJSONString(entity, SerializerFeature.SortField);
          }).forEach(ctx::collect);
      Thread.sleep(aLong);
    }
  }
  
  
  @Override
  public void cancel() {
    isRun = false;
  }
  
  
  private List<Object> buildDataList(boolean isNested) {
    int iterator = 1 + (int) (Math.random() * 100);
    
    List<Object> dataList = new ArrayList<>(iterator);
    
    if (isNested) {
      for (int i = 0; i < iterator; i++) {
        dataList.add(buildDataNested(i));
      }
    } else {
      for (int i = 0; i < iterator; i++) {
        dataList.add(buildDataSimple(i));
      }
    }
    return dataList;
  }
  
  private Object buildDataNested(int partition) {
    EventEntity entity = new EventEntity();
    
    entity.setSource("test-source");
    
    EventHeader header = entity.getHeader();
    
    
    header.setAction_time(entity.getServer_time() - (int) (Math.random() * 100));
    header.setPlatform("website");
    header.setAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36");
    
    header.setVisitor_id(VISITOR_ID + "." + partition);
    header.setSession_id(SESSION_ID + "." + partition);
    
    String token = getFakerToken(partition);
    
    header.setToken(token);
    
    EventBody body = entity.getBody();
    body.setEvent_name("item.click");
    List<EventBody.Parameter> parameters = new ArrayList<>(1);
    parameters.add(new EventBody.Parameter("sku", UUID.randomUUID().toString()));
    body.setParameters(parameters);
    return entity;
  }
  
  private String getFakerToken(int partition) {
    UserInfo userInfo = new UserInfo();
    userInfo.setIsLogin(true);
    userInfo.setUserId("" + partition);
    userInfo.setUserName("kevin-test-" + partition);
    List<UserRole> userRoles = new ArrayList<>(2);
    userRoles.add(new UserRole(5, "User"));
    userInfo.setUserRoles(userRoles);
    
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
    Date now = calendar.getTime();
    
    calendar.add(Calendar.HOUR_OF_DAY, 1);
    Date now_after_1_hour = calendar.getTime();
    return JWT
        .create()
        .withExpiresAt(now_after_1_hour) // 过期时间
        .withNotBefore(now) //
        .withIssuedAt(now) // 发行时间
        .withHeader(new HashMap<>())
        .withClaim("userInfo", JSON.parseObject(JSON.toJSONString(userInfo)))
        .sign(Algorithm.HMAC512("xxxxxxxxxxxxxxxx"));
  }
  
  private Object buildDataSimple(int partition) {
    SimpleEventEntity entity = new SimpleEventEntity();
    entity.setSource("test-source");
    
    long timeMillis = System.currentTimeMillis();
    entity.setServer_time(timeMillis);
    entity.setAction_time(timeMillis);
    
    entity.setVisitor_id(VISITOR_ID + "." + partition);
    entity.setSession_id(SESSION_ID + "." + partition);
    
    String token = getFakerToken(partition);
    entity.setToken(token);
    
    entity.setPlatform("website");
    entity.setAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36");
    
    entity.setEvent_name("item.click");
    List<SimpleEventEntity.Parameter> parameters = new ArrayList<>(1);
    parameters.add(new SimpleEventEntity.Parameter("sku", MockUtils.getSku()));
    entity.setParameters(parameters);
    return entity;
  }
  
  
}
