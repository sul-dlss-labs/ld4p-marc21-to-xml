package edu.stanford;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthDBConnection.class})
public class AuthDBLookupTest {

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    private Record marcRecord = null;
    private AuthDBLookup authLookup = null;

    private Statement mockStatement = null;
    private Connection mockConnection = null;

    private void mockConnection() throws SQLException, IOException {
        mockConnection = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(AuthDBConnection.class);
        Mockito.when(AuthDBConnection.open()).thenReturn(mockConnection);
    }

    @Before
    public void setUp() throws IOException, SQLException {
        mockConnection();
        // Read a MARC record
        MarcStreamReader marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        marcRecord = marcReader.next();
        authLookup = new AuthDBLookup();
    }

    @After
    public void tearDown() throws Exception {
        mockStatement = null;
        mockConnection = null;
    }

    @Test
    public void openConnection() throws IOException, SQLException {
        authLookup.openConnection();
        assertThat(authLookup.authDB, instanceOf(Connection.class));
    }

    @Test
    public void openConnectionIdempotent() throws IOException, SQLException {
        authLookup.openConnection();
        assertThat(authLookup.authDB, instanceOf(Connection.class));
        Connection conn1 = authLookup.authDB;
        authLookup.openConnection();
        assertThat(authLookup.authDB, instanceOf(Connection.class));
        Connection conn2 = authLookup.authDB;
        assertEquals(conn1, conn2);
    }

    @Test
    public void closeConnection() throws Exception {
        authLookup.openConnection();
        authLookup.closeConnection();
        assertNull(authLookup.authDB);
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