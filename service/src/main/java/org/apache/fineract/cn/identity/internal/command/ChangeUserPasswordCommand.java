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
import java.util.UUID;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.DEFAULT)
public class ChangeUserPasswordCommand {
  private String identifier;

  //transient to ensure this field doesn't land in the audit log.
  private transient String password;

  public ChangeUserPasswordCommand() {
  }

  public ChangeUserPasswordCommand(final String identifier, final String password) {
    this.identifier = identifier;
    this.password = password;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "ChangeUserPasswordCommand{" +
        "identifier='" + identifier + '\'' +
        '}';
  }
}
