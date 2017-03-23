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

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthDBConnection.class})
public class MarcConverterWithAuthorityLookupTest {

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    private Connection mockConnection = null;

    private void mockConnection() throws IOException, SQLException {
        mockConnection = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(AuthDBConnection.class);
        Mockito.when(AuthDBConnection.open()).thenReturn(mockConnection);
    }

    @Before
    public void setUp() throws Exception {
        mockConnection();
    }

    @After
    public void tearDown() throws Exception {
        mockConnection = null;
    }

    @Test
    public void authLookups() throws Exception {
        MarcConverterWithAuthorityLookup.authLookupInit();
        MarcStreamReader marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        Record marcRecord = marcReader.next();
        Record record = MarcConverterWithAuthorityLookup.authLookups(marcRecord);
        // This assumes the fixture data has no fields that resolve URIs
        assertEquals(record, marcRecord);
    }

    @Test
    public void authLookupInit() throws Exception {
        MarcConverterWithAuthorityLookup.authLookupInit();
        assertNotNull(MarcConverterWithAuthorityLookup.authLookup);
    }

    @Test
    public void authLookupClose() throws Exception {
        MarcConverterWithAuthorityLookup.authLookupInit();
        assertNotNull(MarcConverterWithAuthorityLookup.authLookup);
        MarcConverterWithAuthorityLookup.authLookupClose();
        assertNull(MarcConverterWithAuthorityLookup.authLookup);
    }

}