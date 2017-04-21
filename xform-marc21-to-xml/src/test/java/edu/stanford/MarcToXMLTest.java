package edu.stanford;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 *
 */
public class MarcToXMLTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static String usage = "usage: " + MarcToXML.className;

    private Path outputPath;
    private String logFile;

    private String marcFilePath;

    private MarcUtils marcUtils;
    private MarcToXML marcToXML;

    private void mockMarcToXML() throws Exception {
        marcToXML = spy(MarcToXML.class);
        //marcToXML.authDBLookup = SqliteUtils.sqliteAuthDBLookup();
    }

    private void mockAuthException() throws Exception {
        mockMarcToXML();
        doThrow(new SQLException("SQL exception message")).when(marcToXML).authLookups(any(Record.class));
    }

    private void mockAuthLookups() throws Exception {
        mockMarcToXML();
        Record record = marcUtils.getMarcRecord();
        doReturn(record).when(marcToXML).authLookups(any(Record.class));
        doNothing().when(marcToXML).authLookupInit();
        doNothing().when(marcToXML).authLookupClose();
    }

    @Before
    public void setUp() throws IOException {
        marcUtils = new MarcUtils();
        marcUtils.createOutputPath();
        outputPath = marcUtils.outputPath;
        marcFilePath = marcUtils.marcFilePath;
        marcToXML = new MarcToXML();
    }

    @After
    public void tearDown() throws IOException {
        marcToXML.setLogger(null);
        marcToXML = null;
        marcFilePath = null;
        outputPath = null;
        logFile = null;
        marcUtils.deleteOutputPath();
        marcUtils = null;
    }

    private void setupIO() throws IOException {
        setupInput();
        setupOutput();
        setupLogger();
    }

    private void setupInput() throws FileNotFoundException {
        // Access to a MARC binary file resource
        marcToXML.setMarcInputFile(marcFilePath);
        MarcStreamReader marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        assertTrue(marcReader.hasNext());
    }

    private void setupOutput()  {
        marcToXML.setXmlOutputPath(outputPath.toString());
        assertTrue(outputPath.toFile().isDirectory());
    }

    private void setupLogger() throws IOException {
        logFile = marcUtils.createOutputFile("MarcToXMLTest", ".log").toString();
        marcToXML.setLogger(logFile);
    }

    // TODO: check what happens when long options are used instead of short options?
    // TODO: might need to check for the presence of each of them to get the value?

    @Test
    public void mainHelp() throws ParseException, IOException, SQLException {
        exit.expectSystemExit();
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = new String[]{"-h"};
        MarcToXML.main(args);
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
    }

    @Test
    public void mainInputFileTest() throws IOException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = marcFilePath;
        try {
            String[] args = new String[] {"-i " + iFile};
            MarcToXML.main(args);
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            String errMsg = "ERROR: No MARC-XML output path specified.\n";
            assertEquals(errMsg, errContent.toString());
            assertThat(outContent.toString(), containsString(usage));
            System.setErr(stderr);
            System.setOut(stdout);
        }
    }

    @Test
    public void inputFileTest() throws IOException {
        String iFile = marcFilePath;
        marcToXML.setMarcInputFile(marcFilePath);
        assertEquals(iFile, marcToXML.marcInputFile);
    }

    @Test
    public void mainInputNotFileTest() throws IOException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = "marc_file.mrc";
        try {
            String[] args = new String[] {"-i " + iFile};
            MarcToXML.main(args);
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            String errMsg = "ERROR: MARC input file is not a file.\n";
            assertEquals(errMsg, errContent.toString());
            assertThat(outContent.toString(), containsString(usage));
            System.setErr(stderr);
            System.setOut(stdout);
        }
    }

    @Test
    public void mainInputFileMissingTest() throws IOException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        try {
            String[] args = new String[]{};
            MarcToXML.main(args);
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            String errMsg = "ERROR: No MARC input file specified.\n";
            assertEquals(errMsg, errContent.toString());
            assertThat(outContent.toString(), containsString(usage));
            System.setErr(stderr);
            System.setOut(stdout);
        }
    }

    @Test
    public void outputPathTest() throws Exception {
        String oPath = outputPath.toString();
        marcToXML.setXmlOutputPath(oPath);
        assertEquals(oPath, marcToXML.xmlOutputPath);
    }

    @Test
    public void mainOutputPathTest() throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = marcFilePath;
        String oPath = outputPath.toString();
        try {
            String[] args = new String[]{"-i " + iFile, "-o " + oPath};
            MarcToXML.main(args);
        } catch (SQLRecoverableException e) {
            assertEquals(SQLRecoverableException.class, e.getClass());
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            assertEquals("", errContent.toString());
            assertEquals("", outContent.toString());
            System.setErr(stderr);
            System.setOut(stdout);
        }
    }

    @Test
    public void mainOutputPathNotDirectoryTest() throws IOException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = marcFilePath;
        String oPath = marcUtils.createOutputFile("outputFileIsNotDirectory", ".xml").toString();
        try {
            String[] args = new String[] {"-i " + iFile, "-o " + oPath};
            MarcToXML.main(args);
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            String err = "ERROR: MARC-XML output path is not a directory.\n";
            assertEquals(err, errContent.toString());
            assertThat(outContent.toString(), containsString(usage));
            System.setErr(stderr);
            System.setOut(stdout);
        }
    }


//    @Test
//    public void mainXMLReplaceTrueTest() throws Exception {
////        disableConversion();
//        mockAuthLookups();
//        PrintStream stdout = System.out;
//        PrintStream stderr = System.err;
//        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outContent));
//        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(errContent));
//        String iFile = marcFilePath;
//        String oPath = outputPath.toString();
//        try {
//            // Must provide valid args that are parsed prior to the -r flag
//            String[] args = new String[] {"-i " + iFile, "-o " + oPath, "-r"};
//            MarcToXML.main(args);
//        } catch (SQLException e) {
//            assertEquals(SQLException.class, e.getClass());
//        } catch (ParseException e) {
//            assertEquals(ParseException.class, e.getClass());
//        } catch (FileNotFoundException e) {
//            assertEquals(FileNotFoundException.class, e.getClass());
//        } finally {
//            assertEquals("", errContent.toString());
//            assertEquals("", outContent.toString());
//            System.setErr(stderr);
//            System.setOut(stdout);
//            assertEquals(true, MarcToXML.xmlReplace);
//            MarcToXML.setXmlReplace(false);
//        }
//    }


//    @Test
//    public void outputFileNameTest() {
//        PowerMockito.mockStatic(System.class);
//        PowerMockito.when(System.getenv("LD4P_MARCXML")).thenReturn(outputPath.toString());
//        String noFilePath = null;
//        MarcToXML.setXmlOutputPath(noFilePath);
//        assertEquals(System.getenv("LD4P_MARCXML"), MarcToXML.xmlOutputPath);
//
//        Record record = marcUtils.getMarcRecord();
//        String result = MarcToXML.xmlOutputFilePath(record);
//        assertTrue(result.contains(outputPath.toString()));
//        String cn = record.getControlNumber();
//        assertTrue(result.contains(cn));
//        String fmt = ".xml";
//        assertTrue(result.contains(fmt));
//    }


    @Test
    public void doConversionTrueTest() throws Exception {
        setupOutput();
        Record record = marcUtils.getMarcRecord();
        String marcXmlFilePath = marcToXML.xmlOutputFilePath(record);
        File file = new File(marcXmlFilePath);
        assertFalse(file.exists());
        assertFalse(marcToXML.xmlReplace);
        assertTrue(marcToXML.doConversion(file, marcToXML.xmlReplace));
    }

    @Test
    public void doConversionReplaceTest() throws IOException {
        File file = marcUtils.createOutputFile("replaceXML", ".xml").toFile();
        assertTrue(file.exists());
        marcToXML.setXmlReplace(true);
        assertTrue(marcToXML.xmlReplace);
        assertTrue(marcToXML.doConversion(file, marcToXML.xmlReplace));
    }

    @Test
    public void doConversionWithoutReplaceTest() throws IOException {
        File file = marcUtils.createOutputFile("replaceXML", ".xml").toFile();
        assertTrue(file.exists());
        assertFalse(marcToXML.xmlReplace);
        assertFalse(marcToXML.doConversion(file, marcToXML.xmlReplace));
    }

    @Test
    public void convertRecordTest() throws Exception {
        mockAuthLookups();
        setupIO();
        Record record = marcUtils.getMarcRecord();
        String marcXmlFilePath = marcToXML.xmlOutputFilePath(record);
        File file = new File(marcXmlFilePath);
        assertFalse(file.exists());
        marcToXML.convertMarcRecord(record);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
    }

    @Test
    public void convertRecordReplaceTest() throws Exception {
        mockAuthLookups();
        setupIO();
        // Convert the record and get its file path
        Record record = marcUtils.getMarcRecord();
        String marcXmlFilePath = marcToXML.xmlOutputFilePath(record);
        File marcXmlFile = new File(marcXmlFilePath);
        assertFalse(marcXmlFile.exists());
        marcToXML.convertMarcRecord(record);
        assertTrue(marcXmlFile.exists());
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedA = marcXmlFile.lastModified();
        // Does the replacement option work?
        Boolean xmlReplace = true;
        marcToXML.setXmlReplace( xmlReplace );
        Boolean doConversion = marcToXML.doConversion(marcXmlFile, xmlReplace);
        assertTrue(doConversion);
        // Does the file get replaced?
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() is different
        marcToXML.convertMarcRecord(record);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedB = marcXmlFile.lastModified();
        assertTrue(modifiedA < modifiedB);
    }

    @Test
    public void convertRecordSkipTest() throws Exception {
        mockAuthLookups();
        setupIO();
        Record record = marcUtils.getMarcRecord();
        String marcXmlFilePath = marcToXML.xmlOutputFilePath(record);
        File marcXmlFile = new File(marcXmlFilePath);
        assertFalse(marcXmlFile.exists());
        marcToXML.convertMarcRecord(record);
        assertTrue(marcXmlFile.exists());
        assertTrue(marcXmlFile.length() > 0);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedA = marcXmlFile.lastModified();
        // Check the default logic that existing records are not replaced.
        assertFalse(marcToXML.xmlReplace);
        assertFalse(marcToXML.doConversion(marcXmlFile, marcToXML.xmlReplace));
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() could be different
        marcToXML.convertMarcRecord(record);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedB = marcXmlFile.lastModified();
        assertEquals(modifiedA, modifiedB);
    }

    @Test
    public void convertRecordExceptionTest() throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        try {
            mockAuthException();
            setupIO();
            marcToXML.convertMarcRecords();
        } catch (SQLException e) {
            assertEquals(SQLException.class, e.getClass());
        } catch (ParseException e) {
            assertEquals(ParseException.class, e.getClass());
        } catch (FileNotFoundException e) {
            assertEquals(FileNotFoundException.class, e.getClass());
        } finally {
            System.setErr(stderr);
            System.setOut(stdout);
            String err = "SQL exception message";
            assertEquals("", outContent.toString());
            assertThat(errContent.toString(), containsString(err));
        }
    }

}