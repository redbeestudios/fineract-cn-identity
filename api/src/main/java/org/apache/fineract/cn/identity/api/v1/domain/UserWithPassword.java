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
package org.apache.fineract.cn.identity.api.v1.domain;

import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.ScriptAssert;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@ScriptAssert(lang = "javascript", script = "_this.identifier !== \"guest\" && _this.identifier !== \"seshat\" && _this.identifier !== \"system\" && _this.identifier !== \"wepemnefret\"" )
public class UserWithPassword {
  private String id;

  @ValidIdentifier(maxLength = 350)
  private String identifier;

  @ValidIdentifier
  private String role;

  @Length(min = 8)
  private String password;

  private String firebaseToken;

  public UserWithPassword()
  {
    super();
  }

  public UserWithPassword(final String identifier, final String role, final String password) {
    this.identifier = identifier;
    this.role = role;
    this.password = password;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof UserWithPassword))
      return false;
    UserWithPassword that = (UserWithPassword) o;
    return Objects.equals(identifier, that.identifier) &&
        Objects.equals(role, that.role) &&
        Objects.equals(password, that.password) &&
        Objects.equals(firebaseToken, that.firebaseToken) &&
        Objects.equals(id, that.id);
  }

  @Override public int hashCode() {
    return Objects.hash(identifier, role, password);
  }

  @Override public String toString() {
    return "UserWithPassword{" +
        "identifier='" + identifier + '\'' +
        ", role='" + role + '\'' +
        ", password='" + password + '\'' +
        ", firebaseToken=" + firebaseToken +'\'' +
        ", id=" + id +'\'' +
        '}';
  }
}
