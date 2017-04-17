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
import static org.mockito.Mockito.*;

public class AuthDBConnectionTest {

    /*
    Unit test config:      src/test/resources/server.conf
    Packaged code config:  src/main/resources/server.conf
     */

    private AuthDBProperties authDBProperties;
    private AuthDBConnection authDBConnection;

    @Before
    public void setUp() throws IOException {
        authDBProperties = new AuthDBProperties();
        authDBConnection = new AuthDBConnection();
        authDBConnection.setAuthDBProperties(authDBProperties);
    }

    @After
    public void tearDown() {
        authDBProperties = null;
        authDBConnection = null;
    }

    @Test
    public void open() throws IOException, SQLException {
        Connection mockConnection = mock(Connection.class);
        DataSource mockDataSource = mock(DataSource.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        AuthDBConnection spyAuthDBConnection = spy(authDBConnection);
        when(spyAuthDBConnection.dataSource()).thenReturn(mockDataSource);
        Connection conn = spyAuthDBConnection.open();
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

