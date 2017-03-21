package edu.stanford;

import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class MarcXMLValidator {

    // Apache Commons-CLI Options
    // https://commons.apache.org/proper/commons-cli/introduction.html
    static CommandLine cmd = null;
    static Options options = setOptions();

    static Options setOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "help message");
        opts.addOption("i", "inputFile", true, "MARC-XML input file");
        return opts;
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(MarcXMLValidator.class.getName(), options);
    }

    static String parseInputFile(String iFile) {
        // Parse and set the input file
        if (iFile == null) {
            System.err.println("ERROR: No MARC-XML input file specified.");
            printHelp();
            System.exit(1);
        }
        // Check the input file exists
        File marcXmlFile = new File(iFile.trim());
        if (! marcXmlFile.isFile()) {
            System.err.println("ERROR: MARC-XML input file is not a file.");
            printHelp();
            System.exit(1);
        }
        return marcXmlFile.toString();
    }

    public static void main(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
        // Print the help message and exit?
        if (cmd.hasOption('h')) {
            printHelp();
            System.exit(0);
        }
        String marcXmlFilePath = parseInputFile( cmd.getOptionValue("i") );
        if (valid(marcXmlFilePath)) {
            System.out.println("VALID:   " + marcXmlFilePath);
        } else {
            System.out.println("INVALID: " + marcXmlFilePath);
        }
    }

    static boolean valid(String marcXmlFilePath) {
        try {
            File xmlFile = new File(marcXmlFilePath);
            Source xmlSource = new StreamSource(xmlFile);
            setMarcXmlValidator();
            marcXmlValidator.validate(xmlSource);
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private static Validator marcXmlValidator = null;

    private static void setMarcXmlValidator() throws SAXException, IOException {
        if (marcXmlValidator == null) {
            // MARC21 XSD Schema is from https://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd
            InputStream isSchema = MarcXMLValidator.class.getResourceAsStream("/MARC21slim.xsd");
            Source schemaSource = new StreamSource(isSchema);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaSource);
            marcXmlValidator = schema.newValidator();
        }
    }

}