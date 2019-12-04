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
package org.apache.fineract.cn.identity.internal.command.handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.apache.fineract.cn.command.annotation.*;
import org.apache.fineract.cn.command.kafka.KafkaTopicConstants;
import org.apache.fineract.cn.identity.api.v1.events.EventConstants;
import org.apache.fineract.cn.identity.internal.command.ChangeUserPasswordCommand;
import org.apache.fineract.cn.identity.internal.command.ChangeUserRoleCommand;
import org.apache.fineract.cn.identity.internal.command.CreateUserCommand;
import org.apache.fineract.cn.identity.internal.command.CreateUserWithSocialMediaCommand;
import org.apache.fineract.cn.identity.internal.repository.UserEntity;
import org.apache.fineract.cn.identity.internal.repository.Users;
import org.apache.fineract.cn.identity.internal.util.IdentityConstants;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
@Component
public class UserCommandHandler {

  private final Users usersRepository;
  private final UserEntityCreator userEntityCreator;
  private final Logger logger;

  @Autowired
  UserCommandHandler(
      final Users usersRepository,
      final UserEntityCreator userEntityCreator,
      @Qualifier(IdentityConstants.LOGGER_NAME) final Logger logger) {
    this.usersRepository = usersRepository;
    this.userEntityCreator = userEntityCreator;
    this.logger = logger;
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = EventConstants.OPERATION_HEADER, selectorValue = EventConstants.OPERATION_PUT_USER_ROLEIDENTIFIER)
  public String process(final ChangeUserRoleCommand command) {
    final UserEntity user = usersRepository.get(command.getIdentifier())
        .orElseThrow(() -> ServiceException.notFound(
            "User " + command.getIdentifier() + " doesn't exist."));

    user.setRole(command.getRole());
    usersRepository.add(user);

    return user.getIdentifier();
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = EventConstants.OPERATION_HEADER,
      selectorValue = EventConstants.OPERATION_PUT_USER_PASSWORD,
      selectorKafkaEvent = NotificationFlag.NOTIFY,
      selectorKafkaTopic = KafkaTopicConstants.TOPIC_IDENTITY_USER,
      selectorKafkaTopicError = KafkaTopicConstants.TOPIC_ERROR_IDENTITY_USER)
  public String process(final ChangeUserPasswordCommand command) {
    final UserEntity user = usersRepository.get(command.getIdentifier())
        .orElseThrow(() -> ServiceException.notFound(
            "User " + command.getIdentifier() + " doesn't exist."));

    final UserEntity userWithNewPassword = userEntityCreator.build(
        user.getId(),
        user.getIdentifier(),
        user.getRole(),
        command.getPassword(),
        !SecurityContextHolder.getContext().getAuthentication().getName()
            .equals(command.getIdentifier()));
    usersRepository.add(userWithNewPassword);

    return user.getIdentifier();
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = EventConstants.OPERATION_HEADER,
      selectorValue = EventConstants.OPERATION_POST_USER,
      selectorKafkaEvent = NotificationFlag.NOTIFY,
      selectorKafkaTopic = KafkaTopicConstants.TOPIC_IDENTITY_USER,
      selectorKafkaTopicError = KafkaTopicConstants.TOPIC_ERROR_IDENTITY_USER)
  public String process(final CreateUserCommand command) {
    Assert.hasText(command.getPassword());

    final UserEntity userEntity = userEntityCreator.build(
        command.getId(),
        command.getIdentifier(),
        command.getRole(),
        command.getPassword(),
        false);

    usersRepository.add(userEntity);

    return command.getIdentifier();
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = EventConstants.OPERATION_HEADER,
      selectorValue = EventConstants.OPERATION_POST_USER,
      selectorKafkaEvent = NotificationFlag.NOTIFY,
      selectorKafkaTopic = KafkaTopicConstants.TOPIC_IDENTITY_USER,
      selectorKafkaTopicError = KafkaTopicConstants.TOPIC_ERROR_IDENTITY_USER)
  public String process(final CreateUserWithSocialMediaCommand command) {
    Assert.hasText(command.getFirebaseToken());

    FirebaseToken token;
    try {
      token = FirebaseAuth.getInstance().verifyIdToken(command.getFirebaseToken());
    } catch (FirebaseAuthException e) {
      logger.error("Incorrect firebase token", e);
      throw ServiceException.badRequest("Firebase token is incorrect.", e);
    } catch (final IllegalArgumentException e) {
      logger.error("Error getting firebase token", e);
      throw ServiceException.badRequest("There was an error getting information from firebase token.", e);
    }

    if (!token.getEmail().equals(command.getIdentifier()))
      throw ServiceException.badRequest("Firebase token email is different from identifier.");

    final UserEntity userEntity = userEntityCreator.build(
        command.getId(),
        command.getIdentifier(),
        command.getRole(),
        null,
        false);

    usersRepository.add(userEntity);

    return command.getIdentifier();
  }
}
