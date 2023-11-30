package com.winfred.core.entity.log;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author winfred958
 */
@Getter
public class EventHeader implements Serializable {
  private static final long serialVersionUID = -3709433819783664998L;

  private String platform;

  @JSONField(name = "visitor_id")
  private String visitorId;
  @JSONField(name = "session_id")
  private String sessionId;
  private String token;

  @JSONField(name = "action_time")
  private Long actionTime;

  private String os;
  @JSONField(name = "os_version")
  private String osVersion;
  private String lib; //sdk名称
  @JSONField(name = "lib_version")
  private String libVersion;// sdk版本

  private String ip;

  @JSONField(name = "screen_height")
  private String screenHeight;
  @JSONField(name = "screen_width")
  private String screenWidth;

  /**
   * web
   */
  private String browser;
  @JSONField(name = "browser_version")
  private String browserVersion;
  private String agent;
  private String referer;
  @JSONField(name = "current_url")
  private String currentUrl;

  @JSONField(name = "initial_referrer")
  private String initialReferrer;
  @JSONField(name = "initial_referring_domain")
  private String initialReferringDomain;
  private String language;
  /**
   * app
   */
  @JSONField(name = "app_version")
  private String appVersion;
  @JSONField(name = "app_build_number")
  private String appBuildNumber;

  private String lat;
  private String lon;

  private String device;
  @JSONField(name = "device_type")
  private String deviceType;
  private String manufacturer;// 制造商
  private String carrier;// 运营商
  private String model;
  private String radio;
  private String wifi;

  @JSONField(name = "device_language")
  private String deviceLanguage;
  @JSONField(name = "app_language")
  private String appLanguage;


  public EventHeader setPlatform(String platform) {
    this.platform = platform;
    return this;
  }

  public EventHeader setVisitorId(String visitorId) {
    this.visitorId = visitorId;
    return this;
  }

  public EventHeader setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public EventHeader setToken(String token) {
    this.token = token;
    return this;
  }

  public EventHeader setActionTime(Long actionTime) {
    this.actionTime = actionTime;
    return this;
  }

  public EventHeader setOs(String os) {
    this.os = os;
    return this;
  }

  public EventHeader setOsVersion(String osVersion) {
    this.osVersion = osVersion;
    return this;
  }

  public EventHeader setLib(String lib) {
    this.lib = lib;
    return this;
  }

  public EventHeader setLibVersion(String libVersion) {
    this.libVersion = libVersion;
    return this;
  }

  public EventHeader setIp(String ip) {
    this.ip = ip;
    return this;
  }

  public EventHeader setScreenHeight(String screenHeight) {
    this.screenHeight = screenHeight;
    return this;
  }

  public EventHeader setScreenWidth(String screenWidth) {
    this.screenWidth = screenWidth;
    return this;
  }

  public EventHeader setBrowser(String browser) {
    this.browser = browser;
    return this;
  }

  public EventHeader setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }

  public EventHeader setAgent(String agent) {
    this.agent = agent;
    return this;
  }

  public EventHeader setReferer(String referer) {
    this.referer = referer;
    return this;
  }

  public EventHeader setCurrentUrl(String currentUrl) {
    this.currentUrl = currentUrl;
    return this;
  }

  public EventHeader setInitialReferrer(String initialReferrer) {
    this.initialReferrer = initialReferrer;
    return this;
  }

  public EventHeader setInitialReferringDomain(String initialReferringDomain) {
    this.initialReferringDomain = initialReferringDomain;
    return this;
  }

  public EventHeader setLanguage(String language) {
    this.language = language;
    return this;
  }

  public EventHeader setAppVersion(String appVersion) {
    this.appVersion = appVersion;
    return this;
  }

  public EventHeader setAppBuildNumber(String appBuildNumber) {
    this.appBuildNumber = appBuildNumber;
    return this;
  }

  public EventHeader setLat(String lat) {
    this.lat = lat;
    return this;
  }

  public EventHeader setLon(String lon) {
    this.lon = lon;
    return this;
  }

  public EventHeader setDevice(String device) {
    this.device = device;
    return this;
  }

  public EventHeader setDeviceType(String deviceType) {
    this.deviceType = deviceType;
    return this;
  }

  public EventHeader setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
    return this;
  }

  public EventHeader setCarrier(String carrier) {
    this.carrier = carrier;
    return this;
  }

  public EventHeader setModel(String model) {
    this.model = model;
    return this;
  }

  public EventHeader setRadio(String radio) {
    this.radio = radio;
    return this;
  }

  public EventHeader setWifi(String wifi) {
    this.wifi = wifi;
    return this;
  }

  public EventHeader setDeviceLanguage(String deviceLanguage) {
    this.deviceLanguage = deviceLanguage;
    return this;
  }

  public EventHeader setAppLanguage(String appLanguage) {
    this.appLanguage = appLanguage;
    return this;
  }
}

