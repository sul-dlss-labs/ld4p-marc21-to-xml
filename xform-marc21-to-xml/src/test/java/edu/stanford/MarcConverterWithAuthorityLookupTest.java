package edu.stanford;

import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({AuthDBConnection.class})
public class MarcConverterWithAuthorityLookupTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    private final String serverConfResource = "/server.conf";
    private final String serverConfPath = getClass().getResource(serverConfResource).getFile();

    private Connection mockConnection = null;

    private void mockConnection() throws IOException, SQLException {
        mockConnection = mock(Connection.class);
        PowerMockito.mockStatic(AuthDBConnection.class);
        Mockito.when(AuthDBConnection.open(any())).thenReturn(mockConnection);
    }

    @Before
    public void setUp() throws Exception {
        mockConnection();
        MarcConverterWithAuthorityLookup.authDBProperties = null;
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

    @Test
    public void authLookupCloseWithoutInit() throws SQLException {
        assertNull(MarcConverterWithAuthorityLookup.authLookup);
        MarcConverterWithAuthorityLookup.authLookupClose();
        assertNull(MarcConverterWithAuthorityLookup.authLookup);
    }

    @Test
    public void setAuthDBPropertiesDefault() {
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.hasOption("p")).thenReturn(false);
        assertNull(MarcConverterWithAuthorityLookup.authDBProperties);
        MarcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        assertNotNull(MarcConverterWithAuthorityLookup.authDBProperties);
    }

    @Test
    public void setAuthDBProperties() {
        CommandLine cmd = mockCommandLine(serverConfPath);
        assertNull(MarcConverterWithAuthorityLookup.authDBProperties);
        MarcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        assertNotNull(MarcConverterWithAuthorityLookup.authDBProperties);
    }

    @Test
    public void failSetAuthDBProperties() {
        exit.expectSystemExitWithStatus(1);
        // capture the STDERR message
        PrintStream stderr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // call setAuthDBProperties with a missing file path
        assertNull(MarcConverterWithAuthorityLookup.authDBProperties);
        CommandLine cmd = mockCommandLine("missing-server.conf");
        MarcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        // check the STDERR message
        String err = "ERROR: Failure to set Authority-DB properties.";
        assertThat(errContent.toString(), containsString(err));
        System.setErr(stderr);
    }

    private CommandLine mockCommandLine(String serverConf) {
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.hasOption("p")).thenReturn(true);
        when(cmd.getOptionValue("p")).thenReturn(serverConf);
        return cmd;
    }
}