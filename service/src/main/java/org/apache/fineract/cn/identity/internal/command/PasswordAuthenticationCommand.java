/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.identity.internal.command;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
public class PasswordAuthenticationCommand {

  private String useridentifier;
  private transient String password;
  private String pushNotificationFirebaseToken;

  PasswordAuthenticationCommand() {
  }

  public PasswordAuthenticationCommand(
      final String useridentifier,
      final String password) {
    this(useridentifier, password, null);
  }

  public PasswordAuthenticationCommand(
      final String useridentifier,
      final String password,
      final String pushNotificationFirebaseToken) {
    this.useridentifier = useridentifier;
    this.password = password;
    this.pushNotificationFirebaseToken = pushNotificationFirebaseToken;
  }


  public String getUseridentifier() {
    return useridentifier;
  }

  public void setUseridentifier(String useridentifier) {
    this.useridentifier = useridentifier;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPushNotificationFirebaseToken() {
    return pushNotificationFirebaseToken;
  }

  public void setPushNotificationFirebaseToken(String pushNotificationFirebaseToken) {
    this.pushNotificationFirebaseToken = pushNotificationFirebaseToken;
  }

  @Override
  public String toString() {
    return "PasswordAuthenticationCommand{" +
        "useridentifier='" + useridentifier + '\'' +
        "pushNotificationFirebaseToken='" + pushNotificationFirebaseToken + '\'' +
        '}';
  }
}
