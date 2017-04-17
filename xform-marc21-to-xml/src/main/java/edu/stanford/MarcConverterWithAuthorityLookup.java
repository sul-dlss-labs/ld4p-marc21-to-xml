package edu.stanford;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
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
class MarcConverterWithAuthorityLookup {

    static AuthDBProperties authDBProperties = null;
    static AuthDBLookup authLookup = null;

    static void addOptions(Options opts) {
        opts.addOption("h", "help", false, "help message");
        opts.addOption("p", "auth-db-property-file", true, "Authority DB connection property file");
    }

    static void printHelp(String className, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(className, options);
    }

    static void setAuthDBProperties(CommandLine cmd) {
        // Set the authority-db properties
        try {
            if (cmd.hasOption("p")) {
                // Use a properties file given on the command line
                String dbPropFile = cmd.getOptionValue("p").trim();
                authDBProperties = new AuthDBProperties(dbPropFile);
            } else {
                // Use a properties file packaged in the JAR resources
                authDBProperties = new AuthDBProperties();
            }
        } catch (IOException ex) {
            System.err.println("ERROR: Failure to set Authority-DB properties.");
            System.err.print(ex.getStackTrace());
            System.exit(1);
        }
    }

    static Record authLookups(Record record) throws IOException, SQLException {
        return authLookup.marcResolveAuthorities(record);
    }

    static void authLookupInit() throws IOException, SQLException {
        if (authLookup == null) {
            authLookup = new AuthDBLookup();
            authLookup.openConnection(authDBProperties);
        }
    }

    static void authLookupClose() throws SQLException {
        if (authLookup != null) {
            authLookup.closeConnection();
            authLookup = null;
        }
    }
}

