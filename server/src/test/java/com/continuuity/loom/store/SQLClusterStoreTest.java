/*
 * Copyright 2012-2014, Continuuity, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.continuuity.loom.store;

import com.continuuity.loom.conf.Configuration;
import com.continuuity.loom.conf.Constants;
import com.google.common.base.Throwables;
import org.apache.twill.internal.zookeeper.InMemoryZKServer;
import org.apache.twill.zookeeper.ZKClientService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 */
public class SQLClusterStoreTest extends ClusterStoreTest {
  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();
  private static InMemoryZKServer zkServer;
  private static ZKClientService zkClient;
  private static SQLClusterStore clusterStore;

  @BeforeClass
  public static void beforeClass() throws SQLException, ClassNotFoundException, IOException {
    zkServer = InMemoryZKServer.builder().setDataDir(tmpFolder.newFolder()).setTickTime(1000).build();
    zkServer.startAndWait();

    zkClient = ZKClientService.Builder.of(zkServer.getConnectionStr()).build();
    zkClient.startAndWait();

    Configuration sqlConf = Configuration.create();
    sqlConf.set(Constants.JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
    sqlConf.set(Constants.JDBC_CONNECTION_STRING, "jdbc:derby:memory:loom;create=true");
    sqlConf.setLong(Constants.ID_START_NUM, 1);
    sqlConf.setLong(Constants.ID_INCREMENT_BY, 1);
    DBConnectionPool dbConnectionPool = new DBConnectionPool(sqlConf);
    clusterStore = new SQLClusterStore(zkClient, dbConnectionPool, sqlConf);
    clusterStore.initialize();
    clusterStore.initDerbyDB();
    store = clusterStore;
    clusterStore.clearData();
  }

  @Before
  public void before() throws SQLException {
    clusterStore.clearData();
  }

  @AfterClass
  public static void afterClass() {
    zkClient.stopAndWait();
    zkServer.stopAndWait();
    try {
      DriverManager.getConnection("jdbc:derby:memory:loom;drop=true");
    } catch (SQLException e) {
      // this is normal when a drop happens
      if (!e.getSQLState().equals("08006") ) {
        Throwables.propagate(e);
      }
    }
  }
}
