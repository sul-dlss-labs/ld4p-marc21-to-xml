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
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
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
import static java.nio.file.Files.createTempFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({ MarcToXMLStream.class, MarcConverterWithAuthorityLookup.class })
public class MarcToXMLStreamTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static String usage = "usage: " + MarcToXMLStream.className;

    private static Path outputPath;
    private static Path outputFile;

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    private final String marcFileResource = "/one_record.mrc";
    private final String marcFilePath = getClass().getResource(marcFileResource).getFile();

    @Before
    public void setUp() throws Exception {
        // Set the MARC file reader
        MarcStreamReader marcReader = new MarcStreamReader(new FileInputStream(marcFilePath));
        MarcToXMLStream.setMarcReader(marcReader); // usually STDIN
        // Output MARC-XML where these tests can access it easily.
        outputPath = createTempDirectory("MarcToXMLStreamTest_");
        outputFile = createTempFile(outputPath, "marc_record", "xml");
        OutputStream outFileStream = new FileOutputStream(outputFile.toString());
        MarcWriter marcWriter = new MarcXmlWriter(outFileStream, true);
        MarcToXMLStream.setMarcWriter(marcWriter); // usually STDOUT
        stubAuthLookups();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(outputPath.toFile());
    }

    @Test
    public void mainTest() throws Exception {
        PowerMockito.spy(MarcToXMLStream.class);
        PowerMockito.doNothing().when(MarcToXMLStream.class, "convertRecords");
        String [] args = new String[] {};
        MarcToXMLStream.main(args);
    }

    @Test
    public void mainHelp() throws ParseException, IOException, SQLException, ParseException, SQLException {
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
        File marcXmlFile = outputFile.toFile();
        assertTrue(marcXmlFile.exists());  // setUp() creates a temporary file
        long modifiedA = marcXmlFile.lastModified();
        // Does the file get updated?
        TimeUnit.SECONDS.sleep(1); // delay so file.lastModified() is different
        MarcToXMLStream.convertRecords();
        long modifiedB = marcXmlFile.lastModified();
        assertTrue(modifiedA < modifiedB);
        assertTrue(MarcXMLValidator.valid(outputFile.toString()));
    }

    private void stubAuthLookups() throws Exception {
        MarcStreamReader reader = new MarcStreamReader(new FileInputStream(marcFilePath));
        Record marcRecord = reader.next();
        reader = null;
        PowerMockito.mockStatic(MarcConverterWithAuthorityLookup.class);
        PowerMockito.when(MarcConverterWithAuthorityLookup.authLookups(Mockito.any())).thenReturn(marcRecord);
        PowerMockito.doNothing().when(MarcConverterWithAuthorityLookup.class, "authLookupInit");
        PowerMockito.doNothing().when(MarcConverterWithAuthorityLookup.class, "authLookupClose");
    }

}