package edu.stanford;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class AuthDBConnectionTest {

    /*
    Unit test config:      src/test/resources/server.conf
    Packaged code config:  src/main/resources/server.conf
     */

    private AuthDBProperties authDBProperties;
    private AuthDBConnection authDBConnection;

    @Before
    public void setUp() throws IOException, SQLException {
        authDBProperties = new AuthDBProperties();
        authDBConnection = SqliteUtils.sqliteAuthDBConnection();
    }

    @After
    public void tearDown() {
        authDBProperties = null;
        authDBConnection = null;
    }

    @Test
    public void setAuthDBProperties() {
        authDBConnection.setAuthDBProperties(authDBProperties);
        assertNotNull(authDBConnection.authDBProperties);
        assertSame(authDBConnection.authDBProperties, authDBProperties);
    }


    @Test
    public void open() throws IOException, SQLException {
        Connection conn = authDBConnection.open();
        assertNotNull(conn);
        assertThat(conn, instanceOf(Connection.class));
    }

    @Test
    public void dataSource() throws IOException, SQLException {
        DataSource dataSource = authDBConnection.dataSource();
        assertNotNull(dataSource);
        assertThat(dataSource, instanceOf(DataSource.class));
    }

    @Test
    public void dataSourceCache() throws Exception {
        Properties cacheProps = authDBConnection.dataSourceCache();
        assertEquals(cacheProps.getProperty("MinLimit"), "1");
        assertEquals(cacheProps.getProperty("InitialLimit"), "1");
        assertEquals(cacheProps.getProperty("AbandonedConnectionTimeout"), "100");
        assertEquals(cacheProps.getProperty("PropertyCheckInterval"), "80");
    }

}

