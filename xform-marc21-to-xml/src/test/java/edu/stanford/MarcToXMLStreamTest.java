package edu.stanford;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.marc4j.marc.Record;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 *
 */
public class MarcToXMLStreamTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static String usage = "usage: " + MarcToXMLStream.className;

    private MarcUtils marcUtils;
//    private Record marcRecord;

    private MarcToXMLStream marcToXMLStream;

    private void mockMarcToXMLStream() throws Exception {
        marcToXMLStream = spy(MarcToXMLStream.class);
        //marcToXMLStream.authLookup = SqliteUtils.sqliteAuthDBLookup();
    }

    private void mockAuthException() throws Exception {
        mockMarcToXMLStream();
        doThrow(new SQLException("SQL exception message")).when(marcToXMLStream).authLookups(any(Record.class));
    }

    private void mockAuthLookups() throws Exception {
        mockMarcToXMLStream();
        Record record = marcUtils.getMarcRecord();
        doReturn(record).when(marcToXMLStream).authLookups(any(Record.class));
        doNothing().when(marcToXMLStream).authLookupInit();
        doNothing().when(marcToXMLStream).authLookupClose();
    }

    @Before
    public void setUp() throws Exception {
        marcUtils = new MarcUtils();
//        marcRecord = marcUtils.getMarcRecord();
        mockAuthLookups();
        marcToXMLStream.setMarcReader(marcUtils.getMarcReader());
        marcToXMLStream.setMarcWriter(marcUtils.getMarcWriter());
    }

    @After
    public void tearDown() throws IOException {
        marcUtils.deleteOutputPath();
        marcUtils = null;
        marcToXMLStream = null;
    }

//    @Test
//    public void mainTest() throws Exception {
//        PowerMockito.whenNew(MarcToXMLStream.class).withNoArguments().thenReturn(marcToXMLStream);
////        doNothing().when(marcToXMLStream).convertRecords();
//        String [] args = new String[] {};
//        MarcToXMLStream.main(args);
//    }

    @Test
    public void mainHelp() throws IOException, ParseException, SQLException {
        exit.expectSystemExit();
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = new String[]{"-h"};
        MarcToXMLStream.main(args);
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
    }

    @Test
    public void convertRecordsTest() throws Exception {
        Path marcOutput = marcUtils.outputFile;
        File marcXmlFile = marcOutput.toFile();
        assertTrue(marcXmlFile.exists());  // setUp() creates a temporary file
        long modifiedA = marcXmlFile.lastModified();
        // Does the file get updated?
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() is different
        marcToXMLStream.convertRecords();
        long modifiedB = marcXmlFile.lastModified();
        assertTrue(modifiedA < modifiedB);
        assertTrue(MarcXMLValidator.valid(marcOutput.toString()));
    }

}