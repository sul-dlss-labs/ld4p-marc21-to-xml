package edu.stanford;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.marc4j.marc.Record;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 *
 */
public class AuthDBLookupTest {

    private MarcTestUtils marcTestUtils;
    private Record marcRecord;
    private AuthDBLookup authLookup;

    @Before
    public void setUp() throws Exception {
        marcTestUtils = new MarcTestUtils();
        marcRecord = marcTestUtils.getMarcRecord();
        authLookup = SqliteTestUtils.sqliteAuthDBLookup();
    }

    @After
    public void tearDown() throws Exception {
        marcTestUtils.deleteOutputPath();
        marcTestUtils = null;
        authLookup = null;
    }

    @Test
    public void openConnection() throws IOException, SQLException {
        assertNull(authLookup.dbConnection);
        authLookup.openConnection();
        assertThat(authLookup.dbConnection, instanceOf(Connection.class));
    }

    @Test
    public void openConnectionIdempotent() throws IOException, SQLException {
        assertNull(authLookup.dbConnection);
        authLookup.openConnection();
        assertThat(authLookup.dbConnection, instanceOf(Connection.class));
        Connection conn1 = authLookup.dbConnection;
        authLookup.openConnection();
        assertThat(authLookup.dbConnection, instanceOf(Connection.class));
        Connection conn2 = authLookup.dbConnection;
        assertEquals(conn1, conn2);
    }

    @Test
    public void closeConnection() throws Exception {
        assertNull(authLookup.dbConnection);
        authLookup.openConnection();
        assertThat(authLookup.dbConnection, instanceOf(Connection.class));
        authLookup.closeConnection();
        assertNull(authLookup.dbConnection);
    }

    @Test
    public void marcWithoutResolveAuthorities() throws Exception {
        authLookup.openConnection();
        Record record = authLookup.marcResolveAuthorities(marcRecord);
        assertEquals(record, marcRecord);
    }

    // TODO: use some test data with authKey that can be resolved to URIs
    // TODO: when the URIs can be resolved, the return record != marcRecord
    @Ignore("Requires stubs to resolve URIs")
    @Test
    public void marcWithResolveAuthorities() throws Exception {
        authLookup.openConnection();
        Record record = authLookup.marcResolveAuthorities(marcRecord);
        assertNotEquals(record, marcRecord);
    }
}