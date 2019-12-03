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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.fineract.cn.identity.api.v1.domain.UserWithPassword;

/**
 * @author Enzo Bonggio
 */
@SuppressWarnings("unused")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateUserWithSocialMediaCommand {
  private String id;
  private String identifier;
  private String role;

  private transient String firebaseToken;

  public CreateUserWithSocialMediaCommand(
      UserWithPassword instance) {
    this.identifier = instance.getIdentifier();
    this.role = instance.getRole();
    this.firebaseToken = instance.getFirebaseToken();
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getFirebaseToken() {
    return firebaseToken;
  }

  public void setFirebaseToken(String firebaseToken) {
    this.firebaseToken = firebaseToken;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "CreateUserCommand{" +
        "identifier='" + identifier + '\'' +
        ", role='" + role + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
