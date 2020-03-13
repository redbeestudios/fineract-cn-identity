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
package org.apache.fineract.cn.identity.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.identity.api.v1.PermittableGroupIds;
import org.apache.fineract.cn.identity.api.v1.domain.PushNotificationRequest;
import org.apache.fineract.cn.identity.internal.repository.PushNotificationByAccountIdEntity;
import org.apache.fineract.cn.identity.internal.repository.PushNotificationsByAccountId;
import org.apache.fineract.cn.identity.internal.util.IdentityConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class PushNotificationTokenRestController {

  private final Logger logger;
  private final PushNotificationsByAccountId pushNotificationsByAccountIdRepository;

  public PushNotificationTokenRestController(
      @Qualifier(IdentityConstants.LOGGER_NAME) final Logger logger,
      PushNotificationsByAccountId pushNotificationsByAccountIdRepository) {
    this.logger = logger;
    this.pushNotificationsByAccountIdRepository = pushNotificationsByAccountIdRepository;
  }

  @RequestMapping(
      method = RequestMethod.GET,
      consumes = {MediaType.ALL_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTITY_MANAGEMENT)
  @ResponseBody
  List<PushNotificationByAccountIdEntity> getTokensForAccountId(
      @RequestParam(value = "account-id") final String accountId
  ) {
    final List<PushNotificationByAccountIdEntity> pushNotificationTokens = pushNotificationsByAccountIdRepository
        .getAllByAccountId(accountId);
    logger.info("Token for id {}: {}", accountId, pushNotificationTokens);
    return pushNotificationTokens;
  }

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = {MediaType.ALL_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTITY_MANAGEMENT)
  @ResponseBody
  void sendPushNotificationByAccountId(
      @RequestBody final PushNotificationRequest request
  ) throws JsonProcessingException {
    final List<PushNotificationByAccountIdEntity> pushNotificationTokens = pushNotificationsByAccountIdRepository
        .getAllByAccountId(request.getAccountId());
    logger.info("Token for id {}: {}", request.getAccountId(), pushNotificationTokens);
    for (PushNotificationByAccountIdEntity entity : pushNotificationTokens) {
      request.setTo(entity.getFirebaseToken());
      String body = new ObjectMapper().writeValueAsString(request);
      logger.info("Body as string {}", body);
      HttpResponse<String> response = Unirest.post("https://fcm.googleapis.com/fcm/send")
          .header("Content-Type", "application/json")
          .header("Authorization",
              "key=AAAAzjOYARE:APA91bGXOr9Mfa2cBvzcY429_SX3ZU6DWTEHhuOj88I1Rq62cQc8glWX9A6pi64JOSPgy1F0db7-EgR6WbR-NYEsYQ-g0tbsq5AXYnM4zsVYESxvf1JKnPffeIcwb3MQGv8CZ4K_IJm3")
          .body(body)
          .asString();
      logger.info("Response from push notification {}", response.getBody());
    }
  }
}

