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

import com.datastax.driver.core.exceptions.InvalidQueryException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.fineract.cn.anubis.api.v1.domain.ApplicationSignatureSet;
import org.apache.fineract.cn.crypto.SaltGenerator;
import org.apache.fineract.cn.identity.api.v1.PermittableGroupIds;
import org.apache.fineract.cn.identity.internal.mapper.SignatureMapper;
import org.apache.fineract.cn.identity.internal.repository.AllowedOperationType;
import org.apache.fineract.cn.identity.internal.repository.ApplicationCallEndpointSets;
import org.apache.fineract.cn.identity.internal.repository.ApplicationPermissionUsers;
import org.apache.fineract.cn.identity.internal.repository.ApplicationPermissions;
import org.apache.fineract.cn.identity.internal.repository.ApplicationSignatures;
import org.apache.fineract.cn.identity.internal.repository.PermissionType;
import org.apache.fineract.cn.identity.internal.repository.Permissions;
import org.apache.fineract.cn.identity.internal.repository.PermittableGroupEntity;
import org.apache.fineract.cn.identity.internal.repository.PermittableGroups;
import org.apache.fineract.cn.identity.internal.repository.PermittableType;
import org.apache.fineract.cn.identity.internal.repository.PrivateTenantInfoEntity;
import org.apache.fineract.cn.identity.internal.repository.RoleEntity;
import org.apache.fineract.cn.identity.internal.repository.Roles;
import org.apache.fineract.cn.identity.internal.repository.SignatureEntity;
import org.apache.fineract.cn.identity.internal.repository.Signatures;
import org.apache.fineract.cn.identity.internal.repository.Tenants;
import org.apache.fineract.cn.identity.internal.repository.UserEntity;
import org.apache.fineract.cn.identity.internal.repository.Users;
import org.apache.fineract.cn.identity.internal.util.IdentityConstants;
import org.apache.fineract.cn.lang.ServiceException;
import org.apache.fineract.cn.lang.TenantContextHolder;
import org.apache.fineract.cn.lang.security.RsaKeyPairFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Myrle Krantz
 */
@Component
public class Provisioner {
  private final Signatures signature;
  private final Tenants tenant;
  private final Users users;
  private final PermittableGroups permittableGroups;
  private final Permissions permissions;
  private final Roles roles;
  private final ApplicationSignatures applicationSignatures;
  private final ApplicationPermissions applicationPermissions;
  private final ApplicationPermissionUsers applicationPermissionUsers;
  private final ApplicationCallEndpointSets applicationCallEndpointSets;
  private final UserEntityCreator userEntityCreator;
  private final Logger logger;
  private final SaltGenerator saltGenerator;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${identity.passwordExpiresInDays:93}")
  private int passwordExpiresInDays;

  @Value("${identity.timeToChangePasswordAfterExpirationInDays:4}")
  private int timeToChangePasswordAfterExpirationInDays;

  @Autowired
  Provisioner(
          final Signatures signature,
          final Tenants tenant,
          final Users users,
          final PermittableGroups permittableGroups,
          final Permissions permissions,
          final Roles roles,
          final ApplicationSignatures applicationSignatures,
          final ApplicationPermissions applicationPermissions,
          final ApplicationPermissionUsers applicationPermissionUsers,
          final ApplicationCallEndpointSets applicationCallEndpointSets,
          final UserEntityCreator userEntityCreator,
          @Qualifier(IdentityConstants.LOGGER_NAME) final Logger logger,
          final SaltGenerator saltGenerator)
  {
    this.signature = signature;
    this.tenant = tenant;
    this.users = users;
    this.permittableGroups = permittableGroups;
    this.permissions = permissions;
    this.roles = roles;
    this.applicationSignatures = applicationSignatures;
    this.applicationPermissions = applicationPermissions;
    this.applicationPermissionUsers = applicationPermissionUsers;
    this.applicationCallEndpointSets = applicationCallEndpointSets;
    this.userEntityCreator = userEntityCreator;
    this.logger = logger;
    this.saltGenerator = saltGenerator;
  }

  public synchronized ApplicationSignatureSet provisionTenant(final String initialPasswordHash) {
    {
      final Optional<ApplicationSignatureSet> latestSignature = signature.getAllKeyTimestamps().stream()
          .max(String::compareTo)
          .flatMap(signature::getSignature)
          .map(SignatureMapper::mapToApplicationSignatureSet);

      if (latestSignature.isPresent()) {
        final Optional<ByteBuffer> fixedSalt = tenant.getPrivateTenantInfo().map(PrivateTenantInfoEntity::getFixedSalt);
        if (fixedSalt.isPresent()) {
          logger.info("Changing password for tenant '{}' instead of provisioning...", TenantContextHolder
              .checkedGetIdentifier());
          final UserEntity suUser = userEntityCreator
              .build(IdentityConstants.SU_NAME, IdentityConstants.SU_ROLE, initialPasswordHash, true,
                  fixedSalt.get().array(), timeToChangePasswordAfterExpirationInDays);
          users.add(suUser);
          logger.info("Successfully changed admin password '{}'...", TenantContextHolder.checkedGetIdentifier());

          return latestSignature.get();
        }
      }
    }

    logger.info("Provisioning cassandra tables for tenant '{}'...", TenantContextHolder.checkedGetIdentifier());
    final RsaKeyPairFactory.KeyPairHolder keys = RsaKeyPairFactory.createKeyPair();

    byte[] fixedSalt = this.saltGenerator.createRandomSalt();

    try {
      signature.buildTable();
      final SignatureEntity signatureEntity = signature.add(keys);

      tenant.buildTable();
      tenant.add(fixedSalt, passwordExpiresInDays, timeToChangePasswordAfterExpirationInDays);

      users.buildTable();
      permittableGroups.buildTable();
      permissions.buildType();
      roles.buildTable();
      applicationSignatures.buildTable();
      applicationPermissions.buildTable();
      applicationPermissionUsers.buildTable();
      applicationCallEndpointSets.buildTable();


      createPermittablesGroup(PermittableGroupIds.ROLE_MANAGEMENT, "/roles/*", "/permittablegroups/*");
      createPermittablesGroup(PermittableGroupIds.IDENTITY_MANAGEMENT, "/users/*");
      createPermittablesGroup(PermittableGroupIds.SELF_MANAGEMENT, "/users/{useridentifier}/password", "/applications/*/permissions/*/users/*/enabled");
      createPermittablesGroup(PermittableGroupIds.APPLICATION_SELF_MANAGEMENT, "/applications/*/permissions");

      final List<PermissionType> permissions = new ArrayList<>();
      permissions.add(fullAccess(PermittableGroupIds.ROLE_MANAGEMENT));
      permissions.add(fullAccess(PermittableGroupIds.IDENTITY_MANAGEMENT));
      permissions.add(fullAccess(PermittableGroupIds.SELF_MANAGEMENT));
      permissions.add(fullAccess(PermittableGroupIds.APPLICATION_SELF_MANAGEMENT));

      final RoleEntity suRole = new RoleEntity();
      suRole.setIdentifier(IdentityConstants.SU_ROLE);
      suRole.setPermissions(permissions);

      roles.add(suRole);

      final UserEntity suUser = userEntityCreator
              .build(IdentityConstants.SU_NAME, IdentityConstants.SU_ROLE, initialPasswordHash, true,
                      fixedSalt, timeToChangePasswordAfterExpirationInDays);
      users.add(suUser);

      final ApplicationSignatureSet ret = SignatureMapper.mapToApplicationSignatureSet(signatureEntity);

      logger.info("Successfully provisioned cassandra tables for tenant '{}'...", TenantContextHolder.checkedGetIdentifier());

      return ret;
    }
    catch (final InvalidQueryException e)
    {
      logger.error("Failed to provision cassandra tables for tenant.", e);
      throw ServiceException.internalError("Failed to provision tenant.");
    }
  }

  private PermissionType fullAccess(final String permittableGroupIdentifier) {
    final PermissionType ret = new PermissionType();
    ret.setPermittableGroupIdentifier(permittableGroupIdentifier);
    ret.setAllowedOperations(AllowedOperationType.ALL);
    return ret;
  }

  private void createPermittablesGroup(final String identifier, final String... paths) {
    final PermittableGroupEntity permittableGroup = new PermittableGroupEntity();
    permittableGroup.setIdentifier(identifier);
    permittableGroup.setPermittables(Arrays.stream(paths).flatMap(this::permittables).collect(Collectors.toList()));
    permittableGroups.add(permittableGroup);
  }

  private Stream<PermittableType> permittables(final String path)
  {
    final PermittableType getret = new PermittableType();
    getret.setPath(applicationName + path);
    getret.setMethod("GET");

    final PermittableType postret = new PermittableType();
    postret.setPath(applicationName + path);
    postret.setMethod("POST");

    final PermittableType putret = new PermittableType();
    putret.setPath(applicationName + path);
    putret.setMethod("PUT");

    final PermittableType delret = new PermittableType();
    delret.setPath(applicationName + path);
    delret.setMethod("DELETE");

    final List<PermittableType> ret = new ArrayList<>();
    ret.add(getret);
    ret.add(postret);
    ret.add(putret);
    ret.add(delret);

    return ret.stream();
  }


}
