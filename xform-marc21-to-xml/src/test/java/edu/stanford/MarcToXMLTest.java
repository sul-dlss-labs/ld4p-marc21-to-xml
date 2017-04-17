package edu.stanford;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
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

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.createTempDirectory;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({ MarcToXML.class, MarcConverterWithAuthorityLookup.class })
public class MarcToXMLTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static Path outputPath;
    private static String logFile;

    private static String usage = "usage: " + MarcToXML.className;

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    @Before
    public void setUp() throws IOException {
        outputPath = createTempDirectory("MarcToXMLTest_");
        logFile = File.createTempFile("MarcToXMLTest_", ".log", outputPath.toFile()).toString();
        MarcToXML.setLogger(logFile);
    }

    @After
    public void tearDown() throws IOException {
        MarcToXML.setLogger(null);
        MarcToXML.setMarcInputFile(null);
        MarcToXML.setXmlOutputPath(null);
        MarcToXML.setXmlReplace( false );
        FileUtils.deleteDirectory(outputPath.toFile());
        outputPath = null;
        logFile = null;
    }

    private void setupInput() throws FileNotFoundException {
        // Read a MARC binary file resource
        MarcToXML.setMarcInputFile(marcFilePath);
        MarcStreamReader marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        assertTrue(marcReader.hasNext());
    }

    private void setupOutput()  {
        MarcToXML.setXmlOutputPath(outputPath.toString());
        assertTrue(outputPath.toFile().isDirectory());
    }

    // TODO: check what happens when long options are used instead of short options?
    // TODO: might need to check for the presence of each of them to get the value?

    @Test
    public void printHelp() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        MarcToXML.printHelp(MarcToXML.className, MarcToXML.options);
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
    }

    @Test
    public void mainHelp() throws ParseException, FileNotFoundException, SQLException {
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
    public void mainInputFileTest() {
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
            assertEquals(iFile, MarcToXML.marcInputFile);
        }
    }

    @Test
    public void mainInputNotFileTest() {
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
            assertEquals(null, MarcToXML.marcInputFile);
        }
    }

    @Test
    public void mainInputFileMissingTest() {
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
            assertEquals(null, MarcToXML.marcInputFile);
        }
    }

    @Test
    public void mainOutputPathTest() throws Exception {
        stubAuthLookups();
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = marcFilePath;
        String oPath = outputPath.toString();
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
            assertEquals("", errContent.toString());
            assertEquals("", outContent.toString());
            System.setErr(stderr);
            System.setOut(stdout);
            assertEquals(oPath, MarcToXML.xmlOutputPath);
        }
    }

    @Test
    public void mainOutputPathFromEnvTest() throws Exception {
        stubAuthLookups();
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String iFile = marcFilePath;
        Path newPath = createTempDirectory("LD4P_MARCXML_");
        String oPath = newPath.toString();
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("LD4P_MARCXML")).thenReturn(oPath);
        try {
            // When -o option is missing, uses System.getenv("LD4P_MARCXML")
            String[] args = new String[] {"-i " + iFile };
            MarcToXML.main(args);
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
            assertEquals(oPath, MarcToXML.xmlOutputPath);
            FileUtils.deleteDirectory(newPath.toFile());
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
        String oPath = File.createTempFile("MarcToXMLTest_", ".txt", outputPath.toFile()).toString();
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
            assertEquals(null, MarcToXML.xmlOutputPath);
        }
    }


//    @Test
//    public void mainXMLReplaceTrueTest() throws Exception {
////        disableConversion();
//        stubAuthLookups();
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
//        Record record = readMarcRecord();
//        String result = MarcToXML.xmlOutputFilePath(record);
//        assertTrue(result.contains(outputPath.toString()));
//        String cn = record.getControlNumber();
//        assertTrue(result.contains(cn));
//        String fmt = ".xml";
//        assertTrue(result.contains(fmt));
//    }


    @Test
    public void doConversionTrueTest() throws FileNotFoundException {
        setupOutput();
        Record record = readMarcRecord();
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(record);
        File file = new File(marcXmlFilePath);
        assertFalse(file.exists());
        assertFalse(MarcToXML.xmlReplace);
        assertTrue(MarcToXML.doConversion(file, MarcToXML.xmlReplace));
    }

    @Test
    public void doConversionReplaceTest() throws IOException {
        File file = File.createTempFile("MarcToXMLTest_", ".txt", outputPath.toFile());
        assertTrue(file.exists());
        MarcToXML.setXmlReplace(true);
        assertTrue(MarcToXML.xmlReplace);
        assertTrue(MarcToXML.doConversion(file, MarcToXML.xmlReplace));
    }

    @Test
    public void doConversionWithoutReplaceTest() throws IOException {
        File file = File.createTempFile("MarcToXMLTest_", ".txt", outputPath.toFile());
        assertTrue(file.exists());
        assertFalse(MarcToXML.xmlReplace);
        assertFalse(MarcToXML.doConversion(file, MarcToXML.xmlReplace));
    }

    @Test
    public void convertRecordTest() throws Exception {
        setupInput();
        setupOutput();
        stubAuthLookups();
        Record record = readMarcRecord();
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(record);
        File file = new File(marcXmlFilePath);
        assertFalse(file.exists());
        MarcToXML.convertMarcRecord(record);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
    }

    @Test
    public void convertRecordReplaceTest() throws Exception {
        setupInput();
        setupOutput();
        stubAuthLookups();
        // Convert the record and get its file path
        Record record = readMarcRecord();
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(record);
        File marcXmlFile = new File(marcXmlFilePath);
        assertFalse(marcXmlFile.exists());
        MarcToXML.convertMarcRecord(record);
        assertTrue(marcXmlFile.exists());
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedA = marcXmlFile.lastModified();
        // Does the replacement option work?
        Boolean xmlReplace = true;
        MarcToXML.setXmlReplace( xmlReplace );
        Boolean doConversion = MarcToXML.doConversion(marcXmlFile, xmlReplace);
        assertTrue(doConversion);
        // Does the file get replaced?
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() is different
        MarcToXML.convertMarcRecord(record);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedB = marcXmlFile.lastModified();
        assertTrue(modifiedA < modifiedB);
    }

    @Test
    public void convertRecordSkipTest() throws Exception {
        setupInput();
        setupOutput();
        stubAuthLookups();
        Record record = readMarcRecord();
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(record);
        File marcXmlFile = new File(marcXmlFilePath);
        assertFalse(marcXmlFile.exists());
        MarcToXML.convertMarcRecord(record);
        assertTrue(marcXmlFile.exists());
        assertTrue(marcXmlFile.length() > 0);
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
        long modifiedA = marcXmlFile.lastModified();
        // Check the default logic that existing records are not replaced.
        assertFalse(MarcToXML.xmlReplace);
        assertFalse(MarcToXML.doConversion(marcXmlFile, MarcToXML.xmlReplace));
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() could be different
        MarcToXML.convertMarcRecord(record);
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
        String iFile = marcFilePath;
        String oPath = outputPath.toString();
        try {
            stubAuthException();
            String[] args = new String[] {"-i " + iFile, "-o " + oPath};
            MarcToXML.main(args);
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

    private void stubAuthInitClose() throws Exception {
        PowerMockito.doNothing().when(MarcConverterWithAuthorityLookup.class, "authLookupInit");
        PowerMockito.doNothing().when(MarcConverterWithAuthorityLookup.class, "authLookupClose");
    }

    private void stubAuthException() throws Exception {
        PowerMockito.mockStatic(MarcConverterWithAuthorityLookup.class);
        PowerMockito.when(MarcConverterWithAuthorityLookup.authLookups(Mockito.any())).thenThrow(new SQLException("SQL exception message"));
        stubAuthInitClose();
    }

    private void stubAuthLookups() throws Exception {
        Record record = readMarcRecord();
        PowerMockito.mockStatic(MarcConverterWithAuthorityLookup.class);
        PowerMockito.when(MarcConverterWithAuthorityLookup.authLookups(Mockito.any())).thenReturn(record);
        stubAuthInitClose();
    }

    private Record readMarcRecord() throws FileNotFoundException {
        FileInputStream iStream = new FileInputStream(marcFilePath);
        MarcStreamReader reader = new MarcStreamReader(iStream);
        assertTrue(reader.hasNext());
        return reader.next();
    }
}