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

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.mapping.Mapper;
import org.apache.fineract.cn.cassandra.core.CassandraSessionProvider;
import org.apache.fineract.cn.cassandra.core.TenantAwareCassandraMapperProvider;
import org.apache.fineract.cn.cassandra.core.TenantAwareEntityTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@Component
public class Users {

  static final String TABLE_NAME = "isis_users";
  static final String IDENTIFIER_COLUMN = "identifier";
  static final String ROLE_COLUMN = "roleIdentifier";
  static final String PASSWORD_COLUMN = "passwordWord";
  static final String ID_COLUMN = "id";
  static final String PASSWORD_EXPIRES_ON_COLUMN = "password_expires_on";
  static final String SALT_COLUMN = "salt";
  static final String ITERATION_COUNT_COLUMN = "iteration_count";

  private final CassandraSessionProvider cassandraSessionProvider;
  private final TenantAwareEntityTemplate tenantAwareEntityTemplate;
  private final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;

  @Autowired
  Users(final CassandraSessionProvider cassandraSessionProvider,
      final TenantAwareEntityTemplate tenantAwareEntityTemplate,
      final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider)
  {
    this.cassandraSessionProvider = cassandraSessionProvider;
    this.tenantAwareEntityTemplate = tenantAwareEntityTemplate;
    this.tenantAwareCassandraMapperProvider = tenantAwareCassandraMapperProvider;
  }

  public void buildTable() {
    final Create create = SchemaBuilder.createTable(TABLE_NAME)
        .ifNotExists()
        .addPartitionKey(IDENTIFIER_COLUMN, DataType.text())
        .addColumn(ROLE_COLUMN, DataType.text())
        .addColumn(PASSWORD_COLUMN, DataType.blob())
        .addColumn(SALT_COLUMN, DataType.blob())
        .addColumn(ITERATION_COUNT_COLUMN, DataType.cint())
        .addColumn(PASSWORD_EXPIRES_ON_COLUMN, DataType.date());

    cassandraSessionProvider.getTenantSession().execute(create);
  }

  public void add(final UserEntity instance) {
    tenantAwareEntityTemplate.save(instance);
  }


  public Optional<UserEntity> get(final String identifier)
  {
    final UserEntity instance =
        tenantAwareCassandraMapperProvider.getMapper(UserEntity.class).get(identifier);

    if (instance != null) {
      Assert.notNull(instance.getIdentifier());
      Assert.notNull(instance.getRole());
    }

    return Optional.ofNullable(instance);
  }

  public List<UserEntity> getAll()
  {
    final Mapper<UserEntity> entityMapper = tenantAwareCassandraMapperProvider.getMapper(UserEntity.class);
    final Session tenantSession = cassandraSessionProvider.getTenantSession();

    final Statement statement = QueryBuilder.select().all().from(TABLE_NAME);

    return entityMapper.map(tenantSession.execute(statement)).all();
  }
}
