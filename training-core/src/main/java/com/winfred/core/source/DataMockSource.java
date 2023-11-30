package com.winfred.core.source;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.winfred.core.entity.log.EventBody;
import com.winfred.core.entity.log.EventEntity;
import com.winfred.core.entity.log.EventHeader;
import com.winfred.core.entity.user.UserInfo;
import com.winfred.core.entity.user.UserRole;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeutils.base.MapSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author winfred958
 */
public class DataMockSource extends RichParallelSourceFunction<EventEntity> implements CheckpointedFunction {

  private static final ScheduledThreadPoolExecutor schedulePool = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r);
    }
  });

  private static final String VISITOR_KEY = "visitorId";
  private static final String SESSION_KEY = "sessionId";
  private static final long serialVersionUID = -3939087196608349638L;


  private String sessionId = null;

  private String visitorId = null;

  private volatile boolean isRun;

  private long intervalMillisecondMin;
  private long intervalMillisecondMax;


  private ListState<Map<String, String>> state;

  private Map<String, String> map;

  /**
   * mock 数据, interval 测试时不易过小
   *
   * @param intervalMillisecondMin mock 数据最小时间间隔
   * @param intervalMillisecondMax mock 数据最大时间间隔
   */
  public DataMockSource(long intervalMillisecondMin, long intervalMillisecondMax) {
    this.intervalMillisecondMin = intervalMillisecondMin;
    this.intervalMillisecondMax = intervalMillisecondMax;
  }

  public String getSessionId() {
    if (this.sessionId == null) {
      changeSession();
    }
    return sessionId;
  }

  public String getVisitorId() {
    if (this.visitorId == null) {
      changeVisitor();
    }
    return visitorId;
  }

  public void changeSession() {
    sessionId = UUID.randomUUID().toString();
    map.put(SESSION_KEY, sessionId);
  }

  public void changeVisitor() {
    visitorId = UUID.randomUUID().toString();
    map.put(VISITOR_KEY, visitorId);
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    map = new ConcurrentHashMap<>(16);
    /**
     * session_id 3 秒一换
     */
    schedulePool
        .schedule(this::changeSession, 3000, TimeUnit.MILLISECONDS);

    /**
     * visitor_id 10 秒一换
     */
    schedulePool
        .schedule(this::changeVisitor, 10000, TimeUnit.MILLISECONDS);

    isRun = true;
  }

  @Override
  public void run(SourceContext<EventEntity> ctx) throws Exception {
    Object lock = ctx.getCheckpointLock();
    while (isRun) {
      synchronized (lock) {
        buildDataList()
            .forEach(ctx::collect);
        Thread.sleep(RandomUtils.nextLong(intervalMillisecondMin, intervalMillisecondMax));
      }
    }
  }

  @Override
  public void cancel() {
    isRun = false;
  }


  private List<EventEntity> buildDataList() {
    int iterator = 1 + (int) (Math.random() * 100);
    List<EventEntity> dataList = new ArrayList<>(iterator);
    for (int i = 0; i < iterator; i++) {
      dataList.add(buildDataNested(i));
    }
    return dataList;
  }

  private EventEntity buildDataNested(int partition) {
    EventEntity entity = new EventEntity();

    entity.setSource("test-source");

    EventHeader header = entity.getHeader();


    header.setActionTime(entity.getServerTime() - (int) (Math.random() * 100));
    header.setPlatform("website");
    header.setAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36");

    header.setVisitorId(getVisitorId() + "." + partition);
    header.setSessionId(getSessionId() + "." + partition);

    String token = getFakerToken(partition);

    header.setToken(token);

    EventBody body = entity.getBody();
    body.setEventName("item.click");
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

  @Override
  public void snapshotState(FunctionSnapshotContext context) throws Exception {
    state.clear();
    state.add(map);
  }

  @Override
  public void initializeState(FunctionInitializationContext context) throws Exception {
    state = context
        .getOperatorStateStore()
        .getListState(new ListStateDescriptor<Map<String, String>>("mapState", new MapSerializer<>(StringSerializer.INSTANCE, StringSerializer.INSTANCE)));

    state
        .get()
        .forEach(entry -> {
          this.visitorId = entry.get(VISITOR_KEY);
          this.sessionId = entry.get(SESSION_KEY);
        });

  }
}
