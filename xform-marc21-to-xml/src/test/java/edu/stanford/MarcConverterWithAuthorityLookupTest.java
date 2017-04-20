package edu.stanford;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.marc4j.marc.Record;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class MarcConverterWithAuthorityLookupTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final String serverConfResource = "/server.conf";
    private final String serverConfPath = getClass().getResource(serverConfResource).getFile();

    private CommandLine mockCommandLine(String serverConf) {
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.hasOption("p")).thenReturn(true);
        when(cmd.getOptionValue("p")).thenReturn(serverConf);
        return cmd;
    }

    private MarcUtils marcUtils;
    private Record marcRecord;

    private AuthDBProperties authDBProperties;
    private AuthDBLookup authDBLookup;
    private MarcConverterWithAuthorityLookup marcConverterWithAuthorityLookup;

    private void authLookupMocks() {
        marcConverterWithAuthorityLookup.authDBProperties = authDBProperties;
        marcConverterWithAuthorityLookup.authLookup = authDBLookup;
    }

    @Before
    public void setUp() throws Exception {
        marcUtils = new MarcUtils();
        marcRecord = marcUtils.getMarcRecord();
        authDBLookup = SqliteUtils.sqliteAuthDBLookup();
        authDBProperties = new AuthDBProperties();
        marcConverterWithAuthorityLookup = new MarcConverterWithAuthorityLookup();
    }

    @After
    public void tearDown() throws IOException {
        marcUtils.deleteOutputPath();
        marcUtils = null;
        authDBLookup = null;
        authDBProperties = null;
        marcConverterWithAuthorityLookup = null;
    }

    @Test
    public void authLookups() throws Exception {
        authLookupMocks();
        Record record = marcConverterWithAuthorityLookup.authLookups(marcRecord);
        // This assumes the fixture data has no fields that resolve URIs
        assertEquals(record, marcRecord);
    }

    @Test
    public void authLookupInit_setAuthDBLookup() throws Exception {
        // Custom mocks for this test
        marcConverterWithAuthorityLookup = mock(MarcConverterWithAuthorityLookup.class);
        when(marcConverterWithAuthorityLookup.authLookupReset()).thenReturn(authDBLookup);
        doCallRealMethod().when(marcConverterWithAuthorityLookup).authLookupInit();
        // Test the authLookupInit()
        assertNull(marcConverterWithAuthorityLookup.authLookup);
        marcConverterWithAuthorityLookup.authLookupInit();
        assertNotNull(marcConverterWithAuthorityLookup.authLookup);
        assertThat(marcConverterWithAuthorityLookup.authLookup, instanceOf(AuthDBLookup.class));
    }

    @Test
    public void authLookupInit_setAuthDBProperties() throws IOException, SQLException {
        // Custom mocks for this test
        marcConverterWithAuthorityLookup = mock(MarcConverterWithAuthorityLookup.class);
        when(marcConverterWithAuthorityLookup.authLookupReset()).thenReturn(authDBLookup);
        doCallRealMethod().when(marcConverterWithAuthorityLookup).authLookupInit();
        // Test the authLookupInit()
        assertNull(marcConverterWithAuthorityLookup.authDBProperties);
        marcConverterWithAuthorityLookup.authLookupInit();
        assertNotNull(marcConverterWithAuthorityLookup.authDBProperties);
        assertEquals(authDBProperties, marcConverterWithAuthorityLookup.authDBProperties);
    }

    @Test
    public void authLookupClose() throws Exception {
        authLookupMocks();
        assertNotNull(marcConverterWithAuthorityLookup.authLookup);
        marcConverterWithAuthorityLookup.authLookupClose();
        assertNull(marcConverterWithAuthorityLookup.authLookup);
    }

    @Test
    public void authLookupCloseWithoutInit() throws SQLException {
        assertNull(marcConverterWithAuthorityLookup.authLookup);
        marcConverterWithAuthorityLookup.authLookupClose();
        assertNull(marcConverterWithAuthorityLookup.authLookup);
    }

    @Test
    public void setAuthDBProperties() {
        CommandLine cmd = mockCommandLine(serverConfPath);
        assertNull(marcConverterWithAuthorityLookup.authDBProperties);
        marcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        assertNotNull(marcConverterWithAuthorityLookup.authDBProperties);
    }

    @Test
    public void setAuthDBPropertiesUsesDefaultProperties() throws IOException, SQLException {
        CommandLine cmd = mock(CommandLine.class);
        when(cmd.hasOption("p")).thenReturn(false);
        assertNull(marcConverterWithAuthorityLookup.authDBProperties);
        marcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        assertNotNull(marcConverterWithAuthorityLookup.authDBProperties);
        assertEquals(authDBProperties, marcConverterWithAuthorityLookup.authDBProperties);
    }

    @Test
    public void failSetAuthDBProperties() {
        exit.expectSystemExitWithStatus(1);
        // capture the STDERR message
        PrintStream stderr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // call setAuthDBProperties with a missing file path
        assertNull(marcConverterWithAuthorityLookup.authDBProperties);
        CommandLine cmd = mockCommandLine("missing-server.conf");
        marcConverterWithAuthorityLookup.setAuthDBProperties(cmd);
        // check the STDERR message
        String err = "ERROR: Failure to set Authority-DB properties.";
        assertThat(errContent.toString(), containsString(err));
        System.setErr(stderr);
    }

    @Test
    public void printHelp() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String className = "testClassName";
        Options options = new Options();
        options.addOption("h", "help");
        MarcConverterWithAuthorityLookup.printHelp(className, options);
        assertThat(outContent.toString(), containsString(className));
        assertThat(outContent.toString(), containsString("help"));
        System.setOut(stdout);
    }

}