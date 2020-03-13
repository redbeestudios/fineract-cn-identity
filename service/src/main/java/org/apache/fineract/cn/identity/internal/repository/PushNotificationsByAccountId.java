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
import java.util.List;
import java.util.UUID;
import org.apache.fineract.cn.cassandra.core.CassandraSessionProvider;
import org.apache.fineract.cn.cassandra.core.TenantAwareCassandraMapperProvider;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationsByAccountId {

  static final String TABLE_NAME = "isis_push_notification_by_account_id";
  static final String ACCOUNT_ID = "account_id";
  static final String FIREBASE_TOKEN = "firebase_token";
  private final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;
  private final CassandraSessionProvider cassandraSessionProvider;

  public PushNotificationsByAccountId(
      TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider,
      CassandraSessionProvider cassandraSessionProvider) {
    this.tenantAwareCassandraMapperProvider = tenantAwareCassandraMapperProvider;
    this.cassandraSessionProvider = cassandraSessionProvider;
  }


  public List<PushNotificationByAccountIdEntity> getAllByAccountId(String accountId) {
    final Mapper<PushNotificationByAccountIdEntity> entityMapper = tenantAwareCassandraMapperProvider
        .getMapper(PushNotificationByAccountIdEntity.class);
    final Session tenantSession = cassandraSessionProvider.getTenantSession();

    final Statement statement = QueryBuilder.select().all()
        .from(TABLE_NAME)
        .where(QueryBuilder.eq(ACCOUNT_ID, UUID.fromString(accountId)));

    return entityMapper.map(tenantSession.execute(statement)).all();
  }

}
