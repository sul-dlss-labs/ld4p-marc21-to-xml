package edu.stanford;

import org.apache.commons.cli.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 *
 */
public class MarcXMLValidatorTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final String validFileResource = "/one_record.xml";
    private final String validMarcFilePath = getClass().getResource(validFileResource).getFile();

    private final String invalidFileResource = "/invalid_record.xml";
    private final String invalidMarcFilePath = getClass().getResource(invalidFileResource).getFile();

    private static String usage = "usage: edu.stanford.MarcXMLValidator";

    @Test
    public void setOptions() {
        Options options = MarcXMLValidator.options;
        assertNotNull(options.getMatchingOptions("i"));
        assertNotNull(options.getMatchingOptions("h"));
    }

    @Test
    public void printHelp() {
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        MarcXMLValidator.printHelp();
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
    }

    @Test
    public void mainHelp() throws ParseException {
        exit.expectSystemExit();
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = new String[]{"-h"};
        MarcXMLValidator.main(args);
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
    }

    @Test
    public void parseExistingInputFile() throws ParseException {
        // replicate the code for parsing the args
        String[] args = new String[]{"-i " + validMarcFilePath};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(MarcXMLValidator.options, args);
        String iFile = cmd.getOptionValue("i");
        String parsedFile = MarcXMLValidator.parseInputFile(iFile);
        assertEquals(validMarcFilePath, parsedFile);
    }

    @Test
    public void parseNullInputFile() throws ParseException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // replicate the code for parsing the args and parse the input file
        String[] args = new String[]{};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(MarcXMLValidator.options, args);
        String iFile = cmd.getOptionValue("i");
        MarcXMLValidator.parseInputFile(iFile);
        // Verify the status messages
        String err = "ERROR: No MARC-XML input file specified.\n";
        assertEquals(err, errContent.toString());
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
        System.setErr(stderr);
    }

    @Test
    public void parseMissingInputFile() throws ParseException {
        exit.expectSystemExitWithStatus(1);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // replicate the code for parsing the args and parse the input file
        String[] args = new String[]{"-i " + "missing_file.xml"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(MarcXMLValidator.options, args);
        String iFile = cmd.getOptionValue("i");
        MarcXMLValidator.parseInputFile(iFile);
        // Verify the status messages
        String err = "ERROR: MARC-XML input file is not a file.\n";
        assertEquals(err, errContent.toString());
        assertThat(outContent.toString(), containsString(usage));
        System.setOut(stdout);
        System.setErr(stderr);
    }

    @Test
    public void mainReallyValid() throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // validate the file
        String[] args = new String[]{"-i " + validMarcFilePath};
        MarcXMLValidator.main(args);
        // Verify the status messages
        assertEquals("", errContent.toString());
        assertThat(outContent.toString(), containsString("VALID"));
        System.setOut(stdout);
        System.setErr(stderr);
    }

    @Test
    public void mainReallyInvalid() throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        // validate the file
        String[] args = new String[]{"-i " + invalidMarcFilePath};
        MarcXMLValidator.main(args);
        // Verify the status messages
        String err = "Invalid content was found";
        assertThat(errContent.toString(), containsString(err));
        assertThat(outContent.toString(), containsString("INVALID"));
        System.setOut(stdout);
        System.setErr(stderr);
    }

}

