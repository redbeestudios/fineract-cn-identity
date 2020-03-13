package org.apache.fineract.cn.identity.api.v1.domain;


import java.util.Map;

public class PushNotificationRequest {

  private String accountId;

  private String priority;

  private Notification notification;

  private Map<String, Object> data;

  private String to;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public Notification getNotification() {
    return notification;
  }

  public void setNotification(
      Notification notification) {
    this.notification = notification;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  @Override
  public String toString() {
    return "PushNotificationRequest{" +
        "accountId='" + accountId + '\'' +
        ", priority='" + priority + '\'' +
        ", notification=" + notification +
        ", data=" + data +
        ", to='" + to + '\'' +
        '}';
  }

  public static class Notification {

    private String title;
    private String body;

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getBody() {
      return body;
    }

    public void setBody(String body) {
      this.body = body;
    }

    @Override
    public String toString() {
      return "Notification{" +
          "title='" + title + '\'' +
          ", body='" + body + '\'' +
          '}';
    }
  }
}

