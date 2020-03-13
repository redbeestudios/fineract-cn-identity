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
package org.apache.fineract.cn.identity.internal.repository;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import java.util.Optional;
import org.apache.fineract.cn.cassandra.core.CassandraSessionProvider;
import org.apache.fineract.cn.cassandra.core.TenantAwareCassandraMapperProvider;
import org.apache.fineract.cn.cassandra.core.TenantAwareEntityTemplate;
import org.springframework.stereotype.Component;

@Component
public class PushNotifications {

  static final String TABLE_NAME = "isis_push_notification";
  static final String ACCOUNT_ID = "account_id";
  static final String FIREBASE_TOKEN = "firebase_token";
  private final TenantAwareEntityTemplate tenantAwareEntityTemplate;
  private final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;
  private final CassandraSessionProvider cassandraSessionProvider;

  public PushNotifications(
      TenantAwareEntityTemplate tenantAwareEntityTemplate,
      TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider,
      CassandraSessionProvider cassandraSessionProvider) {
    this.tenantAwareEntityTemplate = tenantAwareEntityTemplate;
    this.tenantAwareCassandraMapperProvider = tenantAwareCassandraMapperProvider;
    this.cassandraSessionProvider = cassandraSessionProvider;
  }

  public void add(final PushNotificationEntity instance) {
    tenantAwareEntityTemplate.save(instance);
  }

  public void delete(final PushNotificationEntity instance) {
    tenantAwareEntityTemplate.delete(instance);
  }

  public Optional<PushNotificationEntity> getByFirebaseToken(String firebaseToken) {
    final Mapper<PushNotificationEntity> entityMapper = tenantAwareCassandraMapperProvider
        .getMapper(PushNotificationEntity.class);
    final Session tenantSession = cassandraSessionProvider.getTenantSession();

    final Statement statement = QueryBuilder.select().all()
        .from(TABLE_NAME)
        .where(QueryBuilder.eq(FIREBASE_TOKEN, firebaseToken));

    return Optional.ofNullable(entityMapper.map(tenantSession.execute(statement)).one());
  }

}
