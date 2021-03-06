/*
 * Copyright (C) 2013 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.mocks.StubConnection;
import com.zaxxer.hikari.mocks.StubDataSource;

/**
 * @author Brett Wooldridge
 */
public class UnwrapTest
{
    @Test
    public void testUnwrapConnection() throws SQLException
    {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(1);
        config.setInitializationFailFast(true);
        config.setConnectionTestQuery("VALUES 1");
        config.setDataSourceClassName("com.zaxxer.hikari.mocks.StubDataSource");

        HikariDataSource ds = new HikariDataSource(config);

        try {
            ds.getConnection().close();
            Assert.assertSame("Idle connections not as expected", 1, TestElf.getPool(ds).getIdleConnections());
    
            Connection connection = ds.getConnection();
            Assert.assertNotNull(connection);
    
            StubConnection unwrapped = connection.unwrap(StubConnection.class);
            Assert.assertTrue("unwrapped connection is not instance of StubConnection: " + unwrapped, (unwrapped != null && unwrapped instanceof StubConnection));
        }
        finally {
            ds.close();
        }
    }

    @Test
    public void testUnwrapDataSource() throws SQLException
    {
       HikariConfig config = new HikariConfig();
       config.setMinimumIdle(1);
       config.setMaximumPoolSize(1);
       config.setInitializationFailFast(true);
       config.setConnectionTestQuery("VALUES 1");
       config.setDataSourceClassName("com.zaxxer.hikari.mocks.StubDataSource");

       HikariDataSource ds = new HikariDataSource(config);
       try {
          StubDataSource unwrap = ds.unwrap(StubDataSource.class);
          Assert.assertNotNull(unwrap);
          Assert.assertTrue(unwrap instanceof StubDataSource);

          Assert.assertTrue(ds.isWrapperFor(HikariDataSource.class));
          Assert.assertTrue(ds.unwrap(HikariDataSource.class) instanceof HikariDataSource);

          Assert.assertFalse(ds.isWrapperFor(getClass()));
          try {
             ds.unwrap(getClass());
          }
          catch (SQLException e) {
             Assert.assertTrue(e.getMessage().contains("Wrapped DataSource"));
          }
       }
       finally {
           ds.close();
       }
    }
}
