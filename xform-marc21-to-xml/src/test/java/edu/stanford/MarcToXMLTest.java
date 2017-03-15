package edu.stanford;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.times;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({ MarcToXML.class })
public class MarcToXMLTest {

    private static String logFile;
    private static Path outputPath;
    private static Options options = MarcToXML.options;

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    private MarcStreamReader marcReader = null;
    private Record marcRecord = null;

    @Before
    public void setUp() throws IOException {
        outputPath = createTempDirectory("MarcToXMLTest_");
        logFile = File.createTempFile("MarcToXMLTest_", ".log", outputPath.toFile()).toString();
        MarcToXML.setLogger(logFile);
        MarcToXML.setXmlOutputPath(outputPath.toString());
        // Read a MARC binary file resource
        marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        assertTrue(marcReader.hasNext());
        MarcToXML.setMarcInputFile(marcFilePath);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(outputPath.toFile());
    }

    @Test
    public void mainTest() throws Exception {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("LD4P_MARCXML")).thenReturn(null);
        PowerMockito.mockStatic(MarcToXML.class);
        PowerMockito.when(MarcToXML.doConversion(Mockito.any(), Mockito.anyBoolean())).thenReturn(false);
        String [] args = new String[] {"-i " + marcFilePath};
        MarcToXML.main(args);
        assertNotNull(options.getMatchingOptions("i"));
        PowerMockito.verifyStatic(times(1)); // main() calls doConversion() once.
    }

    @Test
    public void convertRecordTest() throws IOException, ParseException, SQLException {
        marcRecord = marcReader.next();
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(marcRecord);
        File file = new File(marcXmlFilePath);
        assertFalse(file.exists());
        convertRecordUsingStubAuthDB(marcRecord);
        assertTrue(file.exists());
        assertTrue(MarcXMLValidator.valid(marcXmlFilePath));
    }

    @Test
    public void convertRecordReplaceTest() throws IOException, ParseException, InterruptedException {
        // Mimic the command line code to set the replacement option and set it.
        MarcToXML.setOptions();
        String [] args = new String[] {"-r"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        Boolean xmlReplace = cmd.hasOption("r");
        MarcToXML.setXmlReplace( xmlReplace );
        // Convert the record and get its file path
        marcRecord = marcReader.next();
        convertRecordUsingStubAuthDB(marcRecord);
        String marcXmlFilePath = MarcToXML.xmlOutputFilePath(marcRecord);
        File marcXmlFile = new File(marcXmlFilePath);
        long modifiedA = marcXmlFile.lastModified();
        // Does the replacement option work?
        Boolean doConversion = MarcToXML.doConversion(marcXmlFile, xmlReplace);
        assertTrue(doConversion);
        // Does the file get replaced?
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() is different
        convertRecordUsingStubAuthDB(marcRecord);
        long modifiedB = marcXmlFile.lastModified();
        assertTrue(modifiedA < modifiedB);
    }

    void convertRecordUsingStubAuthDB(Record marcRecord) {
        PowerMockito.mockStatic(MarcToXML.class, CALLS_REAL_METHODS);
        PowerMockito.stub(PowerMockito.method(MarcToXML.class, "authorityLookup")).toReturn(marcRecord);
        MarcToXML.convertMarcRecord(marcRecord);
    }

    // TODO: read in the one_record.xml file
    // TODO: use XMLUnit to check the output file has the same content
    // TODO: see http://www.xmlunit.org/
    // TODO: use a test MARC record that requires AuthDB access to resolve URIs?

    @Test
    public void inputFileNameTest() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String noFilePath = null;
        try {
            MarcToXML.setMarcInputFile(noFilePath);
        } catch (Throwable expected) {
            assertEquals(NullPointerException.class, expected.getClass());
        }
        String errMsg = "ERROR: No MARC input file specified.\n";
        assertEquals(errMsg, errContent.toString());
    }

    @Test
    public void outputFileNameTest() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("LD4P_MARCXML")).thenReturn(outputPath.toString());
        String noFilePath = null;
        MarcToXML.setXmlOutputPath(noFilePath);
        assertEquals(MarcToXML.xmlOutputPath, System.getenv("LD4P_MARCXML"));

        marcRecord = marcReader.next();
        String result = MarcToXML.xmlOutputFilePath(marcRecord);
        assertTrue(result.contains(outputPath.toString()));
        String cn = marcRecord.getControlNumber();
        assertTrue(result.contains(cn));
        String fmt = ".xml";
        assertTrue(result.contains(fmt));
    }

    private void debugInspections() {
        marcRecord = marcReader.next();
        List cFields = marcRecord.getControlFields();
        List dFields = marcRecord.getDataFields();
        System.err.println(cFields.toString());
        System.err.println(dFields.toString());
    }

}