package edu.stanford;

import org.apache.commons.cli.*;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Uses the Marc4J library to transform the MARC record to MarcXML.
 * The MARC record must have the authority key delimiter output in the system record dump.
 * When encountering an authority key subfield delimiter ('?' or '=') it will add a subfield 0 to the MarcXML record
 * for each 92X field in order to leverage the functionality of the LOC marc2bibframe converter's ability to create
 * bf:hasAuthority elements for URI's present in that subfield (BF1.0).
 */
class MarcToXMLStream extends MarcConverterWithAuthorityLookup {

    public static void main (String [] args) throws IOException, SQLException, ParseException {
        MarcToXMLStream marcToXMLStream = new MarcToXMLStream();
        marcToXMLStream.parseArgs(args);
        marcToXMLStream.authLookupInit();
        marcToXMLStream.convertRecords();
        marcToXMLStream.authLookupClose();
    }

    void convertRecords() throws IOException, SQLException {
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(authLookups(record));
        }
        marcWriter.close();
    }

    private MarcReader marcReader = new MarcStreamReader(System.in);
    private MarcWriter marcWriter = new MarcXmlWriter(System.out, true);

    public void setMarcReader(MarcReader reader) {
        marcReader = reader;
    }

    public void setMarcWriter(MarcWriter writer) {
        marcWriter = writer;
    }

    static String className = MarcToXMLStream.class.getName();

    // Apache Commons-CLI Options
    // https://commons.apache.org/proper/commons-cli/introduction.html
    CommandLine cmd = null;
    static Options options = setOptions();

    static Options setOptions() {
        Options opts = new Options();
        MarcConverterWithAuthorityLookup.addOptions(opts);
        return opts;
    }

    void parseArgs(String [] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
        if (cmd.hasOption('h')) {
            // Print the help message and exit
            printHelp(className, options);
            System.exit(0);
        }
        // Parse optional options
        setAuthDBProperties(cmd);
    }

}
